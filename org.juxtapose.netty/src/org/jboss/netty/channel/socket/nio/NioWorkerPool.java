/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.channel.socket.nio;

import java.util.concurrent.Executor;


/**
 * Default implementation which hands of {@link NioWorker}'s
 * 
 *
 */
public class NioWorkerPool extends AbstractNioWorkerPool<NioWorker> {

    public NioWorkerPool(Executor executor, int workerCount, boolean allowShutdownOnIdle) {
        super(executor, workerCount, allowShutdownOnIdle);
    }

    @Override
    protected NioWorker createWorker(Executor executor, boolean allowShutdownOnIdle) {
        return new NioWorker(executor, allowShutdownOnIdle);
    }

}
