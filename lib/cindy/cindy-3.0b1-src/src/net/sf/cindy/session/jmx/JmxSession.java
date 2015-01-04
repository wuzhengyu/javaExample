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
package net.sf.cindy.session.jmx;

import java.net.SocketAddress;
import java.util.Map;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import net.sf.cindy.PacketDecoder;
import net.sf.cindy.PacketEncoder;
import net.sf.cindy.Session;
import net.sf.cindy.SessionFilter;
import net.sf.cindy.SessionHandler;
import net.sf.cindy.filter.StatisticFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>Session</code> which support jmx.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class JmxSession extends StandardMBean implements SessionMBean {

    private static final Log log = LogFactory.getLog(JmxSession.class);

    private final Session session;

    public JmxSession(Session session) throws NotCompliantMBeanException {
        super(SessionMBean.class);
        this.session = session;
    }

    private String getClassName(Object obj) {
        return obj == null ? null : obj.getClass().getName();
    }

    private Object newInstance(String className) {
        try {
            return Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getSessionType() {
        return session.getSessionType().toString();
    }

    public SocketAddress getLocalAddress() {
        return session.getLocalAddress();
    }

    public SocketAddress getRemoteAddress() {
        return session.getRemoteAddress();
    }

    public void setLocalAddress(SocketAddress address) {
        session.setLocalAddress(address);
    }

    public void setRemoteAddress(SocketAddress address) {
        session.setRemoteAddress(address);
    }

    public void setPacketDecoder(String decoderClassName) {
        session.setPacketDecoder((PacketDecoder) newInstance(decoderClassName));
    }

    public String getPacketDecoder() {
        return getClassName(session.getPacketDecoder());
    }

    public void setPacketEncoder(String encoderClassName) {
        session.setPacketEncoder((PacketEncoder) newInstance(encoderClassName));
    }

    public String getPacketEncoder() {
        return getClassName(session.getPacketEncoder());
    }

    public Map getAttributes() {
        return session.getAttributes();
    }

    public int getSessionTimeout() {
        return session.getSessionTimeout();
    }

    public void setSessionTimeout(int timeout) {
        session.setSessionTimeout(timeout);
    }

    public boolean isStarted() {
        return session.isStarted();
    }

    public void start() {
        session.start();
    }

    public void close() {
        session.close();
    }

    public String[] getSessionFilters() {
        SessionFilter[] filters = session.getSessionFilters();
        String[] result = new String[filters.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = getClassName(filters[i]);
        }
        return result;
    }

    public void removeSessionFilter(int index) {
        session.removeSessionFilter(session.getSessionFilter(index));
    }

    public void addSessionFilter(String filterClassName) {
        session.addSessionFilter((SessionFilter) newInstance(filterClassName));
    }

    public void addSessionFilter(int index, String filterClassName) {
        session.addSessionFilter(index,
                (SessionFilter) newInstance(filterClassName));
    }

    public String getSessionHandler() {
        return getClassName(session.getSessionHandler());
    }

    public void setSessionHandler(String handlerClassName) {
        session
                .setSessionHandler((SessionHandler) newInstance(handlerClassName));
    }

    private static final String[] STAT_NAMES = new String[] { "ReceivedBytes",
            "SentBytes", "ElapsedTime", "AvgReceiveSpeed", "AvgSendSpeed",
            "ReceiveSpeed", "SendSpeed" };
    private static final String[] STAT_DESCS = STAT_NAMES;
    private static final OpenType[] STAT_TYPES = new OpenType[] {
            SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
            SimpleType.DOUBLE, SimpleType.DOUBLE, SimpleType.DOUBLE,
            SimpleType.DOUBLE };

    private static final CompositeType STAT_COMPOSITE_TYPE;

    static {
        CompositeType type = null;
        try {
            type = new CompositeType("cindy.statistic", "statistic",
                    STAT_NAMES, STAT_DESCS, STAT_TYPES);
        } catch (OpenDataException e) {
            log.error(e, e);
        }
        STAT_COMPOSITE_TYPE = type;
    }

    public CompositeData getStatistic() {
        SessionFilter[] filters = session.getSessionFilters();
        for (int i = 0; i < filters.length; i++) {
            if (filters[i] instanceof StatisticFilter) {
                StatisticFilter stat = (StatisticFilter) filters[i];
                try {
                    return new CompositeDataSupport(STAT_COMPOSITE_TYPE,
                            STAT_NAMES, new Object[] {
                                    new Long(stat.getReceivedBytes()),
                                    new Long(stat.getSentBytes()),
                                    new Long(stat.getElapsedTime()),
                                    new Double(stat.getAvgReceiveSpeed()),
                                    new Double(stat.getAvgSendSpeed()),
                                    new Double(stat.getReceiveSpeed()),
                                    new Double(stat.getSendSpeed()) });
                } catch (OpenDataException e) {
                    log.error(e, e);
                }
            }
        }
        return null;
    }

}
