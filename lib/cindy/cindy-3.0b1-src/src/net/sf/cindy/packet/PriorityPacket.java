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

import net.sf.cindy.Packet;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of <code>Packet</code>, compared by priority and create
 * time.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class PriorityPacket extends DelegatePacket implements Comparable {

    private static final AtomicLong TIMESTAMP = new AtomicLong();

    private final int priority;
    private final long createTime;

    public PriorityPacket(Packet packet, int priority) {
        super(packet);
        this.priority = priority;
        createTime = TIMESTAMP.getAndIncrement();
    }

    public int compareTo(Object obj) {
        PriorityPacket packet = (PriorityPacket) obj;
        int sp = packet.priority;
        return priority < sp ? -1 : (priority > sp ? 1
                : (int) (createTime - packet.createTime));
    }

    public String toString() {
        return super.toString() + " [priority] " + priority;
    }
}
