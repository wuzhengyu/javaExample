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

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import net.sf.cindy.Packet;
import net.sf.cindy.PacketEncoder;
import net.sf.cindy.Session;
import net.sf.cindy.buffer.BufferBuilder;
import net.sf.cindy.packet.DefaultPacket;

/**
 * Encode serial object to <code>Packet</code>.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class SerialEncoder implements PacketEncoder {

    public Packet encode(Session session, Object obj) throws Exception {
        if (obj instanceof Serializable) {
            final BufferBuilder builder = new BufferBuilder();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(
                        new OutputStream() {

                            public void write(int b) {
                                builder.append((byte) b);
                            }

                            public void write(byte[] b, int off, int len) {
                                builder.append(ByteBuffer.wrap(b, off, len));
                            }
                        });
                oos.writeUnshared(obj);
                oos.close();
                return new DefaultPacket(builder.toBuffer());
            } finally {
                builder.release();
            }
        }
        return null;

    }

}
