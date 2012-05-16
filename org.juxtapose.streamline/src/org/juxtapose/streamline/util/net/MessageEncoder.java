package org.juxtapose.streamline.util.net;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.juxtapose.streamline.experimental.protocol.message.Message;
import org.juxtapose.streamline.experimental.protocol.message.MessageSerializer;

public class MessageEncoder extends OneToOneEncoder{

	 @Override
	  protected Object encode( ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception 
	  {  
		 if (!(msg instanceof Message)) {
			  // Ignore what this encoder can't encode.
			  throw new IllegalArgumentException("Can only encode object of type Message");
		  }

		  // Convert to a BigInteger first for easier implementation.
		  byte[] bytes = MessageSerializer.serializeMessage( (Message)msg );

		  // Convert the number into a byte array.
		  int dataLength = bytes.length;

		  // Construct a message.
		  ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
		  buf.writeByte((byte) 'F'); // magic number
		  buf.writeInt(dataLength);  // data length
		  buf.writeBytes(bytes);      // data

		  // Return the constructed message.
		  return buf;
	  }

}
