package org.juxtapose.streamline.producer.executor;

public interface StickyRunnable extends Runnable 
{
	public void setHash( int inHash );
	public int getHash( );
}
