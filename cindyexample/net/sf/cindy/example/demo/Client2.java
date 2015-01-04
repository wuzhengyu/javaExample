package net.sf.cindy.example.demo;

import java.net.InetSocketAddress;

import net.sf.cindy.Future;
import net.sf.cindy.FutureListener;
import net.sf.cindy.Session;
import net.sf.cindy.SessionType;
import net.sf.cindy.encoder.SerialEncoder;
import net.sf.cindy.session.SessionFactory;

public class Client2 {

	public static void main(String[] args) {
		Session session = SessionFactory.createSession(SessionType.TCP);

		// set remote address
		session.setRemoteAddress(new InetSocketAddress("localhost", 1234));

		// start session and wait until completed
		session.start().complete();
		session.setPacketEncoder(new SerialEncoder());
		session.send("Cindy ");
		session.send("Hello world!").addListener(new FutureListener() {
			
			public void futureCompleted(Future future) throws Exception {
				future.getSession().close();
			}
		});

	}

}
