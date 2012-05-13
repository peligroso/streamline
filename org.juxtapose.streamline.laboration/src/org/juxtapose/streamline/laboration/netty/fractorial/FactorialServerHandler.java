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
  import java.util.Formatter;
  import java.util.logging.Level;
  import java.util.logging.Logger;
  
  import org.jboss.netty.channel.ChannelEvent;
  import org.jboss.netty.channel.ChannelHandlerContext;
  import org.jboss.netty.channel.ChannelStateEvent;
  import org.jboss.netty.channel.ExceptionEvent;
  import org.jboss.netty.channel.MessageEvent;
  import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
  
  /**
31   * Handler for a server-side channel.  This handler maintains stateful
32   * information which is specific to a certain channel using member variables.
33   * Therefore, an instance of this handler can cover only one channel.  You have
34   * to create a new handler instance whenever you create a new channel and insert
35   * this handler  to avoid a race condition.
36   */
  public class FactorialServerHandler extends SimpleChannelUpstreamHandler {
  
      private static final Logger logger = Logger.getLogger(
              FactorialServerHandler.class.getName());
  
      // Stateful properties.
      private int lastMultiplier = 1;
      private BigInteger factorial = new BigInteger(new byte[] { 1 });
  
      @Override
      public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception 
      {
          if (e instanceof ChannelStateEvent) {
              logger.info(e.toString());
          }
          super.handleUpstream(ctx, e);
      }
  
      @Override
      public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) 
      {
          // Calculate the cumulative factorial and send it to the client.
          BigInteger number;
          if (e.getMessage() instanceof BigInteger) {
              number = (BigInteger) e.getMessage();
          } else {
              number = new BigInteger(e.getMessage().toString());
          }
          lastMultiplier = number.intValue();
          factorial = factorial.multiply(number);
          e.getChannel().write(factorial);
      }
  
      @Override
      public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception 
      {
          logger.info(new Formatter().format( "Factorial of %,d is: %,d", lastMultiplier, factorial).toString());
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
  }


