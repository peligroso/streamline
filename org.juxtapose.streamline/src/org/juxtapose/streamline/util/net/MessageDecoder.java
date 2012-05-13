package org.juxtapose.streamline.util.net;

import java.math.BigInteger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.juxtapose.streamline.util.message.MessageSerializer;

public class MessageDecoder extends FrameDecoder
{

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,ChannelBuffer buffer) throws Exception 
	{
		if (buffer.readableBytes() < 5) {
			return null;
		}

		buffer.markReaderIndex();

		// Check the magic number.
		int magicNumber = buffer.readUnsignedByte();
		if (magicNumber != 'F') 
		{
			buffer.resetReaderIndex();
			throw new CorruptedFrameException("Invalid magic number: " + magicNumber);
		}

		// Wait until the whole data is available.
		int dataLength = buffer.readInt(); 
		if (buffer.readableBytes() < dataLength) {
			buffer.resetReaderIndex();
			return null;
		}

		// Convert the received data into a new BigInteger.
		byte[] decoded = new byte[dataLength];
		buffer.readBytes(decoded);

		return MessageSerializer.unserializeMessage(decoded);
	}
}
