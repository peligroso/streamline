package org.juxtapose.streamline.util.net;


public class ServerReferenceStore extends ReferenceStore
{

	@Override
	public int createReference()
	{
		return referenceIncrement.addAndGet( 1 );
	}
	
}
