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

import java.nio.channels.SelectionKey;

/**
 * NIO Reactor.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public interface Reactor {

    void register(ReactorHandler handler);

    void deregister(ReactorHandler handler);

    void interest(ReactorHandler handler, int ops);

    int OP_READ = SelectionKey.OP_READ;
    int OP_NON_WRITE = 1 << 1;
    int OP_WRITE = SelectionKey.OP_WRITE;
    int OP_CONNECT = SelectionKey.OP_CONNECT;
    int OP_ACCEPT = SelectionKey.OP_ACCEPT;
}
