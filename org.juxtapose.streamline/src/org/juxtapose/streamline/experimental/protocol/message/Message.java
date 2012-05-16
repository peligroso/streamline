package org.juxtapose.streamline.experimental.protocol.message;

public class Message 
{
	byte type;
	
	public final byte getType()
	{
		return type;
	}
	
	public final void setType( byte inType )
	{
		type = inType;
	}
}
