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
package net.sf.cindy.example.chat;

import java.nio.ByteBuffer;

import net.sf.cindy.Buffer;
import net.sf.cindy.Packet;
import net.sf.cindy.PacketDecoder;
import net.sf.cindy.PacketEncoder;
import net.sf.cindy.Session;
import net.sf.cindy.SessionAcceptor;
import net.sf.cindy.SessionAcceptorHandler;
import net.sf.cindy.SessionType;
import net.sf.cindy.buffer.BufferFactory;
import net.sf.cindy.packet.DefaultPacket;
import net.sf.cindy.session.SessionFactory;
import net.sf.cindy.util.Charset;

/**
 * Chat server.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class ChatServer {

    private static final byte[] TOKEN = System.getProperty("line.separator")
            .getBytes();

    /**
     * Decode <code>Packet</code> to chat message.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    private static class ChatMessageDecoder implements PacketDecoder {

        public Object decode(Session session, Packet packet) throws Exception {
            Buffer buffer = packet.getContent();
            int index = buffer.indexOf(TOKEN);
            if (index >= 0) {
                String s = buffer.getString(Charset.SYSTEM, index
                        - buffer.position());
                buffer.skip(TOKEN.length);
                return s;
            }
            return null;
        }
    }

    /**
     * Encode chat message to <code>Packet</code>.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    private static class ChatMessageEncoder implements PacketEncoder {

        public Packet encode(Session session, Object obj) throws Exception {
            String s = (String) obj;
            ByteBuffer buffer = Charset.SYSTEM.encode(s);
            return new DefaultPacket(BufferFactory.allocate(
                    buffer.remaining() + TOKEN.length).put(buffer).put(TOKEN)
                    .flip());
        }
    }

    public static void main(String[] args) throws Exception {
        SessionAcceptor acceptor = SessionFactory
                .createSessionAcceptor(SessionType.TCP);
        acceptor.setAcceptorHandler(new SessionAcceptorHandler() {

            public void sessionAccepted(SessionAcceptor acceptor,
                    Session session) throws Exception {
                session.setPacketDecoder(new ChatMessageDecoder());
                session.setPacketEncoder(new ChatMessageEncoder());
                session.addSessionFilter(new ChatLogFilter());
                session.setSessionHandler(new ChatHandler());
                session.start();
            }

            public void exceptionCaught(SessionAcceptor acceptor,
                    Throwable cause) {
                System.err.println(cause);
            }
        });
        acceptor.start();
        if (acceptor.isStarted())
            System.out.println("ChatServer listen on "
                    + acceptor.getListenAddress());
    }
}
