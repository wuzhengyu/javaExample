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

import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import net.sf.cindy.PacketDecoder;
import net.sf.cindy.PacketEncoder;
import net.sf.cindy.Session;
import net.sf.cindy.SessionAcceptor;
import net.sf.cindy.SessionAcceptorHandler;
import net.sf.cindy.SessionHandler;
import net.sf.cindy.SessionType;
import net.sf.cindy.example.http.handler.CachedFileHandler;
import net.sf.cindy.example.http.handler.EchoHandler;
import net.sf.cindy.filter.SSLFilter;
import net.sf.cindy.session.SessionFactory;

/**
 * Simple http server.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class HttpServer {

    private static final int port = 8080;
    private static final PacketEncoder encoder = new HttpResponseEncoder();
    private static final PacketDecoder decoder = new HttpRequestDecoder();

    private static boolean echo;
    private static SSLContext sslc;
    private static SessionHandler handler;

    public static void main(String[] args) throws Exception {
        List params = Arrays.asList(args);

        echo = params.contains("-echo");
        handler = echo ? (SessionHandler) new EchoHandler()
                : new CachedFileHandler();
        sslc = params.contains("-secure") ? getSSLcontext() : null;

        SessionAcceptor acceptor = SessionFactory
                .createSessionAcceptor(SessionType.TCP);
        acceptor.setListenPort(port);
        acceptor.setAcceptorHandler(new SessionAcceptorHandler() {

            public void exceptionCaught(SessionAcceptor acceptor,
                    Throwable cause) {
                System.err.println(cause);
            }

            public void sessionAccepted(SessionAcceptor acceptor,
                    Session session) throws Exception {
                startSession(session);
            }
        });
        acceptor.start();
        if (acceptor.isStarted())
            print();
    }

    private static void print() {
        System.out.println((echo ? "echo" : "file")
                + " http server listen on port " + port + " with ssl "
                + (sslc == null ? "disabled" : "enabled"));
    }

    private static void startSession(Session session) {
        session.setPacketDecoder(decoder);
        session.setPacketEncoder(encoder);
        session.setSessionHandler(handler);
        if (sslc != null) {
            SSLFilter filter = new SSLFilter(sslc);
            filter.setClientMode(false);
            session.addSessionFilter(filter);
        }
        session.start();
    }

    private static SSLContext getSSLcontext() throws Exception {
        char[] password = "password".toCharArray();

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(HttpServer.class.getClassLoader().getResourceAsStream(
                "net/sf/cindy/example/http/testkeys"), password);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context;
    }
}
