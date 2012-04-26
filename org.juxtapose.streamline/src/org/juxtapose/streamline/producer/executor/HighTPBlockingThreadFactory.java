package org.juxtapose.streamline.producer.executor;

import java.util.concurrent.ThreadFactory;

public class HighTPBlockingThreadFactory implements ThreadFactory
{
	public static String HIGH_TP_BLOCKING_EXECUTOR_THREAD = "High TP Blocking Executor Thread: ";
	public static int HIGH_TP_BLOCKING_COUNT = 0;
	
	public static HighTPBlockingThreadFactory factory = new HighTPBlockingThreadFactory();
	
	@Override
	public Thread newThread(Runnable r)
	{
		Thread t = new Thread( r, HIGH_TP_BLOCKING_EXECUTOR_THREAD+(HIGH_TP_BLOCKING_COUNT++) );
		t.setPriority( Thread.MAX_PRIORITY );
		t.setDaemon( true );
		
		return t;
	}

}
