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
package net.sf.cindy.decoder;

import java.util.Collection;
import java.util.Iterator;

import net.sf.cindy.Buffer;
import net.sf.cindy.Packet;
import net.sf.cindy.PacketDecoder;
import net.sf.cindy.Session;
import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArrayList;

/**
 * <code>PacketDecoder</code> chain.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class PacketDecoderChain implements PacketDecoder {

    private final Collection chain = new CopyOnWriteArrayList();

    public PacketDecoderChain addPacketDecoder(PacketDecoder decoder) {
        if (decoder != null)
            chain.add(decoder);
        return this;
    }

    public PacketDecoderChain removePacketDecoder(PacketDecoder decoder) {
        chain.remove(decoder);
        return this;
    }

    public Object decode(Session session, Packet packet) throws Exception {
        Buffer content = packet.getContent();
        int position = content.position();
        int limit = content.limit();

        for (Iterator iter = chain.iterator(); iter.hasNext();) {
            PacketDecoder decoder = (PacketDecoder) iter.next();
            Object obj = decoder.decode(session, packet);
            if (obj != null)
                return obj;

            // not use slice to create new buffer, reduce memory cost
            content.limit(limit);
            content.position(position);
        }
        return null;
    }

}
