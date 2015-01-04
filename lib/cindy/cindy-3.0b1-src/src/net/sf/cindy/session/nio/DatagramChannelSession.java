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
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;

import net.sf.cindy.Buffer;
import net.sf.cindy.Packet;
import net.sf.cindy.SessionType;
import net.sf.cindy.buffer.BufferFactory;
import net.sf.cindy.packet.DefaultPacket;
import net.sf.cindy.session.nio.reactor.ReactorHandler;
import net.sf.cindy.util.ChannelUtils;
import net.sf.cindy.util.Configuration;

/**
 * Datagram channel session.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class DatagramChannelSession extends AbstractChannelSession {

    private DatagramChannel channel;

    public DatagramSocket getSocket() {
        DatagramChannel dc = channel;
        if (dc == null)
            return null;
        return dc.socket();
    }

    public SessionType getSessionType() {
        return SessionType.UDP;
    }

    public SocketAddress getRemoteAddress() {
        if (isStarted())
            return channel.socket().getRemoteSocketAddress();
        return super.getRemoteAddress();
    }

    public SocketAddress getLocalAddress() {
        if (isStarted())
            return channel.socket().getLocalSocketAddress();
        return super.getLocalAddress();
    }

    /**
     * Set the datagram channel which the session will used.
     * 
     * @param channel
     *            datagram channel
     * @throws IllegalStateException
     */
    public void setChannel(DatagramChannel channel) {
        if (isStarted())
            throw new IllegalStateException(
                    "can't set datagram channel after session started");
        this.channel = channel;
    }

    /**
     * Get datagram channel associted with the session.
     * 
     * @return datagram channel
     */
    public DatagramChannel getChannel() {
        return channel;
    }

    protected ReactorHandler getReactorHandler() {
        return new ChannelReactorHandler() {

            public SelectableChannel[] getChannels() {
                return new SelectableChannel[] { channel };
            }

            protected void read() throws IOException {
                while (true) {
                    Buffer buffer = BufferFactory.allocate(getReadPacketSize());
                    ByteBuffer byteBuffer = buffer.asByteBuffer();

                    try {
                        SocketAddress address = channel.receive(byteBuffer);
                        if (address == null) {
                            buffer.release();
                            break;
                        }
                        buffer.limit(byteBuffer.position());
                        getSessionFilterChain(false).packetReceived(
                                new DefaultPacket(buffer, address));
                    } catch (IOException e) {
                        buffer.release();
                        throw e;
                    }
                }
            }

            private boolean isConnected() {
                if (channel == null)
                    return false;
                return channel.isConnected();
            }

            protected void checkSendPacket(Packet packet) {
                if (!isConnected() && packet.getAddress() == null)
                    throw new RuntimeException(
                            "can't send packet to unconnected datagram channel without socket address");
            }

            protected boolean write(Packet packet) throws IOException {
                Buffer buffer = packet.getContent();
                int writeCount = 0;
                if (packet.getAddress() == null) // connected
                    writeCount = buffer.write(channel);
                else {
                    writeCount = channel.send(buffer.asByteBuffer(), packet
                            .getAddress());
                    buffer.skip(writeCount);
                }
                return writeCount != 0;
            }

        };
    }

    protected void doStart() throws IOException {
        if (channel != null)
            return;
        try {
            channel = DatagramChannel.open();
            DatagramSocket socket = channel.socket();

            int recvBufferSize = Configuration.getRecvBufferSize();
            if (recvBufferSize > 0)
                socket.setReceiveBufferSize(recvBufferSize);

            int sendBufferSize = Configuration.getSendBufferSize();
            if (sendBufferSize > 0)
                socket.setSendBufferSize(sendBufferSize);

            socket.setReuseAddress(Configuration.isReuseSessionAddress());

            channel.socket().bind(getLocalAddress());
            if (getRemoteAddress() != null)
                channel.connect(getRemoteAddress());
        } catch (IOException e) {
            doClose();
            throw e;
        }
    }

    protected void doClose() {
        ChannelUtils.close(channel);
        channel = null;
    }
}
