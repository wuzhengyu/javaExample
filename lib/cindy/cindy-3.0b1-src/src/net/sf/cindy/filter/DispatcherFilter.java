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
import net.sf.cindy.session.dispatcher.Dispatcher;

/**
 * Dispatch events. The first inner filter.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class DispatcherFilter implements SessionFilter {

    private final Dispatcher dispatcher;

    public DispatcherFilter(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public void exceptionCaught(final SessionFilterChain filterChain,
            final Throwable cause) {
        dispatcher.dispatch(filterChain.getSession(), new Runnable() {

            public void run() {
                filterChain.exceptionCaught(cause);
            }
        });
    }

    public void packetReceived(final SessionFilterChain filterChain,
            final Packet packet) throws Exception {
        dispatcher.dispatch(filterChain.getSession(), new Runnable() {

            public void run() {
                filterChain.packetReceived(packet);
            }
        });
    }

    public void objectReceived(final SessionFilterChain filterChain,
            final Object obj) throws Exception {
        dispatcher.dispatch(filterChain.getSession(), new Runnable() {

            public void run() {
                filterChain.objectReceived(obj);
            }
        });
    }

    public void packetSend(final SessionFilterChain filterChain,
            final Packet packet) throws Exception {
        dispatcher.dispatch(filterChain.getSession(), new Runnable() {

            public void run() {
                filterChain.packetSend(packet);
            }
        });
    }

    public void packetSent(final SessionFilterChain filterChain,
            final Packet packet) throws Exception {
        dispatcher.dispatch(filterChain.getSession(), new Runnable() {

            public void run() {
                filterChain.packetSent(packet);
            }
        });
    }

    public void objectSent(final SessionFilterChain filterChain,
            final Object obj) throws Exception {
        dispatcher.dispatch(filterChain.getSession(), new Runnable() {

            public void run() {
                filterChain.objectSent(obj);
            };
        });
    }

    public void sessionClosed(final SessionFilterChain filterChain)
            throws Exception {
        dispatcher.dispatch(filterChain.getSession(), new Runnable() {

            public void run() {
                filterChain.sessionClosed();
            }
        });
    }

    public void sessionStarted(final SessionFilterChain filterChain)
            throws Exception {
        dispatcher.dispatch(filterChain.getSession(), new Runnable() {

            public void run() {
                filterChain.sessionStarted();
            }
        });
    }

    public void sessionTimeout(final SessionFilterChain filterChain)
            throws Exception {
        dispatcher.dispatch(filterChain.getSession(), new Runnable() {

            public void run() {
                filterChain.sessionTimeout();
            }
        });
    }

}
