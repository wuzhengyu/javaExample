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
 * Decode <code>Packet</code> to object.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public interface PacketDecoder {

    /**
     * Decode packet to object. The content of the packet is readonly, and it's
     * position will not be changed if the method return null.
     * 
     * @param session
     *            session
     * @param packet
     *            packet
     * @return decoded object
     * @throws Exception
     *             any exception
     */
    Object decode(Session session, Packet packet) throws Exception;

}
