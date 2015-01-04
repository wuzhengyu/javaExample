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

import net.sf.cindy.Future;
import net.sf.cindy.FutureListener;
import net.sf.cindy.Session;
import net.sf.cindy.session.dispatcher.Dispatcher;
import net.sf.cindy.session.dispatcher.DispatcherFactory;
import net.sf.cindy.util.ElapsedTime;
import edu.emory.mathcs.backport.java.util.LinkedList;
import edu.emory.mathcs.backport.java.util.Queue;

/**
 * Default future implementation.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class DefaultFuture implements Future {

    private final Session session;
    private final Dispatcher dispatcher = DispatcherFactory.getDispatcher();

    private volatile Queue listeners; // lazy load

    private volatile boolean completed = false;
    private volatile boolean succeeded = false;

    public DefaultFuture(Session session) {
        this.session = session;
    }

    public DefaultFuture(Session session, boolean succeeded) {
        this.session = session;
        completed = true;
        this.succeeded = succeeded;
    }

    public Session getSession() {
        return session;
    }

    protected void caughtException(Throwable throwable) {
        session.getSessionFilterChain(false).exceptionCaught(throwable);
    }

    private void dispatchFutureCompleted(final FutureListener listener) {
        dispatcher.dispatch(session, new Runnable() {

            private void futureCompleted(FutureListener listener) {
                try {
                    listener.futureCompleted(DefaultFuture.this);
                } catch (Throwable e) {
                    caughtException(e);
                }
            }

            public void run() {
                if (listener != null)
                    futureCompleted(listener);
                else if (listeners != null) {
                    for (FutureListener listener = null; (listener = (FutureListener) listeners
                            .poll()) != null;) {
                        futureCompleted(listener);
                    }
                }
            }
        });
    }

    public void addListener(FutureListener listener) {
        if (listener != null) {
            // double-checked locking, it can work for 32-bit primitive values
            if (completed) {
                dispatchFutureCompleted(listener);
            } else {
                synchronized (this) {
                    if (completed)
                        dispatchFutureCompleted(listener);
                    else {
                        if (listeners == null) // lazy load
                            listeners = new LinkedList();
                        listeners.add(listener);
                    }
                }
            }
        }
    }

    public void removeListener(FutureListener listener) {
        if (!completed) { // double-checked locking
            synchronized (this) {
                if (!completed && listeners != null)
                    listeners.remove(listener);
            }
        }
    }

    public boolean complete() {
        if (!completed) {
            synchronized (this) {
                while (!completed) {
                    dispatcher.block(); // block dispatcher
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        return succeeded;
    }

    public boolean complete(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException();
        if (!completed) {
            ElapsedTime startTime = new ElapsedTime();
            synchronized (this) {
                while (!completed) {
                    long waitTime = timeout - startTime.getElapsedTime();
                    if (waitTime <= 0)
                        break;
                    dispatcher.block(); // block dispatcher
                    try {
                        wait(waitTime);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        return completed;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public synchronized void setSucceeded(boolean succeeded) {
        if (completed)
            throw new IllegalStateException(
                    "can't change the state of a completed future");
        completed = true;
        this.succeeded = succeeded;

        notifyAll();

        dispatchFutureCompleted(null);
    }
}
