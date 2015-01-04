/*
 * Copyright 2004-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.cindy.session.nio;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import net.sf.cindy.Buffer;
import net.sf.cindy.Packet;
import net.sf.cindy.SessionType;
import net.sf.cindy.buffer.BufferFactory;
import net.sf.cindy.packet.DefaultPacket;
import net.sf.cindy.session.SessionException;
import net.sf.cindy.session.nio.reactor.ReactorHandler;
import net.sf.cindy.util.ChannelUtils;
import net.sf.cindy.util.Configuration;

/**
 * Socket channel session.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class SocketChannelSession extends AbstractChannelSession {

    private SocketChannel channel;
    private SocketAddress address;

    public SessionType getSessionType() {
        return SessionType.TCP;
    }

    public SocketAddress getLocalAddress() {
        if (isStarted())
            return channel.socket().getLocalSocketAddress();
        return super.getLocalAddress();
    }

    public SocketAddress getRemoteAddress() {
        if (isStarted())
            return address;
        return super.getRemoteAddress();
    }

    /**
     * Set the socket channel which the session will used.
     * 
     * @param channel
     *            the scoket channel
     * @throws IllegalStateException
     */
    public void setChannel(SocketChannel channel) {
        if (isStarted())
            throw new IllegalStateException(
                    "can't set socket channel after session started");
        this.channel = channel;
    }

    /**
     * Get the socket channel which the session will connected to.
     * 
     * @return the scoket channel
     */
    public SocketChannel getChannel() {
        return channel;
    }

    public Socket getSocket() {
        SocketChannel sc = channel;
        return sc == null ? null : sc.socket();
    }

    protected ReactorHandler getReactorHandler() {
        return new ChannelReactorHandler() {

            public SelectableChannel[] getChannels() {
                return new SelectableChannel[] { channel };
            }

            public void onRegistered() {
                // don't dispatch registered event until connect finished.
                if (channel.isConnected()) {
                    address = channel.socket().getRemoteSocketAddress();
                    super.onRegistered();
                }
            }

            public void onConnectable() {
                try {
                    if (channel == null)
                        return;
                    channel.finishConnect();
                    address = channel.socket().getRemoteSocketAddress();
                    // dispacth registered event
                    super.onRegistered();
                } catch (ConnectException ce) {
                    close();
                } catch (IOException e) {
                    dispatchException(new SessionException(e));
                    close();
                }
            }

            protected void read() throws IOException {
                Buffer buffer = BufferFactory.allocate(getReadPacketSize());
                int n = -1;
                int readCount = 0;

                try {
                    while ((n = buffer.read(channel)) >= 0) {
                        if (n == 0)
                            break;
                        readCount += n;
                    }
                } catch (IOException e) {
                    buffer.release();
                    throw e;
                }

                if (readCount > 0) {
                    buffer.flip();
                    getSessionFilterChain(false).packetReceived(
                            new DefaultPacket(buffer, address));
                }
                if (n < 0) // Connection closed
                    throw new ClosedChannelException();
            }

            protected boolean write(Packet packet) throws IOException {
                Buffer buffer = packet.getContent();
                while (true) {
                    int n = buffer.write(channel);
                    if (!buffer.hasRemaining())
                        return true;
                    else if (n == 0) {
                        // have more data, but the kennel buffer
                        // is full, wait next time to write
                        return false;
                    }
                }
            }

        };
    }

    protected void doStart() throws IOException {
        if (getRemoteAddress() == null && channel == null)
            throw new IOException(
                    "must specify remote address or socket channel before start");
        if (channel != null)
            return;
        try {
            channel = SocketChannel.open();
            Socket socket = channel.socket();

            int recvBufferSize = Configuration.getRecvBufferSize();
            if (recvBufferSize > 0)
                socket.setReceiveBufferSize(recvBufferSize);

            int sendBufferSize = Configuration.getSendBufferSize();
            if (sendBufferSize > 0)
                socket.setSendBufferSize(sendBufferSize);

            socket.setReuseAddress(Configuration.isReuseSessionAddress());
            socket.setTcpNoDelay(Configuration.isTcpNoDelay());

            int soLinger = Configuration.getSoLinger();
            socket.setSoLinger(soLinger >= 0, soLinger);

            channel.configureBlocking(false);
            if (getLocalAddress() != null)
                channel.socket().bind(getLocalAddress());
            channel.connect(getRemoteAddress());
        } catch (IOException e) {
            doClose();
            throw e;
        }
    }

    protected void doClose() {
        ChannelUtils.close(channel);
        channel = null;
        address = null;
    }

}
