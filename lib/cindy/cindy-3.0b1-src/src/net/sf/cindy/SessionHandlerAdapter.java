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

/**
 * <code>SessionHandler</code> adapter.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class SessionHandlerAdapter implements SessionHandler {

    public void sessionClosed(Session session) throws Exception {
    }

    public void sessionStarted(Session session) throws Exception {
    }

    public void sessionTimeout(Session session) throws Exception {
    }

    public void objectReceived(Session session, Object obj) throws Exception {
    }

    public void objectSent(Session session, Object obj) throws Exception {
    }

    public void exceptionCaught(Session session, Throwable cause) {
    }

}
