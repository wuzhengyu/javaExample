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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.cindy.Session;
import net.sf.cindy.SessionHandlerAdapter;

/**
 * Chat handler.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class ChatHandler extends SessionHandlerAdapter {

    private static final Set sessions = Collections
            .synchronizedSet(new HashSet());

    private static final Object USER_ID_ATTR = "userId";

    private static int counter = 0; // should use AtomicInteger instead

    public void sessionStarted(Session session) throws Exception {
        sessions.add(session);

        int id = ++counter;
        session.setAttribute(USER_ID_ATTR, new Integer(id));

        // send to current user
        session.send("Welcome, User " + id + "!");
        // send to other user
        send(session, "User " + id + " log in");
    }

    public void sessionTimeout(Session session) throws Exception {
        session.close();
    }

    public void sessionClosed(Session session) throws Exception {
        sessions.remove(session);
        send(session, "User " + session.getAttribute(USER_ID_ATTR) + " log out");
    }

    public void objectReceived(Session session, Object obj) throws Exception {
        String senderId = session.getAttribute(USER_ID_ATTR).toString();
        String msg = (String) obj;
        send(session, "User " + senderId + " say: " + msg);
    }

    private void send(Session srcSession, String message) {
        // send message to other user
        synchronized (sessions) {
            for (Iterator iter = sessions.iterator(); iter.hasNext();) {
                Session session = (Session) iter.next();
                if (session != srcSession) {
                    session.send(message);
                }
            }
        }
    }

}
