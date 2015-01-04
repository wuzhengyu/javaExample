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
package net.sf.cindy.example.telnet;

import java.io.IOException;
import java.net.InetSocketAddress;

import net.sf.cindy.Buffer;
import net.sf.cindy.Packet;
import net.sf.cindy.PacketDecoder;
import net.sf.cindy.Session;
import net.sf.cindy.SessionHandlerAdapter;
import net.sf.cindy.SessionType;
import net.sf.cindy.buffer.BufferFactory;
import net.sf.cindy.encoder.BufferEncoder;
import net.sf.cindy.session.SessionFactory;
import net.sf.cindy.util.Charset;

/**
 * Simple telnet program, support TCP and UDP.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class Telnet {

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("usage: java Telnet (tcp|udp) host port");
            return;
        }
        boolean tcp = "tcp".equalsIgnoreCase(args[0]);
        String host = args[1];
        int port = Integer.parseInt(args[2]);

        Session session = startSession(tcp, host, port);

        byte[] b = new byte[4096];
        while (true) {
            int count = System.in.read(b);
            if (count < 0)
                break;
            // block send
            if (!session.send(BufferFactory.wrap(b, 0, count)).complete())
                break;
        }
    }

    private static Session startSession(boolean tcp, String host, int port) {
        System.out.println("start telnet using " + (tcp ? "tcp" : "udp"));
        Session session = SessionFactory.createSession(tcp ? SessionType.TCP
                : SessionType.UDP);
        session.setRemoteAddress(new InetSocketAddress(host, port));

        // set packet encoder and decoder
        session.setPacketEncoder(new BufferEncoder());
        session.setPacketDecoder(new PacketDecoder() {

            public Object decode(Session session, Packet packet)
                    throws Exception {
                Buffer content = packet.getContent();
                return content.getString(Charset.SYSTEM, content.remaining());
            }
        });

        // set session handler
        session.setSessionHandler(new SessionHandlerAdapter() {

            public void objectReceived(Session session, Object obj)
                    throws Exception {
                System.out.println(obj);
            };
        });

        // start session
        session.start().complete();
        return session;
    }
}
