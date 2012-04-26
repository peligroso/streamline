package org.juxtapose.streamline.producer.executor;

import java.util.concurrent.ThreadFactory;

public class LowTPBlockingThreadFactory implements ThreadFactory {

	public static String LOW_TP_BLOCKING_EXECUTOR_THREAD = "Low TP Blocking Executor Thread: ";
	public static int LOW_TP_BLOCKING_COUNT = 0;
	
	public static LowTPBlockingThreadFactory factory = new LowTPBlockingThreadFactory();
	
	@Override
	public Thread newThread(Runnable r)
	{
		Thread t = new Thread( r, LOW_TP_BLOCKING_EXECUTOR_THREAD+(LOW_TP_BLOCKING_COUNT++) );
		t.setPriority( Thread.MAX_PRIORITY );
		t.setDaemon( true );
		
		return t;
	}


}
