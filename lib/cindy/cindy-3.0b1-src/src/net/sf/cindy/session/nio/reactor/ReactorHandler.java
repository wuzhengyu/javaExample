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

import java.nio.channels.SelectableChannel;

import net.sf.cindy.Session;

/**
 * Reactor handler.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public interface ReactorHandler {

    Session getSession();

    SelectableChannel[] getChannels();

    void onRegistered();

    void onDeregistered();

    void onReadable();

    void onWritable();

    void onAcceptable();

    void onConnectable();

    void onTimeout();

}
