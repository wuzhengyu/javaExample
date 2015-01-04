package net.sf.cindy.example.demo;

import java.net.InetSocketAddress;

import net.sf.cindy.Packet;
import net.sf.cindy.Session;
import net.sf.cindy.SessionType;
import net.sf.cindy.buffer.BufferFactory;
import net.sf.cindy.packet.DefaultPacket;
import net.sf.cindy.session.SessionFactory;

public class Client1 {

	public static void main(String[] args) {
		Session session = SessionFactory.createSession(SessionType.TCP);    

		//set remote address
		session.setRemoteAddress(new InetSocketAddress("localhost", 1234)); 

		//start session and wait until completed
		session.start().complete();    

		//create send packet
		Packet packet = new DefaultPacket(BufferFactory.wrap("Hello world".getBytes())); 

		//send packet and wait until completed
		session.flush(packet).complete();

		//close session and wait until completed
		session.close().complete();

	}

}
