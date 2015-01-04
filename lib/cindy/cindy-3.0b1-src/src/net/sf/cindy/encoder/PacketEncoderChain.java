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
package net.sf.cindy.encoder;

import java.util.Collection;
import java.util.Iterator;

import net.sf.cindy.Packet;
import net.sf.cindy.PacketEncoder;
import net.sf.cindy.Session;
import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArrayList;

/**
 * <code>PacketEncoder</code> chain.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class PacketEncoderChain implements PacketEncoder {

    private final Collection chain = new CopyOnWriteArrayList();

    public PacketEncoderChain addPacketEncoder(PacketEncoder encoder) {
        if (encoder != null)
            chain.add(encoder);
        return this;
    }

    public PacketEncoderChain removePacketEncoder(PacketEncoder encoder) {
        chain.remove(encoder);
        return this;
    }

    public Packet encode(Session session, Object obj) throws Exception {
        for (Iterator iter = chain.iterator(); iter.hasNext();) {
            PacketEncoder encoder = (PacketEncoder) iter.next();
            Packet packet = encoder.encode(session, obj);
            if (packet != null)
                return packet;
        }
        return null;
    }
}
