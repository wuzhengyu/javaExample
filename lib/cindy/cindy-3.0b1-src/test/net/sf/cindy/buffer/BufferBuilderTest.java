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

import java.util.Random;

import net.sf.cindy.Buffer;

import junit.framework.TestCase;

/**
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class BufferBuilderTest extends TestCase {

    private Random random = new Random();

    public void testAppendByte() {
        BufferBuilder builder = new BufferBuilder(random.nextInt(256));
        for (int i = 0; i < 256; i++) {
            builder.append((byte) i);
        }
        Buffer buffer = builder.toBuffer();
        assertEquals(buffer.remaining(), 256);
        for (int i = 0; i < 256; i++) {
            assertEquals((byte) i, buffer.get());
        }
        builder.release();
    }
}
