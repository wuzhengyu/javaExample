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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.sf.cindy.Future;
import net.sf.cindy.Packet;
import net.sf.cindy.SessionType;
import net.sf.cindy.session.nio.reactor.Reactor;
import net.sf.cindy.session.nio.reactor.ReactorHandler;
import net.sf.cindy.util.ChannelUtils;
import net.sf.cindy.util.Configuration;

/**
 * Server socket channel session. Application can override buildSession method
 * to build custom session.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class ServerSocketChannelSession extends AbstractChannelSession {

    private ServerSocketChannel channel;

    public SessionType getSessionType() {
        return SessionType.UNKNOWN;
    }

    public SocketAddress getRemoteAddress() {
        return null;
    }

    public SocketAddress getLocalAddress() {
        if (isStarted())
            return channel.socket().getLocalSocketAddress();
        return super.getLocalAddress();
    }

    /**
     * Set the port the server socket session listen to.
     * 
     * @param port
     *            listen port
     * @throws IllegalStateException
     */
    public void setLocalPort(int port) {
        if (isStarted())
            throw new IllegalStateException(
                    "can't set local port after session started");
        setLocalAddress(new InetSocketAddress(port));
    }

    /**
     * Set the server socket channel which the session will used.
     * 
     * @param ssc
     *            server socket channel
     * @throws IllegalStateException
     */
    public void setChannel(ServerSocketChannel ssc) {
        if (isStarted())
            throw new IllegalStateException(
                    "can't set server socket channel after session started");
        this.channel = ssc;
    }

    /**
     * Get the server socket channel associted with the session.
     * 
     * @return server socket channel
     */
    public ServerSocketChannel getChannel() {
        return channel;
    }

    /**
     * Get the server socket associted with the session.
     * 
     * @return server socket
     */
    public ServerSocket getSocket() {
        ServerSocketChannel ssc = channel;
        if (ssc == null)
            return null;
        return ssc.socket();
    }

    public Future send(Packet packet, int priority) {
        throw new IllegalStateException(
                "can't send packet to server socket session");
    }

    protected ReactorHandler getReactorHandler() {
        return new ChannelReactorHandler() {

            public SelectableChannel[] getChannels() {
                return new SelectableChannel[] { channel };
            }

            public void onAcceptable() {
                SocketChannel sc = null;
                try {
                    while ((sc = channel.accept()) != null) {
                        buildSession(sc);
                    }
                    // accept next
                    getReactor().interest(this, Reactor.OP_ACCEPT);
                } catch (IOException e) {
                    ChannelUtils.close(sc);
                    dispatchException(e);
                    close();
                }
            }

        };
    }

    /**
     * Build accepted session. Application can choose to start a session or
     * close the channel. The default action is close the socket channel.
     * 
     * @param sc
     *            the socket channel that server socket session accepted
     */
    protected void buildSession(SocketChannel sc) {
        ChannelUtils.close(sc);
    }

    protected void doStart() throws IOException {
        if (channel != null)
            return;
        try {
            channel = ServerSocketChannel.open();
            SocketAddress localAddr = getLocalAddress();

            ServerSocket socket = channel.socket();
            socket.setReuseAddress(Configuration.isReuseAcceptorAddress());
            int recvBufferSize = Configuration.getRecvBufferSize();
            if (recvBufferSize > 0)
                socket.setReceiveBufferSize(recvBufferSize);
            socket.bind(localAddr == null ? new InetSocketAddress(0)
                    : localAddr, Configuration.getAcceptorBacklog());
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
