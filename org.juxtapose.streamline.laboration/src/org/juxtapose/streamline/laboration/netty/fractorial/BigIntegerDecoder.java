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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/**
27   * Decodes the binary representation of a {@link BigInteger} prepended
28   * with a magic number ('F' or 0x46) and a 32-bit integer length prefix into a
29   * {@link BigInteger} instance.  For example, { 'F', 0, 0, 0, 1, 42 } will be
30   * decoded into new BigInteger("42").
31   */
public class BigIntegerDecoder extends FrameDecoder {

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		// Wait until the length prefix is available.
		if (buffer.readableBytes() < 5) {
			return null;
		}

		buffer.markReaderIndex();

		// Check the magic number.
		int magicNumber = buffer.readUnsignedByte();
		if (magicNumber != 'F') {
			buffer.resetReaderIndex();
			throw new CorruptedFrameException(
					"Invalid magic number: " + magicNumber);
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

		return new BigInteger(decoded);
	}
}


