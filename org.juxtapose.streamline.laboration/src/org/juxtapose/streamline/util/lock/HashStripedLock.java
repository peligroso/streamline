package org.juxtapose.streamline.util.lock;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Pontus Jörgne
 * 24 jul 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class HashStripedLock
{
	private HashMap<Integer, ReentrantLock> m_locks;
	
	public HashStripedLock( int inLockCount )
	{
		if( inLockCount < 1 )
			throw new IllegalArgumentException( "Cannot create HashStripedLock with less than 1 element" );
		
		m_locks = new HashMap<Integer, ReentrantLock>();
		
		for( int i = 0; i < inLockCount; i++ )
		{
			m_locks.put( i, new ReentrantLock() );
		}
	}
	
	/**
	 * @param inMutexKey
	 */
	public void lock( String inMutexKey )
	{
		int hashCode = inMutexKey.hashCode();
		int index = Math.abs( hashCode % m_locks.size() );
		
		ReentrantLock lock = m_locks.get( index );
		lock.lock();
	}
	
	/**
	 * @param inMutexKey
	 */
	public void unlock( String inMutexKey )
	{
		int hashCode = inMutexKey.hashCode();
		int index = Math.abs( hashCode % m_locks.size() );
		
		ReentrantLock lock = m_locks.get( index );
		lock.unlock();
	}
	
	
}
