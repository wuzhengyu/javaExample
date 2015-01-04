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
package net.sf.cindy;

/**
 * A Future represents the result of an asynchronous task.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public interface Future {

    /**
     * Get session associated with the future.
     * 
     * @return session
     */
    Session getSession();

    /**
     * Waits if necessary for the task to complete. Warn: Block operate may lead
     * to artificially low throughput.
     * 
     * @return is succeeded
     */
    boolean complete();

    /**
     * Waits if necessary for the task to complete or the timeout period has
     * expired. Warn: Block operate may lead to artificially low throughput.
     * 
     * @param timeout
     *            timeout
     * @return is completed
     */
    boolean complete(int timeout);

    /**
     * Return true if the task completed.
     * 
     * @return is completed
     */
    boolean isCompleted();

    /**
     * Return true if the task completed successful.
     * 
     * @return is succeeded
     */
    boolean isSucceeded();

    /**
     * Add future listener.
     * 
     * @param listener
     *            future listener
     */
    void addListener(FutureListener listener);

    /**
     * Remove future listener.
     * 
     * @param listener
     *            future listener
     */
    void removeListener(FutureListener listener);
}
