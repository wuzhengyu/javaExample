package net.sf.cindy.example.demo;

import net.sf.cindy.Session;
import net.sf.cindy.SessionAcceptor;
import net.sf.cindy.SessionAcceptorHandlerAdapter;
import net.sf.cindy.SessionHandlerAdapter;
import net.sf.cindy.SessionType;
import net.sf.cindy.decoder.SerialDecoder;
import net.sf.cindy.session.SessionFactory;

public class Server1 {

	public static void main(String[] args) {
		SessionAcceptor acceptor = SessionFactory.createSessionAcceptor(SessionType.TCP);
		acceptor.setListenPort(1234);
		acceptor.setAcceptorHandler(new SessionAcceptorHandlerAdapter() {

			public void sessionAccepted(SessionAcceptor acceptor, Session session) throws Exception {
				session.setSessionHandler(new SessionHandlerAdapter() {
					public void sessionStarted(Session session) throws Exception {
						System.out.println("session started");
					}

					public void objectReceived(Session session, Object obj) throws Exception {
						System.out.println("received " + obj.getClass().getName() + " : " + obj);
					}
				});
				session.start();
			}

		});
		acceptor.start();

	}

}
