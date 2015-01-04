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
package net.sf.cindy.session.dispatcher;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.sf.cindy.Session;
import net.sf.cindy.util.Configuration;
import net.sf.cindy.util.ElapsedTime;
import net.sf.cindy.util.LogThreadGroup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.mathcs.backport.java.util.concurrent.ArrayBlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.BlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;

/**
 * Default dispatcher implementation, support block operation such as
 * Future.complete.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class DefaultDispatcher implements Dispatcher {

    private static final Log log = LogFactory.getLog(DefaultDispatcher.class);

    private static final ThreadGroup THREAD_GROUP = new ThreadGroup(
            LogThreadGroup.CINDY_THREAD_GROUP, "Dispatcher");
    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final Dispatcher dispatcher = new DirectDispatcher();

    private final int capacity = Math.max(1, Configuration
            .getDispatcherCapacity());
    private final int keepAliveTime = Math.max(0, Configuration
            .getDispatcherKeepAliveTime());
    private final int concurrent = Math.max(1, Configuration
            .getDispatcherConcurrent());

    private final Object mainLock = new Object();
    private final List idleWorkers = new LinkedList();
    private final Map sessionMap = new WeakHashMap();

    private final Worker[] activeWorkers = new Worker[concurrent];
    private int currentConcurrent = 0;

    private int indexOf(Worker worker) {
        for (int i = 0; i < concurrent; i++) {
            if (activeWorkers[i] == worker)
                return i;
        }
        return -1;
    }

    /**
     * Dispatch session events.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    private class Worker extends Thread {

        private volatile BlockingQueue queue;
        private volatile boolean blocked;

        public Worker() {
            super(THREAD_GROUP, "Dispatcher-" + COUNTER.incrementAndGet());
        }

        private void setQueue(BlockingQueue queue) {
            this.queue = (queue == null ? new ArrayBlockingQueue(capacity)
                    : queue);
        }

        private Runnable getTask() {
            Runnable runnable = null;
            try {
                runnable = (Runnable) queue.poll(keepAliveTime,
                        TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            }

            // similar double-checked locking
            if (runnable == null) {
                synchronized (mainLock) {
                    runnable = (Runnable) queue.poll();
                    if (runnable == null) {
                        // thus getWorker method will not use this thread
                        blocked = true;

                        activeWorkers[indexOf(this)] = null;
                        currentConcurrent--;
                    }
                }
            }
            return runnable;
        }

        public void run() {
            for (Runnable task = null; (task = getTask()) != null; task = null) {
                try {
                    task.run();
                } catch (Throwable e) { // protect catch
                    log.error(e, e);
                }

                if (blocked) {
                    synchronized (mainLock) {
                        queue = null;
                        idleWorkers.add(this);

                        synchronized (this) {
                            try {
                                wait(keepAliveTime);
                            } catch (InterruptedException e) {
                            }
                        }

                        if (idleWorkers.remove(this)) // timeout
                            break;
                        else
                            blocked = false; // have new task
                    }
                }
            }
        }

    }

    private int index = -1; // choose worker index

    private Worker getWorker(Session session) {
        // get last worker
        Worker worker = (Worker) sessionMap.get(session);
        if (worker != null && !worker.blocked)
            return worker;

        if (currentConcurrent >= concurrent) {
            // reach concurrent limit
            index = (index + 1) % concurrent;
            worker = activeWorkers[index];
        } else {
            // create new worker
            worker = newWorker(null);
            activeWorkers[indexOf(null)] = worker;
            currentConcurrent++;
        }

        sessionMap.put(session, worker);
        return worker;
    }

    private Worker newWorker(BlockingQueue queue) {
        Worker worker = null;
        if (idleWorkers.isEmpty()) {
            worker = new Worker();
            worker.setQueue(queue);
            worker.start();
        } else { // reuse idle worker
            worker = (Worker) idleWorkers.remove(idleWorkers.size() - 1);
            worker.setQueue(queue);
            synchronized (worker) {
                worker.notify();
            }
        }
        return worker;
    }

    private final ElapsedTime elapsedTime = new ElapsedTime();

    public void dispatch(Session session, Runnable event) {
        Worker worker = null;
        synchronized (mainLock) {
            worker = getWorker(session);
        }
        if (Thread.currentThread() == worker) {
            dispatcher.dispatch(session, event);
        } else {
            BlockingQueue queue = worker.queue;
            if (!queue.offer(event)) {
                // flow control
                if (elapsedTime.getElapsedTime() >= 10000) {
                    elapsedTime.reset();
                    log.warn("dispatcher flow control");
                }
                try {
                    queue.put(event);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public void block() {
        Thread currentThread = Thread.currentThread();
        if (currentThread instanceof Worker) {
            Worker worker = (Worker) currentThread;
            worker.blocked = true;

            synchronized (mainLock) {
                Worker newWorker = newWorker(worker.queue);
                activeWorkers[indexOf(worker)] = newWorker;
            }
        } else {
            dispatcher.block();
        }
    }

}
