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

import java.io.IOException;
import java.net.SocketAddress;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import net.sf.cindy.SessionAcceptor;

/**
 * <code>SessionAcceptor</code> which support jmx.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class JmxSessionAcceptor extends StandardMBean implements
        SessionAcceptorMBean {

    private final SessionAcceptor acceptor;

    public JmxSessionAcceptor(SessionAcceptor acceptor)
            throws NotCompliantMBeanException {
        super(SessionAcceptorMBean.class);
        this.acceptor = acceptor;
    }

    public String getSessionType() {
        return acceptor.getSessionType().toString();
    }

    public void close() {
        acceptor.close();
    }

    public SocketAddress getListenAddress() {
        return acceptor.getListenAddress();
    }

    public int getListenPort() {
        return acceptor.getListenPort();
    }

    public boolean isStarted() {
        return acceptor.isStarted();
    }

    public void setListenAddress(SocketAddress address) {
        acceptor.setListenAddress(address);
    }

    public void setListenPort(int port) {
        acceptor.setListenPort(port);
    }

    public void start() throws IOException {
        acceptor.start();
    }

    public int getBacklog() {
        return acceptor.getBacklog();
    }

    public boolean isReuseAddress() {
        return acceptor.isReuseAddress();
    }

    public void setBacklog(int backlog) {
        acceptor.setBacklog(backlog);
    }

    public void setReuseAddress(boolean b) {
        acceptor.setReuseAddress(b);
    }

    public int getAcceptedCount() {
        return acceptor.getAcceptedCount();
    }

}
