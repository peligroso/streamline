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

  
  import java.net.InetSocketAddress;
  import java.util.concurrent.Executors;
  
  import org.jboss.netty.bootstrap.ServerBootstrap;
  import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
  
  /**
   * Receives a sequence of integers from a {@link FactorialClient} to calculate
   * the factorial of the specified integer.
   */
  public class FactorialServer {
  
      private final int port;
      
      public FactorialServer(int port) {
          this.port = port;
      }
      
      public void run() {
          // Configure the server.
          ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),Executors.newCachedThreadPool()));
  
          // Set up the event pipeline factory.
          bootstrap.setPipelineFactory(new FactorialServerPipelineFactory());
  
          // Bind and start to accept incoming connections.
          bootstrap.bind(new InetSocketAddress(port));
      }
  
      public static void main(String[] args) throws Exception {
          int port;
          if (args.length > 0) {
              port = Integer.parseInt(args[0]);
          } else {
              port = 8080;
          }
          new FactorialServer(port).run();
      }
  }



