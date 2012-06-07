package org.juxtapose.streamline.util.subscriber;

import org.juxtapose.streamline.util.ISTMEntry;

public class Sequence
{
	static int TYPE_INIT =  1;
	static int TYPE_NO_OBJ = 2;
	static int TYPE_OBJ = 3;
	
	final Long id;
	final ISTMEntry object;
	final int type;
	
	static Sequence INIT_SEQUENCE = new Sequence( -1l, null, Sequence.TYPE_INIT);

	/**
	 * @param inId
	 * @param inData
	 * @param inType
	 */
	Sequence( Long inId, ISTMEntry inData, int inType )
	{
		id = inId;
		object = inData;
		type = inType;
	}
}
