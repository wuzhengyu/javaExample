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
package net.sf.cindy.packet;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import net.sf.cindy.Buffer;
import net.sf.cindy.Packet;
import net.sf.cindy.buffer.BufferFactory;

/**
 * Default implementation of <code>Packet</code>.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class DefaultPacket implements Packet {

    private Buffer content;
    private SocketAddress address;

    public DefaultPacket() {
    }

    public DefaultPacket(Buffer content) {
        this.content = content;
    }

    public DefaultPacket(Buffer content, SocketAddress address) {
        this.content = content;
        this.address = address;
    }

    public DefaultPacket(ByteBuffer content) {
        this(BufferFactory.wrap(content));
    }

    public DefaultPacket(byte[] content) {
        this(BufferFactory.wrap(content));
    }

    public void setContent(Buffer content) {
        this.content = content;
    }

    public void setAddress(SocketAddress address) {
        this.address = address;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public Buffer getContent() {
        return content;
    }

    public String toString() {
        return "Packet [content] " + content + " [address] " + address;
    }

}
