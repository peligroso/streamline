package org.juxtapose.streamline.laboration.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.timeout.*;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

/**
33   * Connects to a server periodically to measure and print the uptime of the
34   * server.  This example demonstrates how to implement reliable reconnection
35   * mechanism in Netty.
36   */
  public class UptimeClient {
  
      // Sleep 5 seconds before a reconnection attempt.
      static final int RECONNECT_DELAY = 5;
  
      // Reconnect when the server sends nothing for 10 seconds.
      private static final int READ_TIMEOUT = 10;
  
      private final String host;
      private final int port;
  
      public UptimeClient() {
          this.host = "localhost";
          this.port = 8086;
      }
  
      public void run() {
    	  // Initialize the timer that schedules subsequent reconnection attempts.
    	  final Timer timer = new HashedWheelTimer();

    	  // Configure the client.
    	  final ClientBootstrap bootstrap = new ClientBootstrap(
    			  new NioClientSocketChannelFactory(
    					  Executors.newCachedThreadPool(),
    					  Executors.newCachedThreadPool()));

    	  // Configure the pipeline factory.
    	  bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

    		  private final ChannelHandler timeoutHandler = new ReadTimeoutHandler(timer, READ_TIMEOUT);

    		  private final ChannelHandler uptimeHandler = new UptimeClientHandler(bootstrap, timer);

    		  public ChannelPipeline getPipeline() throws Exception 
    		  {
    			  return Channels.pipeline(timeoutHandler, uptimeHandler);
    		  }
    	  });

    	  bootstrap.setOption("remoteAddress", new InetSocketAddress(host, port));
  
          // Initiate the first connection attempt - the rest is handled by
          // UptimeClientHandler.
          bootstrap.connect();
      }
  
      public static void main(String[] args) throws Exception {
          // Print usage if no argument is specified.
//          if (args.length != 2) {
//              System.err.println(
//                      "Usage: " + UptimeClient.class.getSimpleName() +
//                      " <host> <port>");
//              return;
//          }
  
          // Parse options.
//          String host = args[0];
//          int port = Integer.parseInt(args[1]);
  
          new UptimeClient().run();
      }
 }
