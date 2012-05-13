package org.juxtapose.streamline.util.net;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

public class ServerConnectorPipelineFactory implements ChannelPipelineFactory
{

	@Override
	public ChannelPipeline getPipeline() throws Exception 
	{
		 ChannelPipeline pipeline = pipeline();
		 
		 pipeline.addLast("decoder", new MessageDecoder());
		 pipeline.addLast("encoder", new MessageEncoder());
		
		 pipeline.addLast("handler", new ServerConnectorHandler());
		 
		 return pipeline;
	}

}
