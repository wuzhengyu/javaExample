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
public class LinkedBufferTest extends BufferTest {

    protected Buffer newBuffer(int capacity) {
        int count = random.nextInt(3) + 1;
        Buffer[] buffers = new Buffer[count];
        for (int i = 0; i < count - 1; i++) {
            boolean useArray = random.nextBoolean();
            int allocateSize = capacity == 0 ? 0 : random.nextInt(capacity);
            capacity -= allocateSize;
            buffers[i] = useArray ? (Buffer) ByteArrayBuffer
                    .allocate(allocateSize) : ByteBufferBuffer.allocate(
                    allocateSize, false);
        }
        buffers[count - 1] = ByteBufferBuffer.allocate(capacity, true);
        return new LinkedBuffer(buffers);
    }
}
