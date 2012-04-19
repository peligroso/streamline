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
	
	public ReentrantLock startStopLock = new ReentrantLock();

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
				throw new IllegalAccessError();
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
}
