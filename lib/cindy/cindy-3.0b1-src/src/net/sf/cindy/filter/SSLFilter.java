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
package net.sf.cindy.filter;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

import net.sf.cindy.Buffer;
import net.sf.cindy.Packet;
import net.sf.cindy.Session;
import net.sf.cindy.SessionFilterAdapter;
import net.sf.cindy.SessionFilterChain;
import net.sf.cindy.buffer.BufferFactory;
import net.sf.cindy.packet.DefaultPacket;
import net.sf.cindy.session.SessionException;
import edu.emory.mathcs.backport.java.util.LinkedList;
import edu.emory.mathcs.backport.java.util.Queue;

/**
 * SSL/TLS filter, require java 5.0.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class SSLFilter extends SessionFilterAdapter {

    private final SSLContext sslc;

    /**
     * Hold send packet if handshake is not completed.
     */
    private final Queue tempQueue = new LinkedList();

    private static class QueuedPacket {

        private final SessionFilterChain chain;
        private final Packet packet;

        public QueuedPacket(SessionFilterChain chain, Packet packet) {
            this.chain = chain;
            this.packet = packet;
        }

    }

    private boolean clientMode;
    private boolean needClientAuth;

    private SSLEngine engine;
    private boolean handshakeCompleted = false;
    private int appBufferSize;
    private int netBufferSize;

    public SSLFilter(SSLContext context) {
        this.sslc = context;
    }

    public SSLContext getSSLContext() {
        return sslc;
    }

    public boolean isClientMode() {
        return clientMode;
    }

    public void setClientMode(boolean clientMode) {
        this.clientMode = clientMode;
    }

    public boolean isNeedClientAuth() {
        return needClientAuth;
    }

    public void setNeedClientAuth(boolean needClientAuth) {
        this.needClientAuth = needClientAuth;
    }

    /**
     * Handshake packet.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    private static class HandshakePacket extends DefaultPacket {

        public HandshakePacket() {
            super(BufferFactory.allocate(0));
        }

    }

    public void sessionStarted(SessionFilterChain filterChain) throws Exception {
        // init engine
        Session session = filterChain.getSession();
        SocketAddress address = session.getRemoteAddress();
        if (clientMode && (address instanceof InetSocketAddress)) {
            InetSocketAddress netAddress = (InetSocketAddress) address;
            engine = sslc.createSSLEngine(netAddress.getAddress()
                    .getHostAddress(), netAddress.getPort());
        } else {
            engine = sslc.createSSLEngine();
        }
        engine.setUseClientMode(clientMode);
        engine.setNeedClientAuth(needClientAuth);
        initSSLEngine(engine);

        appBufferSize = engine.getSession().getApplicationBufferSize();
        netBufferSize = engine.getSession().getPacketBufferSize();

        // handshake
        engine.beginHandshake();

        super.sessionStarted(filterChain);
        if (clientMode)
            session.flush(new HandshakePacket());
    }

    public void sessionClosed(SessionFilterChain filterChain) throws Exception {
        try {
            if (engine != null) {
                engine.closeOutbound();
                if (!engine.isInboundDone()) {
                    Packet encodedPacket = encode(filterChain.getSession(),
                            new HandshakePacket());
                    if (encodedPacket != null)
                        encodedPacket.getContent().release();
                }
                engine.closeInbound();
            }
        } catch (SSLException ssle) {
            throw new SessionException(ssle);
        } finally {
            handshakeCompleted = false;
            engine = null;
            QueuedPacket packet = null;
            while ((packet = (QueuedPacket) tempQueue.poll()) != null) {
                packet.chain.packetSend(packet.packet);
            }
            super.sessionClosed(filterChain);
        }
    }

    /**
     * Templet method.
     */
    protected void initSSLEngine(SSLEngine engine) {
    }

    private synchronized void handshakeCompleted() throws Exception {
        handshakeCompleted = true;
        QueuedPacket packet = null;
        while ((packet = (QueuedPacket) tempQueue.poll()) != null) {
            this.packetSend(packet.chain, packet.packet);
        }
    }

    private void runTask() {
        Runnable runnable = null;
        while ((runnable = engine.getDelegatedTask()) != null)
            runnable.run();
    }

    private Packet decode(Session session, Packet packet) throws Exception {
        Buffer src = packet.getContent();
        ByteBuffer srcBuffer = src.asByteBuffer();

        int size = appBufferSize
                * (int) Math.ceil((double) (src.remaining() + 1)
                        / netBufferSize);
        Buffer dest = BufferFactory.allocate(size);
        ByteBuffer destBuffer = dest.asByteBuffer();

        boolean hasAppData = false;

        // decode
        while (true) {
            SSLEngineResult result = engine.unwrap(srcBuffer, destBuffer);
            Status status = result.getStatus();
            if (status == Status.OK) {
                HandshakeStatus handshakeStatus = result.getHandshakeStatus();
                if (handshakeStatus == HandshakeStatus.NOT_HANDSHAKING) {
                    hasAppData = true;
                    if (!srcBuffer.hasRemaining()) // decode completed
                        break;
                } else if (handshakeStatus == HandshakeStatus.NEED_TASK) {
                    runTask();
                } else if (handshakeStatus == HandshakeStatus.NEED_UNWRAP) {
                    // unwrap again
                } else if (handshakeStatus == HandshakeStatus.NEED_WRAP) {
                    // need send some handshake packet
                    session.flush(new HandshakePacket());
                    break;
                } else if (handshakeStatus == HandshakeStatus.FINISHED) {
                    handshakeCompleted();
                }
            } else if (status == Status.BUFFER_UNDERFLOW) {
                // need more content, wait next time
                break;
            } else if (status == Status.BUFFER_OVERFLOW) {
                // increase dest capacity
                destBuffer.flip();
                Buffer newDest = BufferFactory.allocate(
                        appBufferSize + destBuffer.remaining()).put(destBuffer);
                dest.release();
                dest = newDest;
                destBuffer = dest.asByteBuffer();
            } else if (status == Status.CLOSED) {
                // send close message if possible
                if (result.getHandshakeStatus() == HandshakeStatus.NEED_WRAP)
                    session.flush(new HandshakePacket());
                break;
            }
        }

        src.release();
        if (hasAppData) {
            dest.position(0).limit(destBuffer.position());
            return new DefaultPacket(dest, packet.getAddress());
        }
        return null;
    }

    public void packetReceived(SessionFilterChain filterChain, Packet packet)
            throws Exception {
        Packet decodedPacket = decode(filterChain.getSession(), packet);
        if (decodedPacket != null)
            super.packetReceived(filterChain, decodedPacket);
    }

    private Packet encode(Session session, Packet packet) throws Exception {
        Buffer src = packet.getContent();
        ByteBuffer srcBuffer = src.asByteBuffer();

        int size = netBufferSize
                * (int) Math.ceil((double) (src.remaining() + 1)
                        / appBufferSize);
        Buffer dest = BufferFactory.allocate(size);
        ByteBuffer destBuffer = dest.asByteBuffer();

        // encode
        while (true) {
            SSLEngineResult result = engine.wrap(srcBuffer, destBuffer);
            Status status = result.getStatus();
            if (status == Status.OK) {
                HandshakeStatus handshakeStatus = result.getHandshakeStatus();
                if (handshakeStatus == HandshakeStatus.NOT_HANDSHAKING) {
                    if (!srcBuffer.hasRemaining()) // encode completed
                        break;
                } else if (handshakeStatus == HandshakeStatus.NEED_UNWRAP) {
                    break; // wait receive
                } else if (handshakeStatus == HandshakeStatus.NEED_WRAP) {
                    // wrap again
                } else if (handshakeStatus == HandshakeStatus.NEED_TASK) {
                    runTask();
                } else if (handshakeStatus == HandshakeStatus.FINISHED) {
                    handshakeCompleted();
                    break;
                }
            } else if (status == Status.BUFFER_OVERFLOW) {
                // increase dest capacity
                destBuffer.flip();
                Buffer newDest = BufferFactory.allocate(
                        netBufferSize + destBuffer.remaining()).put(destBuffer);
                dest.release();
                dest = newDest;
                destBuffer = dest.asByteBuffer();
            } else if (status == Status.CLOSED) {
                break;
            } else if (status == Status.BUFFER_UNDERFLOW) {
                // should never happen
                src.release();
                session.close();
                return null;
            }
        }

        src.release();
        dest.position(0).limit(destBuffer.position());
        return new DefaultPacket(dest, packet.getAddress());
    }

    public void packetSend(SessionFilterChain filterChain, Packet packet)
            throws Exception {
        boolean queued = !handshakeCompleted
                && !(packet instanceof HandshakePacket);
        if (queued) {
            tempQueue.offer(new QueuedPacket(filterChain, packet));
            return;
        }

        Packet encodedPacket = encode(filterChain.getSession(), packet);
        if (encodedPacket != null)
            super.packetSend(filterChain, encodedPacket);
    }
}
