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
 * Session type.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public final class SessionType {

    /**
     * TCP session.
     */
    public static final SessionType TCP = new SessionType("TCP");

    /**
     * UDP session.
     */
    public static final SessionType UDP = new SessionType("UDP");

    /**
     * Pipe session.
     */
    public static final SessionType PIPE = new SessionType("Pipe");

    /**
     * File session.
     */
    public static final SessionType FILE = new SessionType("File");

    /**
     * Unknown session type.
     */
    public static final SessionType UNKNOWN = new SessionType("Unknown");

    private final String type;

    private SessionType(String type) {
        this.type = type;
    }

    public String toString() {
        return type;
    }
}
