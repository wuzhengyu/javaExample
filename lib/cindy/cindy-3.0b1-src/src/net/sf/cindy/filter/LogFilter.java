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
import net.sf.cindy.SessionFilterAdapter;
import net.sf.cindy.SessionFilterChain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Log filter.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class LogFilter extends SessionFilterAdapter {

    private static final Log log = LogFactory.getLog(LogFilter.class);

    public void exceptionCaught(SessionFilterChain filterChain, Throwable cause) {
        log.error(filterChain.getSession() + " caught exception: " + cause,
                cause);
        super.exceptionCaught(filterChain, cause);
    }

    public void packetReceived(SessionFilterChain filterChain, Packet packet)
            throws Exception {
        log.info(filterChain.getSession() + " received packet: " + packet);
        super.packetReceived(filterChain, packet);
    }

    public void objectReceived(SessionFilterChain filterChain, Object obj)
            throws Exception {
        log.info(filterChain.getSession() + " received object: " + obj);
        super.objectReceived(filterChain, obj);
    }

    public void packetSend(SessionFilterChain filterChain, Packet packet)
            throws Exception {
        log.info(filterChain.getSession() + " send packet: " + packet);
        super.packetSend(filterChain, packet);
    }

    public void packetSent(SessionFilterChain filterChain, Packet packet)
            throws Exception {
        log.info(filterChain.getSession() + " sent packet: " + packet);
        super.packetSent(filterChain, packet);
    }

    public void objectSent(SessionFilterChain filterChain, Object obj)
            throws Exception {
        log.info(filterChain.getSession() + " sent object: " + obj);
        super.objectSent(filterChain, obj);
    }

    public void sessionClosed(SessionFilterChain filterChain) throws Exception {
        log.info(filterChain.getSession() + " closed");
        super.sessionClosed(filterChain);
    }

    public void sessionStarted(SessionFilterChain filterChain) throws Exception {
        log.info(filterChain.getSession() + " started");
        super.sessionStarted(filterChain);
    }

    public void sessionTimeout(SessionFilterChain filterChain) throws Exception {
        log.info(filterChain.getSession() + " timeout");
        super.sessionTimeout(filterChain);
    }

}
