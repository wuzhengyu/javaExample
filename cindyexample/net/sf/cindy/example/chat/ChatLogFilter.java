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
package net.sf.cindy.example.chat;

import java.net.SocketAddress;

import net.sf.cindy.Session;
import net.sf.cindy.SessionFilterAdapter;
import net.sf.cindy.SessionFilterChain;

/**
 * Log connect message.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class ChatLogFilter extends SessionFilterAdapter {

    private static final Object ADDR_ATTR = "remoteAddr";

    public void sessionStarted(SessionFilterChain filterChain) throws Exception {
        Session session = filterChain.getSession();
        SocketAddress address = session.getRemoteAddress();
        session.setAttribute(ADDR_ATTR, address);
        System.out.println(address + " log in");
        super.sessionStarted(filterChain);
    }

    public void sessionClosed(SessionFilterChain filterChain) throws Exception {
        System.out.println(filterChain.getSession().getAttribute(ADDR_ATTR)
                + " log out");
        super.sessionClosed(filterChain);
    }

    public void sessionTimeout(SessionFilterChain filterChain) throws Exception {
        System.out.println(filterChain.getSession().getAttribute(ADDR_ATTR)
                + " timeout");
        super.sessionTimeout(filterChain);
    }

    public void exceptionCaught(SessionFilterChain filterChain, Throwable cause) {
        System.err.println(filterChain.getSession().getAttribute(ADDR_ATTR)
                + " " + cause);
        super.exceptionCaught(filterChain, cause);
    }
}
