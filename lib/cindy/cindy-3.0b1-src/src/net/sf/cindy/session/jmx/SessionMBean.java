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

import javax.management.openmbean.CompositeData;

/**
 * <code>Session</code> MBean.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public interface SessionMBean {
    
    String getSessionType();

    void setRemoteAddress(SocketAddress address);

    SocketAddress getRemoteAddress();

    void setLocalAddress(SocketAddress address);

    SocketAddress getLocalAddress();

    void setPacketDecoder(String decoderClassName);

    String getPacketDecoder();

    void setPacketEncoder(String encoderClassName);

    String getPacketEncoder();

    Map getAttributes();

    void setSessionTimeout(int timeout);

    int getSessionTimeout();

    boolean isStarted();

    void start();

    void close();

    String[] getSessionFilters();

    void removeSessionFilter(int index);

    void addSessionFilter(String filterClassName);

    void addSessionFilter(int index, String filterClassName);

    String getSessionHandler();

    void setSessionHandler(String handlerClassName);

    CompositeData getStatistic();
}
