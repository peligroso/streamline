package org.juxtapose.streamline.util.net;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

public class ClientConnectorPipelineFactory implements ChannelPipelineFactory
{
	@Override
	public ChannelPipeline getPipeline() throws Exception 
	{
		ChannelPipeline pipeline = pipeline();

		// Add the number codec first,
		pipeline.addLast("decoder", new MessageDecoder());
		pipeline.addLast("encoder", new MessageEncoder());

		// and then business logic.
		pipeline.addLast("handler", new ClientConnectorHandler());

		return pipeline;
	}

}
