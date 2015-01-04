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

import junit.framework.TestCase;
import net.sf.cindy.Buffer;

/**
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class DefaultBufferPoolTest extends TestCase {

    private BufferPool pool;
    private Random random = new Random();

    protected void setUp() throws Exception {
        pool = new DefaultBufferPool();
        super.setUp();
    }

    public void testAllocate() {
        for (int i = 0; i < 100; i++) {
            Buffer[] buffers = new Buffer[random.nextInt(100)];
            for (int j = 0; j < buffers.length; j++) {
                boolean direct = random.nextBoolean();
                int capacity = random.nextInt(1000000);
                buffers[j] = pool.allocate(capacity, direct);

                assertEquals(direct, buffers[j].isDirect());
                assertEquals(capacity, buffers[j].remaining());
                assertEquals(capacity, buffers[j].capacity());
                assertEquals(capacity, buffers[j].limit());
                assertEquals(0, buffers[j].position());
            }
            for (int j = 0; j < buffers.length; j++) {
                buffers[j].release();
            }
        }
    }
}
