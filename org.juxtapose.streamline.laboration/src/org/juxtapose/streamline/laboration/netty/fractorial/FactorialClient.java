package org.juxtapose.streamline.laboration.netty.fractorial;


/*
2    * Copyright 2011 The Netty Project
3    *
4    * The Netty Project licenses this file to you under the Apache License,
5    * version 2.0 (the "License"); you may not use this file except in compliance
6    * with the License. You may obtain a copy of the License at:
7    *
8    * http://www.apache.org/licenses/LICENSE-2.0
9    *
10   * Unless required by applicable law or agreed to in writing, software
11   * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
12   * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
13   * License for the specific language governing permissions and limitations
14   * under the License.
15   */

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

/**
27   * Sends a sequence of integers to a {@link FactorialServer} to calculate
28   * the factorial of the specified integer.
29   */
public class FactorialClient {

	private final String host;
	private final int port;
	private final int count;

	public FactorialClient(String host, int port, int count) {
		this.host = host;
		this.port = port;
		this.count = count;
	}

	public void run() {
		// Configure the client.
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory( Executors.newCachedThreadPool(),Executors.newCachedThreadPool()));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new FactorialClientPipelineFactory(count));

		// Make a new connection.
		ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(host, port));

		// Wait until the connection is made successfully.
		Channel channel = connectFuture.awaitUninterruptibly().getChannel();

		// Get the handler instance to retrieve the answer.
		FactorialClientHandler handler = (FactorialClientHandler) channel.getPipeline().getLast();

		// Print out the answer.
		System.err.format("Factorial of %,d is: %,d", count, handler.getFactorial());

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
		int port = 8080;//Integer.parseInt(args[1]);
		int count = 10;//Integer.parseInt(args[2]);
		if (count <= 0) {
			throw new IllegalArgumentException("count must be a positive integer.");
		}

		new FactorialClient(host, port, count).run();
	}
}


