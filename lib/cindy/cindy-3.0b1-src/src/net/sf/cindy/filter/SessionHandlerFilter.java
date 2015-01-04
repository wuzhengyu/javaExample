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
package net.sf.cindy.filter;

import net.sf.cindy.Session;
import net.sf.cindy.SessionFilterChain;
import net.sf.cindy.SessionHandler;

/**
 * <code>SessionHandler</code> filter. The last inner filter.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class SessionHandlerFilter extends NullFilter {

    public void exceptionCaught(SessionFilterChain filterChain, Throwable cause) {
        Session session = filterChain.getSession();
        SessionHandler handler = session.getSessionHandler();
        if (handler != null)
            handler.exceptionCaught(session, cause);
    }

    public void objectReceived(SessionFilterChain filterChain, Object obj)
            throws Exception {
        Session session = filterChain.getSession();
        SessionHandler handler = session.getSessionHandler();
        if (handler != null)
            handler.objectReceived(session, obj);
    }

    public void objectSent(SessionFilterChain filterChain, Object obj)
            throws Exception {
        Session session = filterChain.getSession();
        SessionHandler handler = session.getSessionHandler();
        if (handler != null)
            handler.objectSent(session, obj);
    }

    public void sessionClosed(SessionFilterChain filterChain) throws Exception {
        Session session = filterChain.getSession();
        SessionHandler handler = session.getSessionHandler();
        if (handler != null)
            handler.sessionClosed(session);
    }

    public void sessionStarted(SessionFilterChain filterChain) throws Exception {
        Session session = filterChain.getSession();
        SessionHandler handler = session.getSessionHandler();
        if (handler != null)
            handler.sessionStarted(session);
    }

    public void sessionTimeout(SessionFilterChain filterChain) throws Exception {
        Session session = filterChain.getSession();
        SessionHandler handler = session.getSessionHandler();
        if (handler != null)
            handler.sessionTimeout(session);
    }
}
