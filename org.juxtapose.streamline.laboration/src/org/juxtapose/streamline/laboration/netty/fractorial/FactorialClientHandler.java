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
import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

/**
35   * Handler for a client-side channel.  This handler maintains stateful
36   * information which is specific to a certain channel using member variables.
37   * Therefore, an instance of this handler can cover only one channel.  You have
38   * to create a new handler instance whenever you create a new channel and insert
39   * this handler to avoid a race condition.
40   */
public class FactorialClientHandler extends SimpleChannelUpstreamHandler {

	private static final Logger logger = Logger.getLogger(
			FactorialClientHandler.class.getName());

	// Stateful properties
	private int i = 1;
	private int receivedMessages;
	private final int count;
	final BlockingQueue<BigInteger> answer = new LinkedBlockingQueue<BigInteger>();

	public FactorialClientHandler(int count) {
		this.count = count;
	}

	public BigInteger getFactorial() {
		boolean interrupted = false;
		for (;;) {
			try {
				BigInteger factorial = answer.take();
				if (interrupted) {
					Thread.currentThread().interrupt();
				}
				return factorial;
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}
	}

	@Override
	public void handleUpstream(
			ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (e instanceof ChannelStateEvent) {
			logger.info(e.toString());
		}
		super.handleUpstream(ctx, e);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		sendNumbers(e);
	}

	@Override
	public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) {
		sendNumbers(e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) {
		receivedMessages ++;
		if (receivedMessages == count) {
			// Offer the answer after closing the connection.
			e.getChannel().close().addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture future) {
					boolean offered = answer.offer((BigInteger) e.getMessage());
					assert offered;
				}
			});
		}
	}

	@Override
	public void exceptionCaught(
			ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.log(
				Level.WARNING,
				"Unexpected exception from downstream.",
				e.getCause());
		e.getChannel().close();
	}

	private void sendNumbers(ChannelStateEvent e) {
		Channel channel = e.getChannel();
		while (channel.isWritable()) {
			if (i <= count) {
				channel.write(Integer.valueOf(i));
				i ++;
			} else {
				break;
			}
		}
	}
}

