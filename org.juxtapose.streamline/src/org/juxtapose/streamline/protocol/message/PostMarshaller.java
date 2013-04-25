package org.juxtapose.streamline.protocol.message;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.juxtapose.streamline.protocol.message.StreamDataProtocol.BigDecimalEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.BooleanEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.DataMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.HashMapEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubQueryMessage;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeBigDecimal;
import org.juxtapose.streamline.util.data.DataTypeBoolean;
import org.juxtapose.streamline.util.data.DataTypeHashMap;
import org.juxtapose.streamline.util.data.DataTypeString;

import com.google.protobuf.ByteString;
import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

/**
 * @author Pontus Jörgne
 * 25 apr 2013
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class PostMarshaller 
{
	/**
	 * @param inMessage
	 * @return
	 */
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
	
	/**
	 * @param inDataMap
	 * @param inMap
	 * @return
	 */
	public  static final IPersistentMap<String, DataType<?>> parseDataMap( DataMap inDataMap, IPersistentMap <String, DataType<?>> inMap )
	{
		IPersistentMap<String, DataType<?>> map = inMap;
		
		List<StringEntry> stringEntries = inDataMap.getStringEntriesList();
		
		if( stringEntries != null && !stringEntries.isEmpty() )
		{
			for( StringEntry entry : stringEntries )
			{
				String field = entry.getField();
				String data = entry.getData();
				
				map = map.assoc( field, new DataTypeString(data) );
			}
		}
		
		List<BigDecimalEntry> bdEntries = inDataMap.getBDEntriesList();
		
		if( bdEntries != null && !bdEntries.isEmpty() )
		{
			for( BigDecimalEntry entry : bdEntries )
			{
				String field = entry.getField();
				int scale = entry.getScale();
				ByteString bs = entry.getIntBytes();
				BigInteger bi = new BigInteger(bs.toByteArray());
				
				BigDecimal bd = new BigDecimal(bi, scale);
				
				map = map.assoc( field, new DataTypeBigDecimal(bd) );
			}
		}
		
		List<BooleanEntry> boolEntries = inDataMap.getBoolEntriesList();
		
		if( boolEntries != null && !boolEntries.isEmpty() )
		{
			for( BooleanEntry entry : boolEntries )
			{
				String field = entry.getField();
				boolean value = entry.getData();
				
				map = map.assoc( field, new DataTypeBoolean(value) );
			}
		}
		
		List<HashMapEntry> hashMapEntries = inDataMap.getHashMapEntriesList();
		
		if( hashMapEntries != null && !hashMapEntries.isEmpty() )
		{
			for( HashMapEntry entry : hashMapEntries )
			{
				String field = entry.getField();
				DataMap dMap = entry.getData();
				
				IPersistentMap< String, DataType<?>> subMap = PersistentHashMap.emptyMap();
				
				subMap = parseDataMap( dMap, subMap );
				
				map = map.assoc( field, new DataTypeHashMap(subMap) );
			}
		}
		
		return map;
	}
	
}
