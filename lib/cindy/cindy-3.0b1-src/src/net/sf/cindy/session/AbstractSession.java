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

import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.cindy.Future;
import net.sf.cindy.Packet;
import net.sf.cindy.PacketDecoder;
import net.sf.cindy.PacketEncoder;
import net.sf.cindy.Session;
import net.sf.cindy.SessionFilter;
import net.sf.cindy.SessionFilterChain;
import net.sf.cindy.SessionHandler;
import net.sf.cindy.decoder.SimplePacketDecoder;
import net.sf.cindy.encoder.SimplePacketEncoder;
import net.sf.cindy.filter.DispatcherFilter;
import net.sf.cindy.filter.PacketDecoderFilter;
import net.sf.cindy.filter.SessionHandlerFilter;
import net.sf.cindy.session.dispatcher.DispatcherFactory;
import net.sf.cindy.util.Configuration;

/**
 * Abstract session.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public abstract class AbstractSession implements Session {

    private static final SessionFilter[] EMPTY_FILTERS = new SessionFilter[0];

    private final Map attributes = Collections.synchronizedMap(new HashMap(0));
    private volatile SessionFilter[] filters = EMPTY_FILTERS;

    private SocketAddress localAddress;
    private SocketAddress remoteAddress;

    private PacketEncoder encoder = new SimplePacketEncoder();
    private PacketDecoder decoder = new SimplePacketDecoder();

    private SessionHandler handler;
    private int readPacketSize = Configuration.getReadPacketSize();
    private int sessionTimeout = Configuration.getSessionTimeout();

    public void setRemoteAddress(SocketAddress address) {
        if (isStarted())
            throw new IllegalStateException(
                    "can't set remote address after session started");
        this.remoteAddress = address;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setLocalAddress(SocketAddress address) {
        if (isStarted())
            throw new IllegalStateException(
                    "can't set local address after session started");
        this.localAddress = address;
    }

    public SocketAddress getLocalAddress() {
        return localAddress;
    }

    public PacketDecoder getPacketDecoder() {
        return decoder;
    }

    public PacketEncoder getPacketEncoder() {
        return encoder;
    }

    public void setPacketDecoder(PacketDecoder decoder) {
        if (decoder != null)
            this.decoder = decoder;
    }

    public void setPacketEncoder(PacketEncoder encoder) {
        if (encoder != null)
            this.encoder = encoder;
    }

    public int getReadPacketSize() {
        return readPacketSize;
    }

    public void setReadPacketSize(int size) {
        if (readPacketSize > 0)
            this.readPacketSize = size;
    }

    public Map getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public Object getAttribute(Object key) {
        return attributes.get(key);
    }

    public boolean containsAttribute(Object key) {
        return attributes.containsKey(key);
    }

    public void setAttribute(Object key, Object attribute) {
        attributes.put(key, attribute);
    }

    public void removeAttribute(Object key) {
        attributes.remove(key);
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int timeout) {
        this.sessionTimeout = Math.max(0, timeout);
    }

    public void setSessionHandler(SessionHandler handler) {
        this.handler = handler;
    }

    public SessionHandler getSessionHandler() {
        return handler;
    }

    public synchronized void addSessionFilter(SessionFilter filter) {
        addSessionFilter(filters.length, filter);
    }

    public synchronized void addSessionFilter(int index, SessionFilter filter) {
        if (filter != null) {
            index = Math.max(0, Math.min(filters.length, index));
            SessionFilter[] newFilters = new SessionFilter[filters.length + 1];
            System.arraycopy(filters, 0, newFilters, 0, index);
            System.arraycopy(filters, index, newFilters, index + 1,
                    filters.length - index);
            newFilters[index] = filter;
            filters = newFilters;
        }
    }

    public synchronized void removeSessionFilter(SessionFilter filter) {
        if (filter != null) {
            for (int i = 0; i < filters.length; i++) {
                if (filters[i].equals(filter)) {
                    SessionFilter[] newFilters = new SessionFilter[filters.length - 1];
                    System.arraycopy(filters, 0, newFilters, 0, i);
                    System.arraycopy(filters, i + 1, newFilters, i,
                            newFilters.length - i);
                    filters = newFilters;
                }
            }
        }
    }

    public SessionFilter getSessionFilter(int index) {
        if (index >= 0 && index < filters.length)
            return filters[index];
        return null;
    }

    public SessionFilter[] getSessionFilters() {
        SessionFilter[] filters = this.filters;
        SessionFilter[] result = new SessionFilter[filters.length];
        System.arraycopy(filters, 0, result, 0, filters.length);
        return result;
    }

    public Future flush(Packet packet) {
        return flush(packet, 0);
    }

    public Future flush(Packet packet, int priority) {
        return send(null, packet, priority);
    }

    public Future send(Object obj) {
        return send(obj, 0);
    }

    public Future send(Object obj, int priority) {
        Packet packet = null;
        try {
            packet = encoder.encode(this, obj);
        } catch (Exception e) {
            dispatchException(e);
            return new DefaultFuture(this, false);
        }
        return send(obj, packet, priority);
    }

    protected abstract Future send(Object obj, Packet packet, int priority);

    /**
     * Dispatch event to <code>SessionHandler</code>. The last filter in the
     * chain.
     */
    private static final SessionHandlerFilter SESSION_HANDLER_FILTER = new SessionHandlerFilter();

    /**
     * Dispatch event in event dispatch thread.
     */
    private static final DispatcherFilter DISPATCH_FILTER = new DispatcherFilter(
            DispatcherFactory.getDispatcher());

    private final SessionFilter packetDecoderFilter = PacketDecoderFilter
            .getInstance(this);

    /**
     * Default SessionFilterChain implementation. Dispatch event in such order:
     * <p>
     * DispatchFilter --> ApplicationFilters --> OperateFilter --> DecodeFilter
     * --> SessionHandlerFilter
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     */
    private class DefaultSessionFilterChain extends AbstractSessionFilterChain {

        private final boolean reversed;

        private final SessionFilter[] appFilters = filters;
        private int cursor = -1;

        private SessionFilter dispatchFilter = DISPATCH_FILTER;
        private SessionFilter operateFilter;
        private SessionFilter decodeFilter = packetDecoderFilter;
        private SessionFilter handlerFilter = SESSION_HANDLER_FILTER;

        public DefaultSessionFilterChain(boolean reversed) {
            this(null, reversed);
        }

        public DefaultSessionFilterChain(SessionFilter operateFilter,
                boolean reversed) {
            this.operateFilter = operateFilter;
            this.reversed = reversed;
            this.cursor = reversed ? appFilters.length : -1;
        }

        public Session getSession() {
            return AbstractSession.this;
        }

        protected SessionFilter nextFilter() {
            SessionFilter filter = null;
            if (dispatchFilter != null) {
                filter = dispatchFilter;
                dispatchFilter = null;
            } else if ((reversed && --cursor >= 0)
                    || (!reversed && ++cursor < appFilters.length)) {
                filter = appFilters[cursor];
            } else if (operateFilter != null) {
                filter = operateFilter;
                operateFilter = null;
            } else if (decodeFilter != null) {
                filter = decodeFilter;
                decodeFilter = null;
            } else {
                filter = handlerFilter;
                handlerFilter = null;
            }
            return filter;
        }

        public void exceptionCaught(Throwable cause) {
            if (!(Configuration.isDisableInnerException() && cause instanceof SessionException))
                super.exceptionCaught(cause);
        }

    }

    public SessionFilterChain getSessionFilterChain(boolean reversed) {
        return new DefaultSessionFilterChain(reversed);
    }

    protected SessionFilterChain getSessionFilterChain(
            SessionFilter operateFilter, boolean reversed) {
        return new DefaultSessionFilterChain(operateFilter, reversed);
    }

    protected void dispatchException(Throwable throwable) {
        getSessionFilterChain(false).exceptionCaught(throwable);
    }

}
