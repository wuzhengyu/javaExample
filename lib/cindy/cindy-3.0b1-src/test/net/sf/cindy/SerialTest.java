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
package net.sf.cindy;

import java.util.Random;

import junit.framework.TestCase;
import net.sf.cindy.Packet;
import net.sf.cindy.decoder.SerialDecoder;
import net.sf.cindy.encoder.SerialEncoder;

/**
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class SerialTest extends TestCase {

    private Random random = new Random();

    public void testEncodeDecode() {
        try {
            for (int i = 0; i < 10; i++) {
                int value = random.nextInt();

                Object obj1 = new Integer(value);

                Packet packet = new SerialEncoder().encode(null, obj1);
                assertNotNull(packet);

                Object obj2 = new SerialDecoder().decode(null, packet);
                assertEquals(obj1, obj2);
            }
        } catch (Exception e) {
            fail(e.toString());
        }
    }
}
