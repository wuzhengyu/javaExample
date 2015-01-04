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

import java.net.SocketAddress;

/**
 * Session acceptor, accept incoming sessions.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public interface SessionAcceptor {

    /**
     * Get the session type of the session accepted by the acceptor.
     * 
     * @return session type
     */
    public SessionType getSessionType();

    /**
     * Get the acceptor handler associated with this acceptor.
     * 
     * @return session acceptor handler
     */
    public SessionAcceptorHandler getAcceptorHandler();

    /**
     * Set the acceptor handler associated with this acceptor.
     * 
     * @param handler
     *            session acceptor handler
     */
    public void setAcceptorHandler(SessionAcceptorHandler handler);

    /**
     * Set the listen address which the acceptor bind with.
     * 
     * @param address
     *            the local address
     * @throws IllegalStateException
     */
    void setListenAddress(SocketAddress address);

    /**
     * Get the listen address which the accpetor bind with.
     * 
     * @return the local address
     */
    SocketAddress getListenAddress();

    /**
     * Set the listen port which the acceptor bind with.
     * 
     * @param port
     *            the local port
     * @throws IllegalStateException
     */
    void setListenPort(int port);

    /**
     * Get the listen port which the acceptor bind with.
     * 
     * @return the local port
     */
    int getListenPort();

    /**
     * Set the maximum queue length for incoming connection. If the value passed
     * equal or less than 0, then the default value will be assumed.
     * 
     * @param backlog
     *            the maximum length of the queue
     */
    void setBacklog(int backlog);

    /**
     * Get the maximum queue length for incoming connection. If the returned
     * value equal or less than 0, then the default value will be assumed.
     * 
     * @return the maximum length of the queue
     */
    int getBacklog();

    /**
     * Enable/disable the SO_REUSEADDR socket option.
     * 
     * @param b
     *            whether to enable or disable the socket option
     */
    void setReuseAddress(boolean b);

    /**
     * Tests if SO_REUSEADDR is enabled.
     * 
     * @return a boolean indicating whether or not SO_REUSEADDR is enabled
     */
    boolean isReuseAddress();

    /**
     * Acceptor is started.
     * 
     * @return is started
     */
    boolean isStarted();

    /**
     * Start acceptor.
     * 
     * @throws IllegalStateException
     */
    void start();

    /**
     * Close acceptor.
     */
    void close();

    /**
     * Get accepted session count.
     * 
     * @return accepted session count
     */
    int getAcceptedCount();

}
