Cindy (http://cindy.sourceforge.net)
================================================

1.Introduce
	This library base on java nio and provide a simple and efficient 
	asynchronous I/O framework, support tcp/udp/pipe, support jmx, 
	easy for testing.

2.Requirements
	- Java 1.4 or higher. 
		http://java.sun.com/j2se/1.4.2/	
	- Apache Commons-Logging
		http://jakarta.apache.org/commons/logging/
	- Java 1.4 backport of JSR 166
		http://www.mathcs.emory.edu/dcl/util/backport-util-concurrent/
	- JUnit (Required only for building the test suite)
		http://www.junit.org/
	- JMX (Required only when enable jmx, included in Java 5.0)
		http://java.sun.com/products/JavaManagement/

3.Configuration
	- Global configuration
		* net.sf.cindy.config (default: config.properties)
			Cindy config file
		* net.sf.cindy.enableJmx (default: false)
			Enable jmx support
		* net.sf.cindy.disableInnerException (default: false)
			Disable inner exception dispatch
	
	- Buffer configuration
		* net.sf.cindy.bufferPool (default: net.sf.cindy.buffer.DefaultBufferPool)
			BufferPool class name
		* net.sf.cindy.useDirectBuffer (default: false)
			Use direct buffer instead of heap buffer
		* net.sf.cindy.useLinkedBuffer (default: false)
			Use linked buffer instead of memory copy			
			
	- Dispatcher configuration
		* net.sf.cindy.dispatcher (default: net.sf.cindy.session.dispatcher.DefaultDispatcher)
			Dispatcher class name
		* net.sf.cindy.dispatcher.concurrent (default: 1)
			Dispatcher concurrent size
		* net.sf.cindy.dispatcher.keepAliveTime (default: 5000)
			Dispatcher keep alive time
		* net.sf.cindy.dispatcher.capacity (default: 1000)
			Dispatcher capacity (flow control)
			
	- Session configuration
		* net.sf.cindy.session.timeout (default: 0)
			Session timeout (like SO_TIMEOUT)
		* net.sf.cindy.session.recvBufferSize (default: -1)
			SO_RCVBUF
		* net.sf.cindy.session.sendBufferSize (default: -1)
			SO_SNDBUF
		* net.sf.cindy.session.reuseAddress	(default: false)
			SO_REUSEADDR
		* net.sf.cindy.session.tcpNoDelay (default: true)
			TCP_NODELAY
		* net.sf.cindy.session.soLinger (default: -1)
			SO_LINGER
		* net.sf.cindy.session.readPacketSize (default: 8192)
			Session read packet size
		* net.sf.cindy.session.writePacketSize (default: 1024*1024)
			Sesssion max write packet size (nio channel does no handle WSAENOBUFS)
		* net.sf.cindy.session.type.tcp (default: net.sf.cindy.session.nio.SocketChannelSession)
			Tcp session class name
		* net.sf.cindy.session.type.udp (default: net.sf.cindy.session.nio.DatagramChannelSession)
			Udp session class name
		* net.sf.cindy.session.type.pipe (default: net.sf.cindy.session.nio.PipeSession)
			Pipe session class name			
		* net.sf.cindy.session.type.file (default: null)
			File session class name					
	
	- Session acceptor configuration
		* net.sf.cindy.acceptor.type.tcp (default: net.sf.cindy.session.nio.NonBlockingSessionAcceptor)
			Tcp session acceptor class name
		* net.sf.cindy.acceptor.backlog (default: 100)
			Acceptor backlog
		* net.sf.cindy.acceptor.reuseAddress (default: false)
			SO_REUSEADDR
		
	For example, to enable jmx, use direct buffer, set dispatcher 
	keep alive time to 10s and enable acceptor reuse address, you 
	can specify configuration in system environment :
	
		-Dnet.sf.cindy.enableJmx=true 
		-Dnet.sf.cindy.useDirectBuffer=true
		-Dnet.sf.cindy.dispatcher.keepAliveTime=10000
		-Dnet.sf.cindy.acceptor.reuseAddress=true
		
	or you can put such cindy.properties on your classpath:
	
		enableJmx=true
		useDirectBuffer=true
		dispatcher.keepAliveTime=10000
		acceptor.reuseAddress=true