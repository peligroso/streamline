package org.juxtapose.streamline.util.net;

public class ClientReferenceStore extends ReferenceStore
{

	@Override
	public int createReference()
	{
		return referenceIncrement.addAndGet( -1 );
	}

}
