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
package net.sf.cindy.session.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.sf.cindy.Session;
import net.sf.cindy.SessionType;
import net.sf.cindy.session.AbstractSessionAcceptor;
import net.sf.cindy.session.nio.SocketChannelSession;
import net.sf.cindy.util.ChannelUtils;
import net.sf.cindy.util.LogThreadGroup;
import net.sf.cindy.util.NamedThreadFactory;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadFactory;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;

/**
 * Blocking session acceptor.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class BlockingSessionAcceptor extends AbstractSessionAcceptor {

    private static final ThreadFactory THREAD_FACTORY = new NamedThreadFactory(
            new LogThreadGroup("SessionAcceptor"), false, "SessionAcceptor");

    private final AtomicInteger counter = new AtomicInteger();

    private ServerSocketChannel channel;
    private Thread thread;

    public SessionType getSessionType() {
        return SessionType.TCP;
    }

    public boolean isStarted() {
        return thread != null;
    }

    public SocketAddress getListenAddress() {
        if (isStarted())
            return channel.socket().getLocalSocketAddress();
        return super.getListenAddress();
    }

    public int getListenPort() {
        if (isStarted())
            return channel.socket().getLocalPort();
        return super.getListenPort();
    }

    public ServerSocket getSocket() {
        return isStarted() ? channel.socket() : null;
    }

    public synchronized void start() {
        if (getAcceptorHandler() == null)
            throw new IllegalStateException("acceptor handler is null");
        if (isStarted())
            return;
        try {
            channel = ServerSocketChannel.open();
            setServerSocketOptions(channel.socket());
            counter.set(0);

            thread = THREAD_FACTORY.newThread(new Runnable() {

                public void run() {
                    while (true) {
                        try {
                            Session session = accept();
                            if (session == null)
                                break;
                            sessionAccepted(session);
                        } catch (IOException e) {
                            exceptionCaught(e);
                        }
                    }
                }
            });
            thread.start();
        } catch (Throwable e) {
            exceptionCaught(e);
            close();
        }
    }

    private Session accept() throws IOException {
        SocketChannel sc = null;
        try {
            sc = channel.accept();
        } catch (ClosedChannelException e) {
            return null;
        }
        counter.incrementAndGet();
        setSocketOptions(sc.socket());
        return newSession(sc);
    }

    public int getAcceptedCount() {
        return counter.get();
    }

    protected Session newSession(SocketChannel sc) {
        SocketChannelSession session = new SocketChannelSession();
        session.setChannel(sc);
        return session;
    }

    public synchronized void close() {
        ChannelUtils.close(channel);
        if (thread != null && thread != Thread.currentThread()) {
            while (thread.isAlive())
                try {
                    thread.join();
                } catch (InterruptedException e) {
                }
        }
        channel = null;
    }
}
