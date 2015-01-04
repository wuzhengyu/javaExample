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

import net.sf.cindy.Session;
import net.sf.cindy.util.LogThreadGroup;
import edu.emory.mathcs.backport.java.util.LinkedList;
import edu.emory.mathcs.backport.java.util.Queue;

/**
 * Direct dispatcher implementation, do not support block operation such as
 * Future.complete.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class DirectDispatcher implements Dispatcher {

    private final ThreadLocal local = new ThreadLocal();

    public void block() {
        if (LogThreadGroup.CINDY_THREAD_GROUP.parentOf(Thread.currentThread()
                .getThreadGroup()))// can't block kernel thread
            throw new IllegalStateException("can't block kernel thread");
    }

    private Queue getQueue() {
        Queue queue = (Queue) local.get();
        if (queue == null) {
            queue = new LinkedList();
            local.set(queue);
        }
        return queue;
    }

    public void dispatch(Session session, Runnable event) {
        Queue queue = getQueue();
        queue.add(event); // enqueue

        if (queue.size() == 1) {
            event.run();
            queue.poll(); // dequeue current runnable
            for (Runnable task = null; (task = (Runnable) queue.peek()) != null; queue
                    .poll())
                task.run();
        }
    }
}
