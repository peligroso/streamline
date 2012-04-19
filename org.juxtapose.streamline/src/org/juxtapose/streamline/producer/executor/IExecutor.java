package org.juxtapose.streamline.producer.executor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public interface IExecutor
{
	public static int HIGH = 0;
	public static int LOW = 0;
	
	public void execute( final Runnable inRunnable, int inPrio );
	public void execute( final Runnable inRunnable, int inPrio, final String inSequenceKey );
	public void executeBlocking( final Runnable inRunnable, int inPrio, final ReentrantLock inSequenceLock );
	public void scheduleExecution( final Runnable inRunnable, int inPrio, long inTime, TimeUnit inTimeUnit );
}
