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

import java.net.SocketAddress;

import net.sf.cindy.Buffer;
import net.sf.cindy.Packet;
import net.sf.cindy.Session;
import net.sf.cindy.SessionFilterAdapter;
import net.sf.cindy.SessionFilterChain;
import net.sf.cindy.SessionType;
import net.sf.cindy.buffer.BufferFactory;
import net.sf.cindy.buffer.LinkedBuffer;
import net.sf.cindy.packet.DefaultPacket;
import net.sf.cindy.util.Configuration;

/**
 * Decode <code>Packet</code> to object. Inner filter before
 * <code>SessionHandlerFilter</code>.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class PacketDecoderFilter extends SessionFilterAdapter {

    private static final boolean USE_LINKED_BUFFER = Configuration
            .isUseLinkedBuffer();

    public static PacketDecoderFilter getInstance(Session session) {
        if (session.getSessionType() == SessionType.UDP)
            return new DiscardPacketDecoderFilter(session);
        if (USE_LINKED_BUFFER)
            return new LinkPacketDecoderFilter(session);
        return new CopyPacketDecoderFilter(session);
    }

    protected final Session session;

    protected PacketDecoderFilter(Session session) {
        this.session = session;
    }

    protected void recognize(Buffer content, SocketAddress address)
            throws Exception {
        while (content.hasRemaining()) {
            Buffer slice = content.asReadOnlyBuffer().slice();
            Object obj = session.getPacketDecoder().decode(session,
                    new DefaultPacket(slice, address));
            if (obj == null)
                break;
            content.skip(slice.position());
            session.getSessionFilterChain(false).objectReceived(obj);
        }
    }

    /**
     * Datagram session should discard previous received packet.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    private static class DiscardPacketDecoderFilter extends PacketDecoderFilter {

        public DiscardPacketDecoderFilter(Session session) {
            super(session);
        }

        public void packetReceived(SessionFilterChain filterChain, Packet packet)
                throws Exception {
            if (packet == null || filterChain.getSession() != session)
                super.packetReceived(filterChain, packet);
            else {
                Buffer content = packet.getContent();
                if (content != null)
                    try {
                        recognize(content, packet.getAddress());
                    } finally {
                        content.release();
                    }
            }
        }

    }

    /**
     * Copy received content as a single content.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    private static class CopyPacketDecoderFilter extends PacketDecoderFilter {

        private Buffer content;
        private SocketAddress address;

        public CopyPacketDecoderFilter(Session session) {
            super(session);
        }

        public void sessionClosed(SessionFilterChain filterChain)
                throws Exception {
            if (filterChain.getSession() == session) {
                synchronized (this) {
                    if (content != null) {
                        content.release();
                        content = null;
                    }
                    address = null;
                }
            }
            super.sessionClosed(filterChain);
        }

        public void packetReceived(SessionFilterChain filterChain, Packet packet)
                throws Exception {
            if (packet == null || filterChain.getSession() != session) {
                super.packetReceived(filterChain, packet);
                return;
            }

            synchronized (this) {
                if (content == null) {
                    content = packet.getContent();
                    address = packet.getAddress();
                } else {
                    Buffer recvContent = packet.getContent();
                    if (content.remaining() < recvContent.remaining()) {
                        Buffer newContent = BufferFactory.allocate(
                                content.position() + recvContent.remaining())
                                .put(content.flip());
                        content.release();
                        content = newContent;
                    }
                    content.put(recvContent);
                    content.flip();
                }

                if (content != null)
                    try {
                        recognize(content, address);
                    } finally {
                        if (content.hasRemaining())
                            content.compact();
                        else {
                            content.release();
                            content = null;
                            address = null;
                        }
                    }
            }
        }
    }

    /**
     * Link received contents as a single content. Experimental.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    private static class LinkPacketDecoderFilter extends PacketDecoderFilter {

        private LinkedPacketDecoderBuffer content;
        private SocketAddress address;

        public LinkPacketDecoderFilter(Session session) {
            super(session);
        }

        public void sessionClosed(SessionFilterChain filterChain)
                throws Exception {
            if (filterChain.getSession() == session) {
                synchronized (this) {
                    if (content != null) {
                        content.release();
                        content = null;
                    }
                    address = null;
                }
            }
            super.sessionClosed(filterChain);
        }

        public void packetReceived(SessionFilterChain filterChain, Packet packet)
                throws Exception {
            if (packet == null || filterChain.getSession() != session) {
                super.packetReceived(filterChain, packet);
                return;
            }

            synchronized (this) {
                if (content == null) {
                    content = new LinkedPacketDecoderBuffer();
                    address = packet.getAddress();
                }
                content.append(packet.getContent());
                try {
                    recognize(content, address);
                } finally {
                    content.releaseNoUseBuffer();
                }
            }

        }

        /**
         * Link several buffers as a single buffer.
         * 
         * @author <a href="chenrui@gmail.com">Roger Chen</a>
         * @version $id$
         */
        private static class LinkedPacketDecoderBuffer extends LinkedBuffer {

            private static final Buffer[] EMPTY_BUFFER = new Buffer[0];

            public LinkedPacketDecoderBuffer() {
                super(EMPTY_BUFFER);
            }

            protected void append(Buffer buffer) {
                super.append(buffer);
                limit(capacity());
            }

            private void releaseNoUseBuffer() {
                int position = position();
                int releasedLen = 0;

                for (Entry e = header.next; e != header;) {
                    int length = e.buffer.remaining();
                    if (e.position + length <= position) {
                        Entry next = e.next;
                        e.previous.next = next;
                        next.previous = e.previous;

                        releasedLen += length;
                        e.buffer.release();
                        e.buffer = null;
                        e.next = null;
                        e.previous = null;
                        e = next;
                    } else
                        e = e.next;
                }

                if (releasedLen > 0) {
                    for (Entry e = header.next; e != header; e = e.next) {
                        e.position -= releasedLen;
                    }
                    position(position() - releasedLen);
                    limit(limit() - releasedLen);
                    capacity(capacity() - releasedLen);
                }
            }
        }
    }

}
