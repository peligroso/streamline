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
  
  import static org.jboss.netty.channel.Channels.*;
  
  import org.jboss.netty.channel.ChannelPipeline;
  import org.jboss.netty.channel.ChannelPipelineFactory;
  import org.jboss.netty.handler.codec.compression.ZlibDecoder;
  import org.jboss.netty.handler.codec.compression.ZlibEncoder;
  import org.jboss.netty.handler.codec.compression.ZlibWrapper;
  
  /**
27   * Creates a newly configured {@link ChannelPipeline} for a server-side channel.
28   */
  public class FactorialServerPipelineFactory implements
          ChannelPipelineFactory {
  
      public ChannelPipeline getPipeline() throws Exception {
          ChannelPipeline pipeline = pipeline();
  
          // Enable stream compression (you can remove these two if unnecessary)
//          pipeline.addLast("deflater", new ZlibEncoder(ZlibWrapper.GZIP));
//          pipeline.addLast("inflater", new ZlibDecoder(ZlibWrapper.GZIP));
  
          // Add the number codec first,
          pipeline.addLast("decoder", new BigIntegerDecoder());
          pipeline.addLast("encoder", new NumberEncoder());
  
          // and then business logic.
          // Please note we create a handler for every new channel
          // because it has stateful properties.
          pipeline.addLast("handler", new FactorialServerHandler());
  
          return pipeline;
      }
  }

