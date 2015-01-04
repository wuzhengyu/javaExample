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
package net.sf.cindy.session;

import junit.framework.TestCase;
import net.sf.cindy.Future;
import net.sf.cindy.Packet;
import net.sf.cindy.Session;
import net.sf.cindy.SessionFilter;
import net.sf.cindy.SessionFilterAdapter;
import net.sf.cindy.SessionType;

/**
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class AbstractSessionTest extends TestCase {

    private Session session;

    protected void setUp() throws Exception {
        super.setUp();
        session = new AbstractSession() {

            public SessionType getSessionType() {
                return SessionType.UNKNOWN;
            }

            protected Future send(Object obj, Packet packet, int priority) {
                return null;
            }

            public Future close() {
                return null;
            }

            public boolean isStarted() {
                return false;
            }

            public Future start() {
                return null;
            }

        };
    }

    protected void tearDown() throws Exception {
        session = null;
        super.tearDown();
    }

    public void testFilter() {
        assertEquals(0, session.getSessionFilters().length);

        SessionFilter first = new SessionFilterAdapter();
        session.addSessionFilter(first);
        assertEquals(1, session.getSessionFilters().length);
        assertEquals(first, session.getSessionFilters()[0]);

        SessionFilter second = new SessionFilterAdapter();
        session.addSessionFilter(second);
        assertEquals(2, session.getSessionFilters().length);
        assertEquals(first, session.getSessionFilters()[0]);
        assertEquals(second, session.getSessionFilters()[1]);

        SessionFilter third = new SessionFilterAdapter();
        session.addSessionFilter(1, third);
        assertEquals(3, session.getSessionFilters().length);
        assertEquals(first, session.getSessionFilters()[0]);
        assertEquals(third, session.getSessionFilters()[1]);
        assertEquals(second, session.getSessionFilters()[2]);

        session.removeSessionFilter(first);
        assertEquals(2, session.getSessionFilters().length);
        assertEquals(third, session.getSessionFilters()[0]);
        assertEquals(second, session.getSessionFilters()[1]);
    }
}
