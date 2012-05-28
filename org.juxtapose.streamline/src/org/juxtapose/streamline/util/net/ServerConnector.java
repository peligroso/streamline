package org.juxtapose.streamline.util.net;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.juxtapose.streamline.stm.ISTM;

public class ServerConnector 
{
	private final int port;
	
	private final ISTM stm;
	
	public ServerConnector( ISTM inSTM, int inPort ) 
	{
        port = inPort;
        stm = inSTM;
    }
	
	public void run() 
	{
        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory( Executors.newCachedThreadPool(),Executors.newCachedThreadPool() ));

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new ServerConnectorPipelineFactory( stm ));

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(port));
    }
}
