package net.sf.cindy.example.demo;

import net.sf.cindy.Session;
import net.sf.cindy.SessionHandlerAdapter;

public class LogSessionHandler extends SessionHandlerAdapter {
	public void objectReceived(Session session, Object obj) throws Exception {
		System.out.println("-->LogSessionHandler.objectReceived:" + obj.toString());
	}

	public void sessionStarted(Session session) throws Exception {
		System.out.println("session started");
	}

	public void sessionClosed(Session session) throws Exception {
		System.out.println("session closed");
	}
}
