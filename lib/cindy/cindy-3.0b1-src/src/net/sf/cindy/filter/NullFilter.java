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

import net.sf.cindy.Packet;
import net.sf.cindy.SessionFilter;
import net.sf.cindy.SessionFilterChain;

/**
 * Null filter. Null-Object pattern.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class NullFilter implements SessionFilter {

    public void exceptionCaught(SessionFilterChain filterChain, Throwable cause) {
    }

    public void packetReceived(SessionFilterChain filterChain, Packet packet)
            throws Exception {
    }

    public void objectReceived(SessionFilterChain filterChain, Object obj)
            throws Exception {
    }

    public void packetSend(SessionFilterChain filterChain, Packet packet)
            throws Exception {
    }

    public void packetSent(SessionFilterChain filterChain, Packet packet)
            throws Exception {
    }

    public void objectSent(SessionFilterChain filterChain, Object obj)
            throws Exception {
    }

    public void sessionClosed(SessionFilterChain filterChain) throws Exception {
    }

    public void sessionStarted(SessionFilterChain filterChain) throws Exception {
    }

    public void sessionTimeout(SessionFilterChain filterChain) throws Exception {
    }

}
