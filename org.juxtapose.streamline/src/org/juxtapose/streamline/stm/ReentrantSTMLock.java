package org.juxtapose.streamline.stm;

public class ReentrantSTMLock 
{
	public static final Long AVAILIBLE = -1l;
	
	final long threadID;
	final int counter;
	
	public ReentrantSTMLock() 
	{
		threadID = AVAILIBLE;
		counter = 0;
	}
	
	private ReentrantSTMLock( long inThreadId, int inCount )
	{
		threadID = inThreadId;
		counter = inCount;
	}
	
	public ReentrantSTMLock accuire( )
	{
		long threadId = Thread.currentThread().getId();
		if( AVAILIBLE.equals( threadID ) || threadID == threadId )
		{
			return new ReentrantSTMLock( threadId, counter+1 );
		}
		return null;
	}
	
	public ReentrantSTMLock release( )
	{
		long threadId = Thread.currentThread().getId();
		if( threadID == threadId )
		{
			int newCount = counter-1;
			
			return  newCount == 0 ? new ReentrantSTMLock() : new ReentrantSTMLock( threadId, newCount );
		}
		else
		{
			throw new IllegalAccessError( "Thread was trying to release lock without first succesfully acquired it" );
		}
	}
}
