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

import java.util.regex.Pattern;

import net.sf.cindy.Buffer;
import net.sf.cindy.Packet;
import net.sf.cindy.PacketDecoder;
import net.sf.cindy.Session;
import net.sf.cindy.util.Charset;

/**
 * <code>HttpRequest</code> Decoder.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class HttpRequestDecoder implements PacketDecoder {

    private static final byte[] TOKEN = "\r\n\r\n".getBytes();
    private static final Pattern PATTERN = Pattern.compile(" ");

    public Object decode(Session session, Packet packet) throws Exception {
        Buffer buffer = packet.getContent();
        int index = buffer.indexOf(TOKEN);
        if (index >= 0) {
            String[] headers = buffer.getString(Charset.UTF8,
                    index + TOKEN.length).split("\r\n");

            HttpRequest request = new HttpRequest();
            parse(request, headers);

            String contentLen = request.getParam("Content-Length");
            if (contentLen != null) {
                int len = Integer.parseInt(contentLen);
                if (buffer.remaining() >= len) {
                    byte[] content = new byte[len];
                    buffer.get(content);
                    request.setContent(content);
                } else {
                    return null;
                }
            }
            return request;
        }
        return null;
    }

    private void parse(HttpRequest request, String[] headers) {
        // every time use String.split will construct a new Pattern object
        String[] s = PATTERN.split(headers[0]);
        request.setRequestMethod(s[0]);
        request.setRequestURI(s[1]);
        request.setVersion(s[2]);

        for (int i = 1; i < headers.length; i++) {
            int index = headers[i].indexOf(":");
            request.setParam(headers[i].substring(0, index).trim(), headers[i]
                    .substring(index + 1).trim());
        }
    }
}
