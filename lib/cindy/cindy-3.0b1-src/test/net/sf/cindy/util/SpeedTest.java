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

import junit.framework.TestCase;

/**
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class SpeedTest extends TestCase {

    public void testSpeed() {
        final Speed speed = Speed.getInstance(1000, 1000);
        assertTrue(speed.getSpeed() == 0);
        assertTrue(speed.getAvgSpeed() == 0);
        speed.addValue(100);
        speed.reset();
        assertTrue(speed.getSpeed() == 0);
        assertTrue(speed.getAvgSpeed() == 0);
        assertTrue(speed.getElapsedTime() >= 0);
        speed.addValue(1000);
        assertTrue(speed.getAvgSpeed() > 0);
    }
}
