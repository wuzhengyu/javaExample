package net.sf.cindy.example.demo;

import java.net.InetSocketAddress;

import net.sf.cindy.Future;
import net.sf.cindy.FutureListener;
import net.sf.cindy.Packet;
import net.sf.cindy.Session;
import net.sf.cindy.SessionType;
import net.sf.cindy.packet.DefaultPacket;
import net.sf.cindy.session.SessionFactory;

public class SyncClient {

	public static void main(String[] args) {
		final Session session = SessionFactory.createSession(SessionType.TCP);
		session.setRemoteAddress(new InetSocketAddress("localhost", 1234));
		Future future = session.start();
		future.addListener(new FutureListener() {

			public void futureCompleted(Future future) throws Exception {
				Packet packet = new DefaultPacket("Hello world!".getBytes());
				session.flush(packet).complete();
				session.close();
			}

		});

	}

}
