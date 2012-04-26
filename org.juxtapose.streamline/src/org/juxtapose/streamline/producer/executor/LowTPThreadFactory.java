package org.juxtapose.streamline.producer.executor;

import java.util.concurrent.ThreadFactory;

public class LowTPThreadFactory implements ThreadFactory {

	public static String LOW_TP_EXECUTOR_THREAD = "Low TP Executor Thread: ";
	public static int LOW_TP_COUNT = 0;
	
	public static LowTPThreadFactory factory = new LowTPThreadFactory();
	
	@Override
	public Thread newThread(Runnable r)
	{
		Thread t = new Thread( r, LOW_TP_EXECUTOR_THREAD+(LOW_TP_COUNT++) );
		t.setPriority( Thread.MIN_PRIORITY );
		t.setDaemon( true );
		
		return t;
	}

}
