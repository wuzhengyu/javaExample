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
package net.sf.cindy.session.nio.reactor;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;

import net.sf.cindy.util.ChannelUtils;
import net.sf.cindy.util.Configuration;
import net.sf.cindy.util.ElapsedTime;
import net.sf.cindy.util.LogThreadGroup;
import net.sf.cindy.util.NamedThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.mathcs.backport.java.util.Queue;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentLinkedQueue;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadFactory;

/**
 * Default reactor implementation.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class DefaultReactor implements Reactor {

    private static final Log log = LogFactory.getLog(DefaultReactor.class);

    private static final ThreadFactory reactorThreadFactory = new NamedThreadFactory(
            new ThreadGroup(LogThreadGroup.CINDY_THREAD_GROUP, "Reactor"),
            false, "Reactor");

    private static final int SELECT_TIMEOUT = 1000;

    /**
     * All operate need be done in the same thread.
     */
    private final Queue registerColl, deregisterColl, interestColl;

    /**
     * The attachment associted with inner <code>SelectionKey</code>.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    private static class Attachment {

        private final ReactorHandler handler;

        private int idleTime; // store session idle time
        private boolean writing; // do not check session timeout when writing

        public Attachment(ReactorHandler handler) {
            this.handler = handler;
        }

    }

    /**
     * Saved all registed <code>ReactorHandlerAttachment</code>. Key is
     * <code>ReactorHandler</code>, Value is <code>Attachment</code>.
     */
    private final Map registered = new ConcurrentHashMap();

    private Selector selector;
    private Thread selectThread = null;
    private volatile boolean close = false;

    /**
     * when no session registed on the reator, don't stop select immediately.
     */
    private static final int CLOSE_TICK = Configuration
            .getDispatcherKeepAliveTime()
            / SELECT_TIMEOUT;
    private int currentTick = 0;

    /**
     * judge session timeout.
     */
    private ElapsedTime lastSelectTime;

    public DefaultReactor() {
        registerColl = new ConcurrentLinkedQueue();
        deregisterColl = new ConcurrentLinkedQueue();
        interestColl = new ConcurrentLinkedQueue();
    }

    private synchronized void start() {
        if (selectThread != null)
            return;

        lastSelectTime = new ElapsedTime();
        try {
            selector = Selector.open();
            selectThread = reactorThreadFactory.newThread(new Runnable() {

                public void run() {
                    try {
                        while (!close) {
                            beforeSelect();
                            if (close) // after beforeSelect, close may be true
                                break;
                            try {
                                selector.select(SELECT_TIMEOUT);
                            } catch (IOException e) {
                                log.error(e, e);
                                break;
                            }
                            afterSelect();
                        }
                    } finally {
                        DefaultReactor.this.stop();
                    }
                }
            });
            selectThread.start();
        } catch (IOException e) {
            ChannelUtils.close(selector);
            log.error(e, e);
        }
    }

    private synchronized void stop() {
        for (Iterator iter = registered.keySet().iterator(); iter.hasNext();) {
            ReactorHandler handler = (ReactorHandler) iter.next();
            dispatchDeregistered(handler);
        }
        registered.clear();
        lastSelectTime = null;
        ChannelUtils.close(selector);
        selector = null;
        registerColl.clear();
        interestColl.clear();
        deregisterColl.clear();
        currentTick = 0;
        close = false;
        selectThread = null;
    }

    private void dispatchDeregistered(ReactorHandler handler) {
        SelectableChannel[] channels = handler.getChannels();
        for (int i = 0; i < channels.length; i++) {
            SelectionKey key = channels[i].keyFor(selector);
            if (key != null)
                key.cancel();
        }
        handler.onDeregistered();
    }

    public void register(ReactorHandler handler) {
        if (Thread.currentThread() == selectThread) {
            changeRegister(new Attachment(handler));
        } else {
            registerColl.offer(new Attachment(handler));
            start(); // auto start when register
            selector.wakeup();
        }
    }

    public void deregister(ReactorHandler handler) {
        if (Thread.currentThread() == selectThread) {
            changeDeregister(handler);
        } else {
            deregisterColl.offer(handler);
            selector.wakeup();
        }
    }

    /**
     * Store reactor handler and interest ops.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    private static class ReactorHandlerInterest {

        private final ReactorHandler handler;
        private final int ops;

        public ReactorHandlerInterest(ReactorHandler handler, int ops) {
            this.handler = handler;
            this.ops = ops;
        }

    }

    public void interest(ReactorHandler handler, int ops) {
        if (Thread.currentThread() == selectThread) {
            changeInterest(new ReactorHandlerInterest(handler, ops));
        } else {
            interestColl.offer(new ReactorHandlerInterest(handler, ops));
            selector.wakeup();
        }
    }

    protected void beforeSelect() {
        Attachment attachment = null;
        while ((attachment = (Attachment) registerColl.poll()) != null)
            changeRegister(attachment);

        ReactorHandlerInterest interest = null;
        while ((interest = (ReactorHandlerInterest) interestColl.poll()) != null)
            changeInterest(interest);

        ReactorHandler handler = null;
        while ((handler = (ReactorHandler) deregisterColl.poll()) != null)
            changeDeregister(handler);

        checkNeedStop();
    }

    private void changeRegister(Attachment attachment) {
        ReactorHandler handler = attachment.handler;
        if (registered.containsKey(handler))
            return;
        SelectableChannel[] channels = handler.getChannels();
        try {
            for (int i = 0; i < channels.length; i++) {
                SelectableChannel channel = channels[i];
                channel.configureBlocking(false);
                int validOps = channel.validOps();
                // It's a bug of java nio, see:
                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4960791
                boolean isConnected = (validOps & SelectionKey.OP_CONNECT) != 0
                        && ((SocketChannel) channel).isConnected();
                channel.register(selector, isConnected ? SelectionKey.OP_READ
                        : (validOps & ~SelectionKey.OP_WRITE), attachment);
            }
            registered.put(handler, attachment);
            handler.onRegistered();
        } catch (IOException e) {
            log.error(e, e);
            dispatchDeregistered(handler);
        }
    }

    private void changeInterest(ReactorHandlerInterest interest) {
        ReactorHandler handler = interest.handler;
        Attachment attachment = (Attachment) registered.get(handler);
        if (attachment == null)
            return;

        // write completed
        if ((interest.ops & OP_NON_WRITE) != 0)
            attachment.writing = false;

        SelectableChannel[] channels = handler.getChannels();
        for (int i = 0; i < channels.length; i++) {
            SelectableChannel channel = channels[i];
            SelectionKey key = channel.keyFor(selector);
            if (key != null)
                key.interestOps(key.interestOps()
                        | (channel.validOps() & interest.ops));
        }
    }

    private void changeDeregister(ReactorHandler handler) {
        if (registered.remove(handler) != null)
            dispatchDeregistered(handler);
    }

    private void checkNeedStop() {
        if (registered.isEmpty()) {
            if (++currentTick >= CLOSE_TICK)
                close = true;
        } else
            currentTick = 0;
    }

    protected void afterSelect() {
        checkSessionTimeout();
        processSelectedKeys();
    }

    private void checkSessionTimeout() {
        // reset active handler
        for (Iterator iter = selector.selectedKeys().iterator(); iter.hasNext();) {
            SelectionKey key = (SelectionKey) iter.next();
            Attachment attachment = (Attachment) key.attachment();
            attachment.idleTime = 0;
        }

        // imporve performance, do not check session timeout frequently
        if (lastSelectTime.getElapsedTime() < SELECT_TIMEOUT)
            return;
        int interval = (int) lastSelectTime.reset();

        // check timeout
        Loop: for (Iterator iter = registered.values().iterator(); iter
                .hasNext();) {
            Attachment attachment = (Attachment) iter.next();
            ReactorHandler handler = attachment.handler;
            int sessionTimeout = handler.getSession().getSessionTimeout();
            if (sessionTimeout <= 0 || attachment.writing) {
                attachment.idleTime = 0;
                continue;
            }

            SelectableChannel[] channels = handler.getChannels();
            for (int i = 0; i < channels.length; i++) {
                SelectionKey key = channels[i].keyFor(selector);
                if ((key.interestOps() & key.readyOps()) != 0) {
                    continue Loop;
                }
            }

            // no event happen
            int newIdleTime = interval + attachment.idleTime;
            if (newIdleTime > sessionTimeout) {
                attachment.idleTime = 0;
                handler.onTimeout();
            } else
                attachment.idleTime = newIdleTime;
        }
    }

    private void processSelectedKeys() {
        for (Iterator iter = selector.selectedKeys().iterator(); iter.hasNext();) {
            SelectionKey key = (SelectionKey) iter.next();
            iter.remove();

            key.interestOps(key.interestOps() & ~key.readyOps());
            Attachment attachment = (Attachment) key.attachment();
            ReactorHandler handler = attachment.handler;

            if (key.isWritable()) // do not check session timeout when writing
                attachment.writing = true;

            if (key.isAcceptable())
                handler.onAcceptable();
            if (key.isConnectable())
                handler.onConnectable();
            if (key.isValid() && key.isReadable())
                handler.onReadable();
            if (key.isValid() && key.isWritable())
                handler.onWritable();
        }
    }
}
