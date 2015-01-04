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

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.cindy.buffer.BufferBuilderTest;
import net.sf.cindy.buffer.ByteArrayBufferTest;
import net.sf.cindy.buffer.ByteBufferBufferTest;
import net.sf.cindy.buffer.DefaultBufferPoolTest;
import net.sf.cindy.buffer.LinkedBufferTest;
import net.sf.cindy.session.AbstractSessionTest;
import net.sf.cindy.session.dispatcher.DirectDispatcherTest;
import net.sf.cindy.util.CharsetTest;
import net.sf.cindy.util.ElapsedTimeTest;
import net.sf.cindy.util.SpeedTest;

/**
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class CindyTests {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(CindyTests.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for net.sf.cindy");
        // $JUnit-BEGIN$

        suite.addTestSuite(BufferBuilderTest.class);
        suite.addTestSuite(ByteArrayBufferTest.class);
        suite.addTestSuite(ByteBufferBufferTest.class);
        suite.addTestSuite(LinkedBufferTest.class);
        suite.addTestSuite(DefaultBufferPoolTest.class);

        suite.addTestSuite(SerialTest.class);

        suite.addTestSuite(CharsetTest.class);
        suite.addTestSuite(ElapsedTimeTest.class);
        suite.addTestSuite(SpeedTest.class);

        suite.addTestSuite(AbstractSessionTest.class);

        suite.addTestSuite(DirectDispatcherTest.class);

        // $JUnit-END$
        return suite;
    }
}
