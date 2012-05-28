package org.juxtapose.streamline.util.net;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol;

public class ClientConnectorPipelineFactory implements ChannelPipelineFactory
{
	@Override
	public ChannelPipeline getPipeline() throws Exception 
	{
		ChannelPipeline pipeline = pipeline();

		// Add the number codec first,
		pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
		pipeline.addLast("protobufDecoder", new ProtobufDecoder(StreamDataProtocol.Message.getDefaultInstance()));

		pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
		pipeline.addLast("protobufEncoder", new ProtobufEncoder());

		// and then business logic.
		pipeline.addLast("handler", new ClientConnectorHandler());

		return pipeline;
	}

}
