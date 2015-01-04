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
package net.sf.cindy.session;

import net.sf.cindy.Packet;
import net.sf.cindy.SessionFilter;
import net.sf.cindy.SessionFilterChain;
import net.sf.cindy.filter.NullFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract session filter chain.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public abstract class AbstractSessionFilterChain implements SessionFilterChain {

    private static final Log log = LogFactory
            .getLog(AbstractSessionFilterChain.class);

    /**
     * Null-Object pattern.
     */
    private static final SessionFilter NULL_FILTER = new NullFilter();

    protected abstract SessionFilter nextFilter();

    private void caughtException(Throwable throwable) {
        getSession().getSessionFilterChain(false).exceptionCaught(throwable);
    }

    private SessionFilter next() {
        SessionFilter filter = nextFilter();
        return filter == null ? NULL_FILTER : filter;
    }

    public void exceptionCaught(Throwable cause) {
        try {
            next().exceptionCaught(this, cause);
        } catch (Throwable e) { // protect catch
            log.error(e, e);
        }
    }

    public void packetReceived(Packet packet) {
        try {
            next().packetReceived(this, packet);
        } catch (Throwable e) {
            caughtException(e);
        }
    }

    public void objectReceived(Object obj) {
        try {
            next().objectReceived(this, obj);
        } catch (Throwable e) {
            caughtException(e);
        }
    }

    public void packetSend(Packet packet) {
        try {
            next().packetSend(this, packet);
        } catch (Throwable e) {
            caughtException(e);
        }
    }

    public void packetSent(Packet packet) {
        try {
            next().packetSent(this, packet);
        } catch (Throwable e) {
            caughtException(e);
        }
    }

    public void objectSent(Object obj) {
        try {
            next().objectSent(this, obj);
        } catch (Throwable e) {
            caughtException(e);
        }
    }

    public void sessionClosed() {
        try {
            next().sessionClosed(this);
        } catch (Throwable e) {
            caughtException(e);
        }
    }

    public void sessionStarted() {
        try {
            next().sessionStarted(this);
        } catch (Throwable e) {
            caughtException(e);
        }

    }

    public void sessionTimeout() {
        try {
            next().sessionTimeout(this);
        } catch (Throwable e) {
            caughtException(e);
        }
    }

}
