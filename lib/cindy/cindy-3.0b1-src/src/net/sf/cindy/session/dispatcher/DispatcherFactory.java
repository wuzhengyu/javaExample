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

import net.sf.cindy.util.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>Dispatcher</code> factory.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class DispatcherFactory {

    private static final Log log = LogFactory.getLog(DispatcherFactory.class);

    private static final Dispatcher dispatcher;

    static {
        String className = Configuration.getDispatcher();
        Dispatcher tempDispatcher = null;
        if (className != null)
            try {
                tempDispatcher = (Dispatcher) Class.forName(className)
                        .newInstance();
            } catch (Exception e) {
                log.error(e);
            }
        dispatcher = tempDispatcher == null ? new DefaultDispatcher()
                : tempDispatcher;
    }

    public static Dispatcher getDispatcher() {
        return dispatcher;
    }
}
