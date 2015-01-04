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
package net.sf.cindy.example.http.handler;

import net.sf.cindy.Future;
import net.sf.cindy.FutureListener;
import net.sf.cindy.Session;
import net.sf.cindy.SessionHandlerAdapter;
import net.sf.cindy.example.http.HttpRequest;
import net.sf.cindy.example.http.HttpResponse;
import net.sf.cindy.util.Charset;

/**
 * Echo http request.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class EchoHandler extends SessionHandlerAdapter {

    public void objectReceived(Session session, Object obj) throws Exception {
        HttpRequest request = (HttpRequest) obj;

        byte[] header = Charset.UTF8.encodeToArray(request.toString());
        boolean keepAlive = "keep-alive".equalsIgnoreCase(request
                .getParam("Connection"));

        HttpResponse response = new HttpResponse();
        response.setStatusCode(200);
        response.setVersion(request.getVersion());
        response.setReasonPhrase("OK");
        response.setParam("Server", "Cindy Http Server");
        response.setParam("Content-Type", "text/plain");
        response.setParam("Content-Length", String.valueOf(header.length));
        response.setParam("Connection", keepAlive ? "keep-alive" : "close");
        response.setContent(header);

        Future future = session.send(response);
        if (!keepAlive)
            future.addListener(new FutureListener() {

                public void futureCompleted(Future future) throws Exception {
                    future.getSession().close();
                }
            });
    }

    public void exceptionCaught(Session session, Throwable cause) {
        session.close();
        System.err.println(cause);
    }
}
