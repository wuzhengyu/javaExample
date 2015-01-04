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
package net.sf.cindy.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Log uncaught exception.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class LogThreadGroup extends ThreadGroup {

    private static final Log log = LogFactory.getLog(LogThreadGroup.class);

    /**
     * Cindy kernel thread group. All kernel threads should belong to this
     * thread group or it's sub thread group.
     */
    public static final ThreadGroup CINDY_THREAD_GROUP = new LogThreadGroup(
            "Cindy");

    public LogThreadGroup(String name) {
        super(name);
    }

    public LogThreadGroup(ThreadGroup parent, String name) {
        super(parent, name);
    }

    public void uncaughtException(Thread t, Throwable e) {
        log.error(t, e);
        super.uncaughtException(t, e);
    }

}
