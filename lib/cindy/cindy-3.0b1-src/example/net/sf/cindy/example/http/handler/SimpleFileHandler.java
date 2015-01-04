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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Date;

import net.sf.cindy.Future;
import net.sf.cindy.FutureListener;
import net.sf.cindy.Session;
import net.sf.cindy.SessionHandlerAdapter;
import net.sf.cindy.example.http.HttpRequest;
import net.sf.cindy.example.http.HttpResponse;
import net.sf.cindy.packet.DefaultPacket;
import net.sf.cindy.util.Charset;

/**
 * Simple file handler. It is not production quality.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class SimpleFileHandler extends SessionHandlerAdapter {

    private final FileNameMap mimeMap = URLConnection.getFileNameMap();

    private final byte[] NOT_FOUND = "Not Found".getBytes();

    protected ByteBuffer getContent(String uri) throws IOException {
        return getContent(uri, getFile(uri));
    }

    protected File getFile(String uri) {
        if (uri.startsWith("/"))
            return new File("." + uri);
        else if (uri.length() == 0)
            return new File(".");
        else
            return new File(uri);
    }

    protected ByteBuffer getContent(String uri, File file) throws IOException {
        if (!file.exists())
            return null;
        if (file.isFile()) { // file
            FileChannel fc = new RandomAccessFile(file, "r").getChannel();
            try {
                ByteBuffer buffer = fc.map(MapMode.READ_ONLY, 0, file.length());
                return buffer;
            } finally {
                fc.close();
            }
        } else { // directory
            StringBuffer buffer = new StringBuffer(1024);
            buffer.append("<html><head><title>").append(uri);
            buffer.append("</title></head><body>");
            buffer.append("<h1>Index of ").append(uri).append("</h1><hr/>");
            buffer.append("<table width=\"100%\">");

            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory())
                    printFile(buffer, files[i]);
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile())
                    printFile(buffer, files[i]);
            }

            buffer.append("</table></body></html>");
            return Charset.UTF8.encode(buffer);
        }
    }

    private void printFile(StringBuffer buffer, File file) {
        boolean directory = file.isDirectory();
        buffer.append("<tr><td><a href=\"");
        buffer.append(file.getName());
        buffer.append(directory ? "/" : "");
        buffer.append("\">");
        buffer.append(file.getName());
        buffer.append(directory ? "/" : "");
        buffer.append("</a></td><td>");
        buffer.append(directory ? "-" : String.valueOf(file.length()));
        buffer.append("</td><td>");
        buffer.append(new Date(file.lastModified()));
        buffer.append("</td></tr>");
    }

    public void objectReceived(Session session, Object obj) throws Exception {
        HttpRequest request = (HttpRequest) obj;

        boolean keepAlive = "keep-alive".equalsIgnoreCase(request
                .getParam("Connection"));
        ByteBuffer content = getContent(request.getRequestURI());

        HttpResponse response = new HttpResponse();
        response.setVersion(request.getVersion());
        if (content == null) {
            response.setStatusCode(404);
            response.setReasonPhrase("Not Found");
            response.setParam("Content-Type", "text/plain");
            response.setParam("Content-Length", String
                    .valueOf(NOT_FOUND.length));
            response.setContent(NOT_FOUND);
        } else {
            response.setStatusCode(200);
            response.setReasonPhrase("OK");
            response.setParam("Content-Type", mimeMap.getContentTypeFor(request
                    .getRequestURI()));
            response.setParam("Content-Length", String.valueOf(content
                    .remaining()));
        }
        response.setParam("Server", "Cindy Http Server");
        response.setParam("Connection", keepAlive ? "keep-alive" : "close");

        Future future = session.send(response); // send http header
        if (content != null)
            future = session.flush(new DefaultPacket(content)); // send content
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
