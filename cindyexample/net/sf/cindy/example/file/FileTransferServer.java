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
package net.sf.cindy.example.file;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import net.sf.cindy.Buffer;
import net.sf.cindy.Packet;
import net.sf.cindy.PacketDecoder;
import net.sf.cindy.Session;
import net.sf.cindy.SessionAcceptor;
import net.sf.cindy.SessionAcceptorHandler;
import net.sf.cindy.SessionHandlerAdapter;
import net.sf.cindy.SessionType;
import net.sf.cindy.buffer.BufferFactory;
import net.sf.cindy.session.SessionFactory;
import net.sf.cindy.util.ChannelUtils;
import net.sf.cindy.util.Charset;

/**
 * File transfer server.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class FileTransferServer {

    /**
     * File transfer handler.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    private static class FileTransferHandler extends SessionHandlerAdapter {

        private FileChannel fc;
        private String fileName;

        public void objectReceived(Session session, Object obj)
                throws Exception {
            Buffer buffer = (Buffer) obj;
            try {
                if (fc == null) {
                    fileName = buffer.getString(Charset.UTF8, buffer
                            .remaining());
                    System.out.println("Receiving " + fileName + " from "
                            + session.getRemoteAddress());
                    fc = new RandomAccessFile(fileName, "rw").getChannel();
                } else {
                    while (buffer.hasRemaining())
                        buffer.write(fc);
                }
            } finally {
                buffer.release();
            }
        }

        public void sessionStarted(Session session) throws Exception {
            System.out.println(session.getRemoteAddress() + " connected");
        }

        public void sessionClosed(Session session) throws Exception {
            System.out.println("Received " + fileName);
            ChannelUtils.close(fc);
            fc = null;
        }

        public void exceptionCaught(Session session, Throwable cause) {
            cause.printStackTrace();
        }

    }

    /**
     * Decode <code>Packet</code> to file transfer buffer.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    private static class FileTransferMessageDecoder implements PacketDecoder {

        public Object decode(Session session, Packet packet) throws Exception {
            Buffer buffer = packet.getContent();
            if (buffer.remaining() >= 2) {
                int len = buffer.getUnsignedShort();
                if (buffer.remaining() >= len) {
                    Buffer content = BufferFactory.allocate(len);
                    buffer.get(content);
                    return content.flip();
                }
            }
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        SessionAcceptor acceptor = SessionFactory
                .createSessionAcceptor(SessionType.TCP);
        acceptor.setAcceptorHandler(new SessionAcceptorHandler() {

            public void exceptionCaught(SessionAcceptor acceptor,
                    Throwable cause) {
                cause.printStackTrace();
            }

            public void sessionAccepted(SessionAcceptor acceptor,
                    Session session) throws Exception {
                session.setPacketDecoder(new FileTransferMessageDecoder());
                session.setSessionHandler(new FileTransferHandler());
                session.start();
            }
        });
        acceptor.start();
        if (acceptor.isStarted())
            System.out.println("FileTransferServer listen on "
                    + acceptor.getListenAddress());
    }

}
