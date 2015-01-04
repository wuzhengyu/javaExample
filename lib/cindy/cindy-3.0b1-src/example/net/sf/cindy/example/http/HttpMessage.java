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
package net.sf.cindy.example.http;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Http message.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public abstract class HttpMessage {

    private static final byte[] EMPTY_CONTENT = new byte[0];

    private final Map params = new LinkedHashMap();
    private String version = "HTTP/1.0";
    private byte[] content = EMPTY_CONTENT;

    public final String getVersion() {
        return version;
    }

    public final void setVersion(String version) {
        this.version = version;
    }

    public final void setParam(String key, String value) {
        if (key != null) {
            if (value == null)
                params.remove(key);
            else
                params.put(key, value);
        }
    }

    public final String getParam(String key) {
        return key == null ? null : (String) params.get(key);
    }

    public final Map getParams() {
        return Collections.unmodifiableMap(params);
    }

    public final byte[] getContent() {
        return content;
    }

    public final void setContent(byte[] content) {
        if (content == null)
            content = EMPTY_CONTENT;
        this.content = content;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (Iterator iter = params.entrySet().iterator(); iter.hasNext();) {
            Entry entry = (Entry) iter.next();
            buffer.append(entry.getKey()).append(": ").append(entry.getValue())
                    .append("\r\n");
        }
        buffer.append("\r\n");
        return buffer.toString();
    }
}
