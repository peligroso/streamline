package org.juxtapose.streamline.laboration.netty;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.handler.timeout.ReadTimeoutException;

public class UptimeClientHandler extends SimpleChannelUpstreamHandler {

	final ClientBootstrap bootstrap;
	private final Timer timer;
	private long startTime = -1;

	public UptimeClientHandler(ClientBootstrap bootstrap, Timer timer) {
		this.bootstrap = bootstrap;
		this.timer = timer;
	}

	InetSocketAddress getRemoteAddress() {
		return (InetSocketAddress) bootstrap.getOption("remoteAddress");
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		println("Disconnected from: " + getRemoteAddress());
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
		println("Sleeping for: " + UptimeClient.RECONNECT_DELAY + "s");
		timer.newTimeout(new TimerTask() {
			public void run(Timeout timeout) throws Exception {
				println("Reconnecting to: " + getRemoteAddress());
				bootstrap.connect();
			}
		}, UptimeClient.RECONNECT_DELAY, TimeUnit.SECONDS);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		if (startTime < 0) {
			startTime = System.currentTimeMillis();
		}

		println("Connected to: " + getRemoteAddress());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		Throwable cause = e.getCause();
		if (cause instanceof ConnectException) {
			startTime = -1;
			println("Failed to connect: " + cause.getMessage());
		}
		if (cause instanceof ReadTimeoutException) {
			// The connection was OK but there was no traffic for last period.
			println("Disconnecting due to no inbound traffic");
		} else {
			cause.printStackTrace();
		}
		ctx.getChannel().close();
	}

	void println(String msg) {
		if (startTime < 0) {
			System.err.format("[SERVER IS DOWN] %s%n", msg);
		} else {
			System.err.format("[UPTIME: %5ds] %s%n", (System.currentTimeMillis() - startTime) / 1000, msg);
		}
	}
}