package org.juxtapose.streamline.stm;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Pontus Jörgne
 * Jan 15, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 * Class for using a synchronized procedure of start/stop with a lock of a single purpose to manage sequential start/stop 
 * It should not matter if disposed is invoked before init.
 */
public abstract class TemporaryController
{
	private boolean initiated = false;
	private boolean disposed = false;
	
	/**
	 * No need for fair looking since the lock will only be requested twice, and if there is a race condition the effect will be kind of the same.
	 * Either it is started and immediately stopped or it will never start at all.
	 */
	public ReentrantLock startStopLock = new ReentrantLock( );

	protected volatile int priority;
	
	public TemporaryController( int inPriority )
	{
		priority = inPriority;
	}
	/**
	 * 
	 */
	public void init()
	{
		startStopLock.lock();
		try
		{
			if( initiated )
			{
				throw new IllegalAccessError("Tried to start an already started TemporaryController");
			}
			if( disposed )
			{
				return;
			}
			initiated = true;
			start();
		}
		finally
		{
			startStopLock.unlock();
		}
	}
	
	/**
	 * 
	 */
	public void dispose()
	{
		startStopLock.lock();
		try
		{
			disposed = true;
			if( initiated )
			{
				stop();
			}
		}finally
		{
			startStopLock.unlock();
		}
	}
	
	protected abstract void start();
	protected abstract void stop();
	
	public boolean isInitiated()
	{
		return initiated;
	}
	
	public boolean isDisposed()
	{
		return disposed;
	}
	
	public void setPriority( int inPriority )
	{
		if( inPriority == priority )
			return;
		
		priority = inPriority;
		priorityUpdated( priority );
	}
	
	public int getPriority( )
	{
		return priority;
	}
	
	/**
	 * @param inPriority
	 * Method is always called as a consequence of setPriority
	 */
	protected abstract void priorityUpdated( int inPriority );
}
