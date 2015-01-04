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
 * <code>SessionFilter</code> adapter.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class SessionFilterAdapter implements SessionFilter {

    public void exceptionCaught(SessionFilterChain filterChain, Throwable cause) {
        filterChain.exceptionCaught(cause);
    }

    public void packetReceived(SessionFilterChain filterChain, Packet packet)
            throws Exception {
        filterChain.packetReceived(packet);
    }

    public void objectReceived(SessionFilterChain filterChain, Object obj)
            throws Exception {
        filterChain.objectReceived(obj);
    }

    public void packetSend(SessionFilterChain filterChain, Packet packet)
            throws Exception {
        filterChain.packetSend(packet);
    }

    public void packetSent(SessionFilterChain filterChain, Packet packet)
            throws Exception {
        filterChain.packetSent(packet);
    }

    public void objectSent(SessionFilterChain filterChain, Object obj)
            throws Exception {
        filterChain.objectSent(obj);
    }

    public void sessionClosed(SessionFilterChain filterChain) throws Exception {
        filterChain.sessionClosed();
    }

    public void sessionStarted(SessionFilterChain filterChain) throws Exception {
        filterChain.sessionStarted();
    }

    public void sessionTimeout(SessionFilterChain filterChain) throws Exception {
        filterChain.sessionTimeout();
    }

}
