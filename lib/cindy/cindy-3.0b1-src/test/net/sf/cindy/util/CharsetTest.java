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
package net.sf.cindy.util;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import junit.framework.TestCase;

/**
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class CharsetTest extends TestCase {

    public void testEncodeAndDecode() {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            StringBuffer buffer = new StringBuffer();
            for (int j = 0; j < random.nextInt(100); j++) {
                buffer.append((char) ('a' + random.nextInt(26)));
            }
            String s1 = buffer.toString();
            String s2 = Charset.UTF8.decode(Charset.UTF8.encode(s1)).toString();
            String s3 = Charset.SYSTEM.decode(Charset.SYSTEM.encode(s1))
                    .toString();
            String s4 = null;
            try {
                s4 = new String(Charset.UTF8.encodeToArray(s1), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                fail(e.toString());
            }
            assertEquals(s1, s2);
            assertEquals(s1, s3);
            assertEquals(s1, s4);
        }

    }
}
