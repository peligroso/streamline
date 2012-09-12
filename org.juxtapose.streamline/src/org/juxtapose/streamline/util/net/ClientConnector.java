package org.juxtapose.streamline.util.net;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.juxtapose.streamline.stm.ISTM;

public class ClientConnector {

	private final String host;
	private final int port;
	private final ISTM stm;
	
	public ClientConnector(String host, int port, ISTM inSTM ) 
	{
		this.host = host;
		this.port = port;
		stm = inSTM;
	}
	
	public void run() {
		// Configure the client.
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory( Executors.newCachedThreadPool(),Executors.newCachedThreadPool()));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new ClientConnectorPipelineFactory( stm ));

		// Make a new connection.
		ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(host, port));

		// Wait until the connection is made successfully.
		Channel channel = connectFuture.awaitUninterruptibly().getChannel();

		// Get the handler instance to retrieve the answer.
//		ClientConnectorHandler handler = (ClientConnectorHandler) channel.getPipeline().getLast();

		// Shut down all thread pools to exit.
//		bootstrap.releaseExternalResources();
	}
	
}
