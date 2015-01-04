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
 * Session filter chain, manage session filters.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public interface SessionFilterChain {

    /**
     * Get session associated with the session filter chain.
     * 
     * @return session
     */
    Session getSession();

    /**
     * Session have established.
     */
    void sessionStarted();

    /**
     * Session have closed or refused.
     */
    void sessionClosed();

    /**
     * Session timeout, but not closed.
     */
    void sessionTimeout();

    /**
     * Session received a packet.
     * 
     * @param packet
     *            the received packet
     */
    void packetReceived(Packet packet);

    /**
     * Session received a object which is decoded by <code>PacketDecoder</code>.
     * 
     * @param obj
     *            object
     */
    void objectReceived(Object obj);

    /**
     * Filter before send packet.
     * 
     * @param packet
     *            send packet
     */
    void packetSend(Packet packet);

    /**
     * Session sent a packet.
     * 
     * @param packet
     *            the sent packet
     */
    void packetSent(Packet packet);

    /**
     * Session sent a object.
     * 
     * @param obj
     *            the sent object
     */
    void objectSent(Object obj);

    /**
     * Session caught a exception.
     * 
     * @param cause
     *            exception
     */
    void exceptionCaught(Throwable cause);
}
