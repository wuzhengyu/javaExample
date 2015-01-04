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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import net.sf.cindy.Session;
import net.sf.cindy.SessionAcceptor;
import net.sf.cindy.SessionType;
import net.sf.cindy.session.jmx.JmxSession;
import net.sf.cindy.session.jmx.JmxSessionAcceptor;
import net.sf.cindy.util.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;

/**
 * Session factory. The session factory's behavior may be affected by
 * configuration.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public final class SessionFactory {

    private static final Log log = LogFactory.getLog(SessionFactory.class);

    private static final MBeanServer MBEAN_SERVER;

    static {
        boolean enableJmx = Configuration.isEnableJmx();
        if (enableJmx) {
            try {
                Class.forName("javax.management.StandardMBean");
            } catch (ClassNotFoundException e) {
                enableJmx = false;
            }
        }

        MBeanServer server = null;
        if (enableJmx) {
            List servers = MBeanServerFactory.findMBeanServer(null);
            if (servers.size() > 0)
                server = (MBeanServer) servers.get(0);
            else
                server = MBeanServerFactory.createMBeanServer();
        }
        MBEAN_SERVER = server;
    }

    private static ObjectName getObjectName(String s)
            throws MalformedObjectNameException {
        return ObjectName.getInstance("net.sf.cindy", "name", s);
    }

    private static final AtomicInteger SESSION_COUNTER = new AtomicInteger();
    private static final AtomicInteger ACCEPTOR_COUNTER = new AtomicInteger();

    private static final Map SESSION_MAP = new HashMap();
    private static final Map ACCEPTOR_MAP = new HashMap();

    private static Class getClass(String className) {
        if (className != null)
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                log.error(e, e);
            }
        return null;
    }

    static {
        SESSION_MAP.put(SessionType.TCP,
                getClass(Configuration.getTcpSession()));
        SESSION_MAP.put(SessionType.UDP,
                getClass(Configuration.getUdpSession()));
        SESSION_MAP.put(SessionType.PIPE, getClass(Configuration
                .getPipeSession()));
        SESSION_MAP.put(SessionType.FILE, getClass(Configuration
                .getFileSession()));

        ACCEPTOR_MAP.put(SessionType.TCP, getClass(Configuration
                .getTcpAcceptor()));
    }

    /**
     * Create a new session.
     * 
     * @param type
     *            session type
     * @return created session
     */
    public static Session createSession(SessionType type) {
        Class c = (Class) SESSION_MAP.get(type);
        if (c == null)
            throw new IllegalArgumentException("unsupported session type: "
                    + type);
        try {
            Session session = (Session) c.newInstance();
            if (MBEAN_SERVER != null)
                MBEAN_SERVER.registerMBean(new JmxSession(session),
                        getObjectName("Session-"
                                + SESSION_COUNTER.incrementAndGet()));
            return session;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a socket session.
     * 
     * @return socket session
     * @deprecated use createSession(SessionType.TCP) instead.
     */
    public static Session createSocketSession() {
        return createSession(SessionType.TCP);
    }

    /**
     * Create a datagram session.
     * 
     * @return datagram session
     * @deprecated use createSession(SessionType.UDP) instead.
     */
    public static Session createDatagramSession() {
        return createSession(SessionType.UDP);
    }

    /**
     * Create a session acceptor.
     * 
     * @param type
     *            session type
     * @return session acceptor
     */
    public static SessionAcceptor createSessionAcceptor(SessionType type) {
        Class c = (Class) ACCEPTOR_MAP.get(type);
        if (c == null)
            throw new IllegalArgumentException("unsupported session type: "
                    + type);
        try {
            SessionAcceptor acceptor = (SessionAcceptor) c.newInstance();
            if (MBEAN_SERVER != null)
                MBEAN_SERVER.registerMBean(new JmxSessionAcceptor(acceptor),
                        getObjectName("SessionAcceptor-"
                                + ACCEPTOR_COUNTER.incrementAndGet()));
            return acceptor;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a session acceptor.
     * 
     * @return session acceptor
     * @deprecated use createSessionAcceptor(SessionType.TCP) instead.
     */
    public static SessionAcceptor createSessionAcceptor() {
        return createSessionAcceptor(SessionType.TCP);
    }

}
