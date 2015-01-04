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
import java.util.Map;

/**
 * Session represents the network connection.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public interface Session {

    /**
     * Get session type.
     * 
     * @return session type
     */
    SessionType getSessionType();

    /**
     * Set the remote address which the session will connected to.
     * 
     * @param address
     *            the remote address
     * @throws IllegalStateException
     */
    void setRemoteAddress(SocketAddress address);

    /**
     * Get the remote address which the session connmected to.
     * 
     * @return the remote address
     */
    SocketAddress getRemoteAddress();

    /**
     * Set the local address which the session bind with.
     * 
     * @param address
     *            the local address
     * @throws IllegalStateException
     */
    void setLocalAddress(SocketAddress address);

    /**
     * Get the local address which the session bind with.
     * 
     * @return the local address
     */
    SocketAddress getLocalAddress();

    /**
     * Set packet decoder.
     * 
     * @param decoder
     *            packet decoder
     */
    void setPacketDecoder(PacketDecoder decoder);

    /**
     * Get packet decoder.
     * 
     * @return packet decoder
     */
    PacketDecoder getPacketDecoder();

    /**
     * Set packet encoder.
     * 
     * @param encoder
     *            packet encoder
     */
    void setPacketEncoder(PacketEncoder encoder);

    /**
     * Get packet encoder.
     * 
     * @return packet encoder
     */
    PacketEncoder getPacketEncoder();

    /**
     * Get all session attributs. The returned map is unmodifiable.
     * 
     * @return all attributes
     */
    Map getAttributes();

    /**
     * Returns <tt>true</tt> if the session contains an attribute for the
     * specified key.
     * 
     * @param key
     *            key
     * @return <tt>true</tt> if the session contains an attribute for the
     *         specified key
     */
    boolean containsAttribute(Object key);

    /**
     * Get the session attribute mapped with the specified key.
     * 
     * @param key
     *            key
     * @return attribute
     */
    Object getAttribute(Object key);

    /**
     * Set session attribute.
     * 
     * @param key
     *            key
     * @param attribute
     *            attribute
     */
    void setAttribute(Object key, Object attribute);

    /**
     * Removes the attribute for this key if it is present.
     * 
     * @param key
     *            key
     */
    void removeAttribute(Object key);

    /**
     * Set session timeout, in millisecond.
     * 
     * @param timeout
     *            session timeout
     */
    void setSessionTimeout(int timeout);

    /**
     * Get session timeout, in millisecond.
     * 
     * @return session timeout
     */
    int getSessionTimeout();

    /**
     * Get session filter chain.
     * 
     * @param reversed
     *            some events like xxxSend/xxxSent need to be dispatched in
     *            reversed order
     * @return session filter chain
     */
    SessionFilterChain getSessionFilterChain(boolean reversed);

    /**
     * Add session filter.
     * 
     * @param filter
     *            session filter
     */
    void addSessionFilter(SessionFilter filter);

    /**
     * Add session filter.
     * 
     * @param index
     *            index
     * @param filter
     *            session filter
     */
    void addSessionFilter(int index, SessionFilter filter);

    /**
     * Remove session filter.
     * 
     * @param filter
     *            session filter
     */
    void removeSessionFilter(SessionFilter filter);

    /**
     * Get session filter.
     * 
     * @param index
     *            index
     * @return session filter
     */
    SessionFilter getSessionFilter(int index);

    /**
     * Get all session filters.
     * 
     * @return session filters
     */
    SessionFilter[] getSessionFilters();

    /**
     * Set session handler.
     * 
     * @param handler
     *            session handler
     */
    void setSessionHandler(SessionHandler handler);

    /**
     * Get session handler.
     * 
     * @return session handler
     */
    SessionHandler getSessionHandler();

    /**
     * Session have started.
     * 
     * @return session have started
     */
    boolean isStarted();

    /**
     * Start the session.
     * 
     * @return start future
     */
    Future start();

    /**
     * Close the session.
     * 
     * @return close future
     */
    Future close();

    /**
     * Send packet with normal priority. This method is a shorthand for
     * flush(packet, 0).
     * 
     * @param packet
     *            send packet
     * @return send future
     */
    Future flush(Packet packet);

    /**
     * Send packet with specified priority. Integer.MAX_VALUE indicate the max
     * priority, Integer.MIN_VALUE indicate the min priority and 0 indicate the
     * normal priority.
     * 
     * @param packet
     *            send packet
     * @param priority
     *            send priority
     * @return send future
     */
    Future flush(Packet packet, int priority);

    /**
     * Send object with normal priority. The object will be encoded to
     * <code>Packet</code> use associated <code>PacketEncoder</code>. This
     * method is a shorthand for send(obj, 0).
     * 
     * @param obj
     *            send object
     * @return send future
     */
    Future send(Object obj);

    /**
     * Send object with specified priority. Integer.MAX_VALUE indicate the max
     * priority, Integer.MIN_VALUE indicate the min priority and 0 indicate the
     * normal priority. The object will be encoded to <code>Packet</code> use
     * associated <code>PacketEncoder</code>.
     * 
     * @param obj
     *            send object
     * @param priority
     *            send priority
     * @return send future
     */
    Future send(Object obj, int priority);

}
