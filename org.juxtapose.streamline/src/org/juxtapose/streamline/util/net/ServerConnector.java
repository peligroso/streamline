package org.juxtapose.streamline.util.net;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class ServerConnector 
{
	private final int port;
	
	public ServerConnector(int inPort) 
	{
        port = inPort;
    }
	
	public void run() 
	{
        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory( Executors.newCachedThreadPool(),Executors.newCachedThreadPool() ));

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new ServerConnectorPipelineFactory());

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(port));
    }
}
