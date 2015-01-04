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
package net.sf.cindy.example.simple;

import java.io.IOException;

import net.sf.cindy.Session;
import net.sf.cindy.SessionAcceptor;
import net.sf.cindy.SessionAcceptorHandlerAdapter;
import net.sf.cindy.SessionHandler;
import net.sf.cindy.SessionType;
import net.sf.cindy.session.SessionFactory;

/**
 * Simple TCP/IP services.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class SimpleServer {

	public static void main(String[] args) throws IOException {
		startService(7, new EchoHandler(), "Echo service");
		startService(9, new DiscardHandler(), "Discard service");
		startService(13, new DaytimeHandler(), "Daytime service");
		startService(19, new ChargenHandler(), "Chargen service");
	}

	private static void startService(int port, final SessionHandler handler, String serviceType) {
		SessionAcceptor acceptor = SessionFactory.createSessionAcceptor(SessionType.TCP);
		acceptor.setListenPort(port);
		acceptor.setAcceptorHandler(new SessionAcceptorHandlerAdapter() {

			public void sessionAccepted(SessionAcceptor acceptor, Session session) throws Exception {
				session.setSessionHandler(handler);
				session.start();
			}

		});
		acceptor.start();
		if (acceptor.isStarted())
			System.out.println(serviceType + " listen on " + acceptor.getListenAddress());
	}
}
