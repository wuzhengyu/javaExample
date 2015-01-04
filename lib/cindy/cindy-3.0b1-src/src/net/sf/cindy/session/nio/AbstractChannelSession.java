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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;

import net.sf.cindy.Buffer;
import net.sf.cindy.Future;
import net.sf.cindy.Packet;
import net.sf.cindy.Session;
import net.sf.cindy.SessionFilterAdapter;
import net.sf.cindy.SessionFilterChain;
import net.sf.cindy.packet.PriorityPacket;
import net.sf.cindy.session.AbstractSession;
import net.sf.cindy.session.DefaultFuture;
import net.sf.cindy.session.SessionException;
import net.sf.cindy.session.dispatcher.DispatcherFactory;
import net.sf.cindy.session.nio.reactor.Reactor;
import net.sf.cindy.session.nio.reactor.ReactorFactory;
import net.sf.cindy.session.nio.reactor.ReactorHandler;
import net.sf.cindy.util.ChannelUtils;
import edu.emory.mathcs.backport.java.util.PriorityQueue;
import edu.emory.mathcs.backport.java.util.Queue;

/**
 * Abstract selectable channel session.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public abstract class AbstractChannelSession extends AbstractSession {

    private final Reactor reactor = ReactorFactory.getReactor();
    private final ReactorHandler handler = getReactorHandler();

    /**
     * Get reactor handler.
     * 
     * @return reactor handler
     */
    protected abstract ReactorHandler getReactorHandler();

    protected final Reactor getReactor() {
        return reactor;
    }

    /**
     * Comparable packet.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    private static class FuturePacket extends PriorityPacket {

        private final DefaultFuture future;
        private final Object obj;

        private final int position;
        private final int limit;

        public FuturePacket(Object obj, Packet packet, int priority,
                DefaultFuture future) {
            super(packet, priority);
            this.obj = obj;
            this.future = future;

            Buffer content = packet.getContent();
            position = content.position();
            limit = content.limit();
        }

    }

    private final Queue sendQueue = new PriorityQueue();
    private volatile FuturePacket currentSendPacket = null;

    private volatile boolean started = false;
    private DefaultFuture startFuture, closeFuture;

    protected Future send(final Object obj, Packet packet, final int priority) {
        if (packet == null || packet.getContent() == null || !isStarted())
            return new DefaultFuture(this, false);
        final DefaultFuture future = new DefaultFuture(this);
        getSessionFilterChain(new SessionFilterAdapter() {

            public void packetSend(SessionFilterChain filterChain, Packet packet)
                    throws Exception {
                if (packet == null || packet.getContent() == null) {
                    future.setSucceeded(false);
                    return;
                }
                Packet futurePacket = new FuturePacket(obj, packet, priority,
                        future);
                boolean firstSend = false;
                boolean sendFailed = false;

                synchronized (sendQueue) {
                    firstSend = currentSendPacket == null
                            && sendQueue.isEmpty();
                    // current session may be closed
                    if (!isStarted() || !sendQueue.offer(futurePacket))
                        sendFailed = true;
                }

                if (sendFailed)
                    future.setSucceeded(false);
                else if (firstSend)
                    reactor.interest(handler, Reactor.OP_WRITE);
            }

        }, true).packetSend(packet);
        return future;
    }

    public boolean isStarted() {
        return started;
    }

    public synchronized Future close() throws IllegalStateException {
        boolean starting = startFuture != null && !startFuture.isCompleted();
        if (closeFuture == null) {
            if (!started && !starting) {
                closeFuture = new DefaultFuture(this, true);
                doClose(); // clear resource even not start
            } else {
                closeFuture = new DefaultFuture(this);
                reactor.deregister(handler);
            }
        }
        return closeFuture;
    }

    protected void doClose() {
    }

    public synchronized Future start() {
        if (closeFuture != null && !closeFuture.isCompleted())
            return new DefaultFuture(this, false);
        closeFuture = null; // then call close will close

        if (startFuture == null) {
            try {
                doStart();
            } catch (IOException e) {
                dispatchException(e);
                return new DefaultFuture(this, false);
            }
            startFuture = new DefaultFuture(this);
            reactor.register(handler);
        }
        return startFuture;
    }

    protected void doStart() throws IOException {
    }

    /**
     * Translate reactor events to session events.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    protected abstract class ChannelReactorHandler implements ReactorHandler {

        public Session getSession() {
            return AbstractChannelSession.this;
        }

        public void onTimeout() {
            getSessionFilterChain(false).sessionTimeout();
        }

        private void clearSendQueue() {
            synchronized (sendQueue) {
                if (currentSendPacket != null) {
                    currentSendPacket.future.setSucceeded(false);
                    currentSendPacket = null;
                }
                for (FuturePacket packet = null; (packet = (FuturePacket) sendQueue
                        .poll()) != null;) {
                    packet.future.setSucceeded(false);
                }
            }
        }

        private void dispatch(Runnable runnable) {
            DispatcherFactory.getDispatcher().dispatch(
                    AbstractChannelSession.this, runnable);
        }

        public void onRegistered() {
            clearSendQueue(); // protect code

            started = true;
            // keep dispatch order
            dispatch(new Runnable() {

                public void run() {
                    startFuture.setSucceeded(true);
                    getSessionFilterChain(false).sessionStarted();
                }
            });
        }

        public void onDeregistered() {
            SelectableChannel[] channels = getChannels();
            for (int i = 0; i < channels.length; i++) {
                ChannelUtils.close(channels[i]);
            }
            started = false;
            clearSendQueue();
            if (startFuture != null && !startFuture.isCompleted())
                startFuture.setSucceeded(false);
            startFuture = null;
            doClose();

            // keep dispatch order
            dispatch(new Runnable() {

                public void run() {
                    if (closeFuture != null && !closeFuture.isCompleted())
                        closeFuture.setSucceeded(true);
                    getSessionFilterChain(false).sessionClosed();
                }
            });
        }

        public void onAcceptable() {
        }

        public void onConnectable() {
        }

        public void onReadable() {
            try {
                read();
                reactor.interest(handler, Reactor.OP_READ);
            } catch (ClosedChannelException cce) {
                close();
            } catch (Throwable e) {
                dispatchException(new SessionException(e));
                close();
            }
        }

        /**
         * Read packet from channel.
         * 
         * @throws IOException
         *             session will be closed
         */
        protected void read() throws IOException {
        }

        public void onWritable() {
            try {
                while (true) {
                    synchronized (sendQueue) {
                        if (currentSendPacket == null)
                            currentSendPacket = (FuturePacket) sendQueue.poll();
                    }
                    if (currentSendPacket == null) {
                        reactor.interest(handler, Reactor.OP_NON_WRITE);
                        return;
                    }

                    try {
                        checkSendPacket(currentSendPacket);
                    } catch (RuntimeException e) {
                        dispatchException(e);
                        DefaultFuture future = currentSendPacket.future;
                        currentSendPacket = null;
                        future.setSucceeded(false);
                        continue;
                    }

                    Buffer buffer = currentSendPacket.getContent();
                    if (!buffer.hasRemaining() || write(currentSendPacket)) {
                        buffer.limit(currentSendPacket.limit);
                        buffer.position(currentSendPacket.position);
                        buffer.release();
                        final FuturePacket packet = currentSendPacket;
                        currentSendPacket = null;

                        // keep dispatch order
                        dispatch(new Runnable() {

                            public void run() {
                                packet.future.setSucceeded(true);
                                getSessionFilterChain(true).packetSent(
                                        packet.getDelegate());
                                if (packet.obj != null)
                                    getSessionFilterChain(true).objectSent(
                                            packet.obj);
                            }
                        });
                    } else {
                        reactor.interest(handler, Reactor.OP_WRITE);
                        return;
                    }
                }
            } catch (ClosedChannelException cce) {
                close();
            } catch (Throwable e) {
                dispatchException(new SessionException(e));
                close();
            }
        }

        /**
         * Check send packet.
         * 
         * @param packet
         *            send packet
         */
        protected void checkSendPacket(Packet packet) {
        }

        /**
         * Write packet to channel.
         * 
         * @param packet
         *            send packet
         * @return if write succeeded, return true. if kennel buffer is full,
         *         return false.
         * @throws IOException
         *             session will be closed
         */
        protected boolean write(Packet packet) throws IOException {
            return false;
        }
    }

}
