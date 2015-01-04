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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamConstants;

import net.sf.cindy.Buffer;
import net.sf.cindy.Packet;
import net.sf.cindy.PacketDecoder;
import net.sf.cindy.Session;

/**
 * Decode <code>Packet</code> to serial object.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class SerialDecoder implements PacketDecoder {

    public Object decode(Session session, Packet packet) throws Exception {
        final Buffer buffer = packet.getContent();
        if (buffer.remaining() < 4)
            return null;

        if (buffer.getShort() == ObjectStreamConstants.STREAM_MAGIC
                && buffer.getShort() == ObjectStreamConstants.STREAM_VERSION) {
            buffer.skip(-4);

            ObjectInputStream is = null;
            try {
                is = new ObjectInputStream(new InputStream() {

                    public int read() {
                        return buffer.hasRemaining() ? buffer.get() : -1;
                    }

                    public int read(byte[] bytes, int off, int len) {
                        if (buffer.hasRemaining()) {
                            len = Math.min(len, buffer.remaining());
                            buffer.get(bytes, off, len);
                            return len;
                        }
                        return -1;
                    }

                });
                return is.readObject();
            } catch (EOFException e) {
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return null;
    }
}
