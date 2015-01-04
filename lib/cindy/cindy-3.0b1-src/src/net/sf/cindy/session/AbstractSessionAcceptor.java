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
package net.sf.cindy.session;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import net.sf.cindy.Session;
import net.sf.cindy.SessionAcceptor;
import net.sf.cindy.SessionAcceptorHandler;
import net.sf.cindy.util.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract session acceptor.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public abstract class AbstractSessionAcceptor implements SessionAcceptor {

    private static final Log log = LogFactory
            .getLog(AbstractSessionAcceptor.class);

    private SocketAddress address = new InetSocketAddress(0);
    private int port = 0;
    private int backlog = Configuration.getAcceptorBacklog();
    private boolean reuseAddr = Configuration.isReuseAcceptorAddress();
    private SessionAcceptorHandler handler = null;

    public void setListenAddress(SocketAddress address) {
        if (address != null) {
            if (isStarted())
                throw new IllegalStateException(
                        "can't set listen address after acceptor started");
            this.address = address;
        }
    }

    public SocketAddress getListenAddress() {
        return address;
    }

    public void setListenPort(int port) {
        if (isStarted())
            throw new IllegalStateException(
                    "can't set listen port after acceptor started");
        this.port = port;
        this.address = new InetSocketAddress(port);
    }

    public int getListenPort() {
        return port;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        if (isStarted())
            throw new IllegalStateException(
                    "can't set backlog after acceptor started");
        this.backlog = backlog;
    }

    public boolean isReuseAddress() {
        return reuseAddr;
    }

    public void setReuseAddress(boolean b) {
        if (isStarted())
            throw new IllegalStateException(
                    "can't set reuse address after acceptor started");
        this.reuseAddr = b;
    }

    public SessionAcceptorHandler getAcceptorHandler() {
        return handler;
    }

    public void setAcceptorHandler(SessionAcceptorHandler handler) {
        if (handler != null)
            this.handler = handler;
    }

    protected void setSocketOptions(Socket socket) throws IOException {
        int recvBufferSize = Configuration.getRecvBufferSize();
        if (recvBufferSize > 0)
            socket.setReceiveBufferSize(recvBufferSize);

        int sendBufferSize = Configuration.getSendBufferSize();
        if (sendBufferSize > 0)
            socket.setSendBufferSize(sendBufferSize);

        socket.setReuseAddress(Configuration.isReuseSessionAddress());
        socket.setTcpNoDelay(Configuration.isTcpNoDelay());
    }

    protected void setServerSocketOptions(ServerSocket socket)
            throws IOException {
        socket.setReuseAddress(isReuseAddress());

        int recvBufferSize = Configuration.getRecvBufferSize();
        if (recvBufferSize > 0)
            socket.setReceiveBufferSize(recvBufferSize);

        socket.bind(getListenAddress(), getBacklog());
    }

    protected final void exceptionCaught(Throwable e) {
        try {
            handler.exceptionCaught(this, e);
        } catch (Throwable throwable) { // protect catch
            log.error(throwable, throwable);
        }
    }

    protected final void sessionAccepted(Session session) {
        try {
            handler.sessionAccepted(this, session);
        } catch (Throwable e) {
            exceptionCaught(e);
        }
    }
}
