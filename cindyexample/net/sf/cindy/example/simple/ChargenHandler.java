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
package net.sf.cindy.example.simple;

import net.sf.cindy.Future;
import net.sf.cindy.FutureListener;
import net.sf.cindy.Session;
import net.sf.cindy.SessionHandlerAdapter;
import net.sf.cindy.packet.DefaultPacket;

/**
 * Chargen, RFC 864.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class ChargenHandler extends SessionHandlerAdapter {

    // ASCII printing characters
    private static byte[] DATA = new byte[94];

    static {
        for (int i = 0; i < DATA.length; i++) {
            DATA[i] = (byte) (33 + i);
        }
    }

    public void sessionStarted(Session session) throws Exception {
        send(session);
    }

    private void send(Session session) {
        session.flush(new DefaultPacket(DATA)).addListener(
                new FutureListener() {

                    public void futureCompleted(Future future) throws Exception {
                        if (future.isSucceeded())
                            send(future.getSession());
                    }
                });
    }

}
