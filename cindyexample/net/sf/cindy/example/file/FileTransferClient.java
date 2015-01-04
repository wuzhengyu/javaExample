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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import net.sf.cindy.Buffer;
import net.sf.cindy.Future;
import net.sf.cindy.FutureListener;
import net.sf.cindy.Session;
import net.sf.cindy.SessionHandlerAdapter;
import net.sf.cindy.SessionType;
import net.sf.cindy.buffer.BufferFactory;
import net.sf.cindy.encoder.BufferEncoder;
import net.sf.cindy.session.SessionFactory;
import net.sf.cindy.util.ChannelUtils;
import net.sf.cindy.util.Charset;
import net.sf.cindy.util.ElapsedTime;

/**
 * File transfer client.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class FileTransferClient {

    private static final int QUEUE_SIZE = 30;
    private static final int MESSAGE_SIZE = 65535;

    /**
     * File transfer handler.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    private static class FileTransferHandler extends SessionHandlerAdapter {

        private final File file;

        private FileChannel fc;
        private ElapsedTime elapsedTime;

        public FileTransferHandler(File file) {
            this.file = file;
        }

        public void sessionStarted(Session session) throws Exception {
            elapsedTime = new ElapsedTime();
            if (fc == null)
                fc = new RandomAccessFile(file, "r").getChannel();

            sendName(session);
            sendContent(session);
        }

        private void sendName(Session session) {
            ByteBuffer name = Charset.UTF8.encode(file.getName());
            Buffer content = BufferFactory.allocate(name.remaining() + 2)
                    .putUnsignedShort(name.remaining()).put(name).flip();
            session.send(content);
        }

        private int sentCount;

        public void objectSent(Session session, Object obj) throws Exception {
            // the first send object is file name
            if (++sentCount % QUEUE_SIZE == 1)
                sendContent(session);
        }

        private void sendContent(Session session) throws IOException {
            for (int i = 1; i <= QUEUE_SIZE; i++) {
                Buffer buffer = BufferFactory.allocate(MESSAGE_SIZE);
                buffer.position(2);
                int readCount = buffer.read(fc);

                if (readCount == -1) { // end of file
                    buffer.release();
                    session.send(BufferFactory.allocate(0)).addListener(
                            new FutureListener() {

                                public void futureCompleted(Future future)
                                        throws Exception {
                                    future.getSession().close();
                                }
                            });
                    break;
                } else {
                    buffer.putUnsignedShort(0, readCount).flip();
                    session.send(buffer);
                }
            }
        }

        public void sessionClosed(Session session) throws Exception {
            if (elapsedTime != null)
                System.out.println("elapsed time: "
                        + elapsedTime.getElapsedTime() + " ms");
            elapsedTime = null;
            ChannelUtils.close(fc);
            fc = null;
        }

        public void exceptionCaught(Session session, Throwable cause) {
            cause.printStackTrace();
        }

    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("usage: java FileTransferClient file host port");
            return;
        }

        File file = new File(args[0]);
        if (!file.exists()) {
            System.err.println(file + " not found");
            return;
        }
        SocketAddress address = new InetSocketAddress(args[1], Integer
                .parseInt(args[2]));
        System.out.println("start transfer " + file + " to " + address);

        Session session = SessionFactory.createSession(SessionType.TCP);
        session.setRemoteAddress(address);
        session.setPacketEncoder(new BufferEncoder());
        session.setSessionHandler(new FileTransferHandler(file));
        session.start();
    }
}
