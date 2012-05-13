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
  import org.jboss.netty.buffer.ChannelBuffers;
  import org.jboss.netty.channel.Channel;
  import org.jboss.netty.channel.ChannelHandlerContext;
  import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
  
  /**
27   * Encodes a {@link Number} into the binary representation prepended with
28   * a magic number ('F' or 0x46) and a 32-bit length prefix.  For example, 42
29   * will be encoded to { 'F', 0, 0, 0, 1, 42 }.
30   */
  public class NumberEncoder extends OneToOneEncoder {

	  @Override
	  protected Object encode( ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		  if (!(msg instanceof Number)) {
			  // Ignore what this encoder can't encode.
			  return msg;
		  }

		  // Convert to a BigInteger first for easier implementation.
		  BigInteger v;
		  if (msg instanceof BigInteger) {
			  v = (BigInteger) msg;
		  } else {
			  v = new BigInteger(String.valueOf(msg));
		  }

		  // Convert the number into a byte array.
		  byte[] data = v.toByteArray();
		  int dataLength = data.length;

		  // Construct a message.
		  ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
		  buf.writeByte((byte) 'F'); // magic number
		  buf.writeInt(dataLength);  // data length
		  buf.writeBytes(data);      // data

		  // Return the constructed message.
		  return buf;
	  }
  }


