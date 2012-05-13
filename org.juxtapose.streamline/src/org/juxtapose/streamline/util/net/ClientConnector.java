package org.juxtapose.streamline.util.net;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class ClientConnector {

	private final String host;
	private final int port;

	public ClientConnector(String host, int port ) 
	{
		this.host = host;
		this.port = port;
	}
	
	public void run() {
		// Configure the client.
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory( Executors.newCachedThreadPool(),Executors.newCachedThreadPool()));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new ClientConnectorPipelineFactory());

		// Make a new connection.
		ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(host, port));

		// Wait until the connection is made successfully.
		Channel channel = connectFuture.awaitUninterruptibly().getChannel();

		// Get the handler instance to retrieve the answer.
		ClientConnectorHandler handler = (ClientConnectorHandler) channel.getPipeline().getLast();

		// Shut down all thread pools to exit.
		bootstrap.releaseExternalResources();
	}
	

	public static void main(String[] args) throws Exception {
		// Print usage if no argument is specified.
//		if (args.length != 3) {
//			System.err.println(
//					"Usage: " + FactorialClient.class.getSimpleName() +
//			" <host> <port> <count>");
//			return;
//		}

		// Parse options.
		String host = "localhost";//args[0];
		int port = 8085;//Integer.parseInt(args[1]);

		new ClientConnector(host, port).run();
	}
}
