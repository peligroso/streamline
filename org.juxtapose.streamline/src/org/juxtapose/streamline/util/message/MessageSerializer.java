package org.juxtapose.streamline.util.message;

import static org.juxtapose.streamline.util.message.MessageConstants.*;

import org.juxtapose.streamline.stm.DataSerializer;
public class MessageSerializer 
{
	public final byte[] serializeMessage( Message inMessage )
	{
		if( inMessage instanceof SubQuery )
		{
			SubQuery sq = ( SubQuery )inMessage;
			byte[] serviceBytes = DataSerializer.serializeString( sq.service );
			byte[] queryBytes = DataSerializer.serializeQuery( sq.queryMap );
			
			byte[] messageBytes = new byte[ 1 + serviceBytes.length + queryBytes.length ];
			messageBytes[0] = TYPE_SUBQUERY;
			int offset = 1;
			System.arraycopy( serviceBytes, 0, messageBytes, offset, serviceBytes.length );
			offset += serviceBytes.length;
			System.arraycopy( queryBytes, 0, messageBytes, offset, queryBytes.length );
			
			return messageBytes;
		}
		throw new IllegalAccessError();
	}
	
}
