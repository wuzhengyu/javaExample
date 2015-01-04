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

import java.nio.ByteBuffer;

import net.sf.cindy.Buffer;

/**
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class ByteBufferBufferTest extends BufferTest {

    protected Buffer newBuffer(int capacity) {
        return ByteBufferBuffer.allocate(capacity, false);
    }

    public void testWrap() {
        byte[] b = new byte[random.nextInt(10000)];
        random.nextBytes(b);
        ByteBuffer b1 = ByteBuffer.wrap(b);

        Buffer buffer = ByteBufferBuffer.wrap(b1);
        assertEquals(0, buffer.position());
        assertEquals(b1.limit(), buffer.limit());
        assertEquals(b1.capacity(), buffer.capacity());
        assertEquals(b1.remaining(), buffer.remaining());
        for (int i = 0; i < b.length; i++) {
            assertEquals(b[i], buffer.get());
        }

        int offset = random.nextInt(b.length);
        int len = b.length - offset;
        b1 = ByteBuffer.wrap(b, offset, len);
        buffer = ByteBufferBuffer.wrap(b1);
        assertEquals(b1.position(), buffer.position());
        assertEquals(b1.limit(), buffer.limit());
        assertEquals(b1.capacity(), buffer.capacity());
        assertEquals(b1.remaining(), buffer.remaining());
        while (b1.hasRemaining()) {
            assertEquals(b1.get(), buffer.get());
        }
    }
}
