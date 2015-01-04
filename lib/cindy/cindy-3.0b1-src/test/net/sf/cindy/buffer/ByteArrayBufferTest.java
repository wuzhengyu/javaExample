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
package net.sf.cindy.buffer;

import net.sf.cindy.Buffer;

/**
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class ByteArrayBufferTest extends BufferTest {

    protected Buffer newBuffer(int capacity) {
        return ByteArrayBuffer.allocate(capacity);
    }

    public void testWrap() {
        byte[] b = new byte[random.nextInt(10000)];
        random.nextBytes(b);
        Buffer buffer = ByteArrayBuffer.wrap(b);
        assertEquals(0, buffer.position());
        assertEquals(b.length, buffer.limit());
        assertEquals(b.length, buffer.capacity());
        assertEquals(b.length, buffer.remaining());
        for (int i = 0; i < b.length; i++) {
            assertEquals(b[i], buffer.get());
        }

        int offset = random.nextInt(b.length);
        int len = b.length - offset;
        buffer = ByteArrayBuffer.wrap(b, offset, len);
        assertEquals(offset, buffer.position());
        assertEquals(b.length, buffer.limit());
        assertEquals(b.length, buffer.capacity());
        assertEquals(len, buffer.remaining());
        for (int i = offset; i < b.length; i++) {
            assertEquals(b[i], buffer.get());
        }
    }
}
