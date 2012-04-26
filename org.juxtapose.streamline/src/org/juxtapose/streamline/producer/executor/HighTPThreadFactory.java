package org.juxtapose.streamline.producer.executor;

import java.util.concurrent.ThreadFactory;

public class HighTPThreadFactory implements ThreadFactory {

	public static String HIGH_TP_EXECUTOR_THREAD = "High TP Executor Thread: ";
	public static int HIGH_TP_COUNT = 0;
	
	public static HighTPThreadFactory factory = new HighTPThreadFactory();
	
	@Override
	public Thread newThread(Runnable r)
	{
		Thread t = new Thread( r, HIGH_TP_EXECUTOR_THREAD+(HIGH_TP_COUNT++) );
		t.setPriority( Thread.MAX_PRIORITY );
		t.setDaemon( true );
		
		return t;
	}
	
}
