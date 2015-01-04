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
 * <code>Session</code> handler.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public interface SessionHandler {

    /**
     * Session have started.
     * 
     * @param session
     *            session
     * @throws Exception
     *             any exception
     */
    void sessionStarted(Session session) throws Exception;

    /**
     * Session have closed.
     * 
     * @param session
     *            session
     * @throws Exception
     *             any exception
     */
    void sessionClosed(Session session) throws Exception;

    /**
     * Session timeout, but not closed.
     * 
     * @param session
     *            session
     * @throws Exception
     *             any exception
     */
    void sessionTimeout(Session session) throws Exception;

    /**
     * Session received a object which is decoded by <code>PacketDecoder</code>.
     * 
     * @param session
     *            session
     * @param obj
     *            object
     * @throws Exception
     *             any exception
     */
    void objectReceived(Session session, Object obj) throws Exception;

    /**
     * Session sent a object.
     * 
     * @param session
     *            session
     * @param obj
     *            object
     * @throws Exception
     *             any exception
     */
    void objectSent(Session session, Object obj) throws Exception;

    /**
     * Session caught a exception.
     * 
     * @param session
     *            session
     * @param cause
     *            exception
     */
    void exceptionCaught(Session session, Throwable cause);
}
