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

import java.nio.ByteBuffer;
import java.util.Date;

import net.sf.cindy.Future;
import net.sf.cindy.FutureListener;
import net.sf.cindy.Session;
import net.sf.cindy.SessionHandlerAdapter;
import net.sf.cindy.packet.DefaultPacket;
import net.sf.cindy.util.Charset;

/**
 * Daytime, RFC 867.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class DaytimeHandler extends SessionHandlerAdapter {

    public void sessionStarted(Session session) throws Exception {
        ByteBuffer buffer = Charset.UTF8.encode(new Date().toString());
        Future future = session.flush(new DefaultPacket(buffer));
        future.addListener(new FutureListener() {

            public void futureCompleted(Future future) throws Exception {
                future.getSession().close();
            }
        });
    }
}
