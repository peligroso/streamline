package org.juxtapose.streamline.util.message;

import static org.juxtapose.streamline.util.message.MessageConstants.*;

import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.juxtapose.streamline.stm.DataSerializer;
public class MessageSerializer 
{
	/**
	 * @param inMessage
	 * @return
	 */
	public static final byte[] serializeMessage( Message inMessage )
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
	
	/**
	 * @param inBytes
	 * @return
	 */
	public static final Message unserializeMessage( byte[] inBytes )
	{
		if( inBytes == null || inBytes.length < 1 )
			throw new ArrayIndexOutOfBoundsException( "cannot unserialize a byte arr with no elements" );
		
		byte type = inBytes[0];
		
		int offset = 1;
		
		if( type == TYPE_SUBQUERY )
		{
			int serviceLength = (int)DataSerializer.numberFromByteArray( inBytes, offset, DataSerializer.FIELD_BYTE_LENGTH, null );
			offset += DataSerializer.FIELD_BYTE_LENGTH;
			
			byte[] charBytes = ArrayUtils.subarray( inBytes, offset, offset+serviceLength );			
			String service = new String(charBytes);
			offset += charBytes.length;
			
			byte[] queryBytes = ArrayUtils.subarray( inBytes, offset, inBytes.length );
			
			Map<String, String> queryMap = DataSerializer.unserializeQuery( queryBytes );
			
			return new SubQuery( service, queryMap );
		}
		throw new IllegalAccessError();
	}
}
