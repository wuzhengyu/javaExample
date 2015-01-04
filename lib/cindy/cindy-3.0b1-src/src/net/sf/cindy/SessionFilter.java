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
 * Session filter, filter session events.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public interface SessionFilter {

    /**
     * Session have started.
     * 
     * @param filterChain
     *            session filter chain
     * @throws Exception
     *             any exception
     */
    void sessionStarted(SessionFilterChain filterChain) throws Exception;

    /**
     * Session have closed.
     * 
     * @param filterChain
     *            session filter chain
     * @throws Exception
     *             any exception
     */
    void sessionClosed(SessionFilterChain filterChain) throws Exception;

    /**
     * Session timeout, but not closed.
     * 
     * @param filterChain
     *            session filter chain
     * @throws Exception
     *             any exception
     */
    void sessionTimeout(SessionFilterChain filterChain) throws Exception;

    /**
     * Session received a packet.
     * 
     * @param filterChain
     *            session filter chain
     * @param packet
     *            the received packet
     * @throws Exception
     *             any exception
     */
    void packetReceived(SessionFilterChain filterChain, Packet packet)
            throws Exception;

    /**
     * Session received a object which is decoded by <code>PacketDecoder</code>.
     * 
     * @param filterChain
     *            session filter chain
     * @param obj
     *            object
     * @throws Exception
     *             any exception
     */
    void objectReceived(SessionFilterChain filterChain, Object obj)
            throws Exception;

    /**
     * Filter before send packet. This event will be dispatched in the reversed
     * order.
     * 
     * @param filterChain
     *            session filter chain
     * @param packet
     *            send packet
     * @throws Exception
     *             any exception
     */
    void packetSend(SessionFilterChain filterChain, Packet packet)
            throws Exception;

    /**
     * Session sent a packet. This event will be dispatched in the reversed
     * order. The packet's position will not be updated.
     * 
     * @param filterChain
     *            session filter chain
     * @param packet
     *            the sent packet
     * @throws Exception
     *             any exception
     */
    void packetSent(SessionFilterChain filterChain, Packet packet)
            throws Exception;

    /**
     * Session sent a object. This event will be dispatched in the reversed
     * order.
     * 
     * @param filterChain
     *            session filter chain
     * @param obj
     *            the sent object
     * @throws Exception
     *             any exception
     */
    void objectSent(SessionFilterChain filterChain, Object obj)
            throws Exception;

    /**
     * Session caught a exception.
     * 
     * @param filterChain
     *            session filter chain
     * @param cause
     *            exception
     */
    void exceptionCaught(SessionFilterChain filterChain, Throwable cause);
}
