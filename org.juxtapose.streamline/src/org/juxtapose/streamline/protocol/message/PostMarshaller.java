package org.juxtapose.streamline.protocol.message;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import static org.juxtapose.streamline.tools.STMUtil.*;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.BigDecimalEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.BooleanEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.DataKey;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.DataMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.HashMapEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.LongEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubQueryMessage;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeArrayList;
import org.juxtapose.streamline.util.data.DataTypeBigDecimal;
import org.juxtapose.streamline.util.data.DataTypeBoolean;
import org.juxtapose.streamline.util.data.DataTypeHashMap;
import org.juxtapose.streamline.util.data.DataTypeLong;
import org.juxtapose.streamline.util.data.DataTypeStatus;
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
	public static interface IParseObject
	{
		public void update( String inField, DataType<?> inData );
	}
	
	public static class MapParseObject implements IParseObject
	{
		IPersistentMap<String, DataType<?>> map;
		
		public MapParseObject( IPersistentMap<String, DataType<?>> inMap )
		{
			map = inMap;
		}

		@Override
		public void update( String inField, DataType<?> inData )
		{
			map = map.assoc( inField, inData );			
		}
	}
	
	public static class ArrayParseObject implements IParseObject
	{
		SortedMap<String, DataType<?>> map;
		
		public ArrayParseObject( SortedMap<String, DataType<?>> inMap )
		{
			map = inMap;
		}

		@Override
		public void update( String inField, DataType<?> inData )
		{
			map.put( inField, inData );			
		}
	}
	
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
	
	public static final ISTMEntryKey parseKey( DataKey inKey )
	{
		String keys[] = new String[ inKey.getStringEntriesCount() ];
		String vals[] = new String[ inKey.getStringEntriesCount() ];
		
		for( int i = 0; i < inKey.getStringEntriesCount(); i++ )
		{
			StringEntry entry = inKey.getStringEntries( i );
			keys[i] = entry.getField();
			vals[i] = entry.getData();
		}
		
		if( keys.length == 1 && DataConstants.FIELD_SINGLE_VALUE_DATA_KEY.equals( keys[0] ))
		{
			return createDataKey( inKey.getService(), inKey.getType(), vals[0] );
		}
		else
		{
			return createDataKey( inKey.getService(), inKey.getType(), keys, vals );
		}
		
	}
	
	public static final void parseData( DataMap inDataMap, IParseObject inParseObject )
	{
		List<StringEntry> stringEntries = inDataMap.getStringEntriesList();
		
		if( stringEntries != null && !stringEntries.isEmpty() )
		{
			for( StringEntry entry : stringEntries )
			{
				String field = entry.getField();
				String data = entry.getData();
				
				inParseObject.update( field, new DataTypeString(data) );
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
				
				inParseObject.update( field, new DataTypeBigDecimal(bd) );
			}
		}
		
		List<LongEntry> longEntries = inDataMap.getLongEntriesList();
		
		if( longEntries != null && !longEntries.isEmpty() )
		{
			for( LongEntry entry : longEntries )
			{
				String field = entry.getField();
				long value = entry.getData();
				
				inParseObject.update( field, new DataTypeLong( value ) );
			}
		}
		
		List<BooleanEntry> boolEntries = inDataMap.getBoolEntriesList();
		
		if( boolEntries != null && !boolEntries.isEmpty() )
		{
			for( BooleanEntry entry : boolEntries )
			{
				String field = entry.getField();
				boolean value = entry.getData();
				
				inParseObject.update( field, new DataTypeBoolean(value) );
			}
		}
		
		List<HashMapEntry> hashMapEntries = inDataMap.getHashMapEntriesList();
		
		if( hashMapEntries != null && !hashMapEntries.isEmpty() )
		{
			for( HashMapEntry entry : hashMapEntries )
			{
				String field = entry.getField();
				DataMap dMap = entry.getData();
				boolean list = entry.getList();
				
				if( list )
				{
					PersistentArrayList<DataType<?>> arr = parseDataList( dMap );
					inParseObject.update( field, new DataTypeArrayList( arr ) );
				}
				else
				{
					IPersistentMap< String, DataType<?>> subMap = PersistentHashMap.emptyMap();
					subMap = parseDataMap( dMap, subMap );
					inParseObject.update( field, new DataTypeHashMap(subMap) );
				}
			}
		}
	}
	
	
	public static final PersistentArrayList<DataType<?>> parseDataList( DataMap inDataMap )
	{
		PersistentArrayList<DataType<?>> perList = new PersistentArrayList<DataType<?>>();
		
		TreeMap<String, DataType<?>> sortedMap = new TreeMap<String, DataType<?>>();
		
		ArrayParseObject parseObject = new ArrayParseObject( sortedMap );
		parseData( inDataMap, parseObject );
		
		for( Map.Entry<String, DataType<?>> entry : sortedMap.entrySet() )
		{
			perList = perList.add( entry.getValue() );
		}
		
		return perList;
	}
	
	/**
	 * @param inDataMap
	 * @param inMap
	 * @return
	 */
	public static final IPersistentMap<String, DataType<?>> parseDataMap( DataMap inDataMap, IPersistentMap <String, DataType<?>> inMap )
	{
		IPersistentMap<String, DataType<?>> map = inMap;
		
		MapParseObject parseObject = new MapParseObject( map );
		parseData( inDataMap, parseObject );
		
		map = parseObject.map;
		
		Integer status = inDataMap.getStatus();
		
		if( status != null )
		{
			Status st = Status.values()[status];
			map = map.assoc( DataConstants.FIELD_STATUS, new DataTypeStatus( st ) );
		}
		
		return map;
	}
	
}
