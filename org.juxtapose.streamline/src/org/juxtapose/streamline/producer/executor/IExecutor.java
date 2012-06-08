package org.juxtapose.streamline.producer.executor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public interface IExecutor
{
	public static int LOW = 0;
	public static int HIGH = 1;
	
	public void execute( final IExecutable inExecutable, int inPrio );
	public void execute( final IExecutable inExecutable, int inPrio, final String inSequenceKey );
	public void executeBlocking( final IExecutable inExecutable, int inPrio, final ReentrantLock inSequenceLock );
	public void scheduleExecution( final IExecutable inExecutable, int inPrio, long inTime, TimeUnit inTimeUnit );
}
