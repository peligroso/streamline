package org.juxtapose.streamline.protocol.message;

import java.util.HashMap;
import java.util.Map;

import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubQueryMessage;

public class PostMarshaller 
{
	public static final Map<String, String> parseQueryMap( SubQueryMessage inMessage )
	{
		StringMap dMap = inMessage.getQueryMap();
		
		Map<String, String> queryMap = new HashMap<String, String>();

		for( int i = 0; i < dMap.getStringEntriesCount(); i++ )
		{
			StringEntry snt = dMap.getStringEntries( i );
			queryMap.put(snt.getField(), snt.getData());
		}
		
		return queryMap;
	}
}
