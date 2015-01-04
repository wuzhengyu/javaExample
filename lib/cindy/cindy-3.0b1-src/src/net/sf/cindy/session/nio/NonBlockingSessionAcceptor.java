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
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.sf.cindy.Session;
import net.sf.cindy.SessionHandlerAdapter;
import net.sf.cindy.SessionType;
import net.sf.cindy.session.AbstractSessionAcceptor;
import net.sf.cindy.util.ChannelUtils;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;

/**
 * Non-blocking session acceptor.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class NonBlockingSessionAcceptor extends AbstractSessionAcceptor {

    private final AtomicInteger counter = new AtomicInteger();

    private final ServerSocketChannelSession session = new ServerSocketChannelSession() {

        protected void buildSession(SocketChannel sc) {
            counter.incrementAndGet();
            try {
                setSocketOptions(sc.socket());
                SocketChannelSession session = new SocketChannelSession();
                session.setChannel(sc);
                sessionAccepted(session);
            } catch (Throwable e) {
                exceptionCaught(e);
            }
        }
    };

    public NonBlockingSessionAcceptor() {
        session.setSessionHandler(new SessionHandlerAdapter() {

            public void exceptionCaught(Session session, Throwable cause) {
                NonBlockingSessionAcceptor.this.exceptionCaught(cause);
            }
        });
    }

    public SessionType getSessionType() {
        return SessionType.TCP;
    }

    public boolean isStarted() {
        return session.isStarted();
    }

    public SocketAddress getListenAddress() {
        if (isStarted())
            return session.getSocket().getLocalSocketAddress();
        return super.getListenAddress();
    }

    public int getListenPort() {
        if (isStarted())
            return session.getSocket().getLocalPort();
        return super.getListenPort();
    }

    public synchronized void start() {
        if (getAcceptorHandler() == null)
            throw new IllegalStateException("acceptor handler is null");
        if (isStarted())
            return;

        ServerSocketChannel channel = null;
        try {
            channel = ServerSocketChannel.open();
            setServerSocketOptions(channel.socket());
            counter.set(0);
            session.setChannel(channel);
            session.start().complete();
        } catch (IOException e) {
            ChannelUtils.close(channel);
            exceptionCaught(e);
        }
    }

    public int getAcceptedCount() {
        return counter.get();
    }

    public synchronized void close() {
        session.close().complete();
    }
}
