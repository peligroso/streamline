package org.juxtapose.streamline.producer.executor;

public abstract class Executable implements IExecutable{

	int hash;
	
	public Executable( )
	{
		
	}
	
	public Executable( int inHash )
	{
		hash = inHash;
	}
	
	public int getHash()
	{
		return hash;
	}
	
	public void setHash( int inHash )
	{
		hash = inHash;
	}
}
