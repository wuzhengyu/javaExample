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
package net.sf.cindy.example.http;

import java.nio.ByteBuffer;

import net.sf.cindy.Buffer;
import net.sf.cindy.Packet;
import net.sf.cindy.PacketEncoder;
import net.sf.cindy.Session;
import net.sf.cindy.buffer.BufferFactory;
import net.sf.cindy.packet.DefaultPacket;
import net.sf.cindy.util.Charset;

/**
 * <code>HttpResponse</code> Encoder.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class HttpResponseEncoder implements PacketEncoder {

    public Packet encode(Session session, Object obj) throws Exception {
        HttpResponse response = (HttpResponse) obj;

        ByteBuffer header = Charset.UTF8.encode(response.toString());

        Buffer buffer = null;
        int contentLen = response.getContent().length;
        if (contentLen > 0) {
            buffer = BufferFactory.allocate(header.remaining()
                    + response.getContent().length);
            buffer.put(header).put(response.getContent()).flip();
        } else {
            buffer = BufferFactory.wrap(header);
        }

        return new DefaultPacket(buffer);
    }

}
