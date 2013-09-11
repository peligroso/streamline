package org.juxtapose.streamline.protocol.message;

import static org.juxtapose.streamline.tools.STMUtil.createEntryKey;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.BigDecimalEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.BooleanEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.DataKey;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.DataMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.HashMapEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.LongEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.ReferenceEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubQueryMessage;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeLazyRef;
import org.juxtapose.streamline.util.data.DataTypeRef;

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
		public void update( String inField, Object inData );
	}
	
	public static class MapParseObject implements IParseObject
	{
		IPersistentMap<String, Object> map;
		
		public MapParseObject( IPersistentMap<String, Object> inMap )
		{
			map = inMap;
		}

		@Override
		public void update( String inField, Object inData )
		{
			map = map.assoc( inField, inData );			
		}
	}
	
	public static class ArrayParseObject implements IParseObject
	{
		SortedMap<String, Object> map;
		
		public ArrayParseObject( SortedMap<String, Object> inMap )
		{
			map = inMap;
		}

		@Override
		public void update( String inField, Object inData )
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
			return createEntryKey( inKey.getService(), inKey.getType(), vals[0] );
		}
		else
		{
			return createEntryKey( inKey.getService(), inKey.getType(), keys, vals );
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
				
				inParseObject.update( field, data );
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
				
				inParseObject.update( field, bd );
			}
		}
		
		List<LongEntry> longEntries = inDataMap.getLongEntriesList();
		
		if( longEntries != null && !longEntries.isEmpty() )
		{
			for( LongEntry entry : longEntries )
			{
				String field = entry.getField();
				long value = entry.getData();
				
				inParseObject.update( field, value );
			}
		}
		
		List<BooleanEntry> boolEntries = inDataMap.getBoolEntriesList();
		
		if( boolEntries != null && !boolEntries.isEmpty() )
		{
			for( BooleanEntry entry : boolEntries )
			{
				String field = entry.getField();
				boolean value = entry.getData();
				
				inParseObject.update( field, value );
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
					PersistentArrayList<Object> arr = parseDataList( dMap );
					inParseObject.update( field, arr );
				}
				else
				{
					IPersistentMap< String, Object> subMap = PersistentHashMap.emptyMap();
					subMap = parseDataMap( dMap, subMap );
					inParseObject.update( field, subMap );
				}
			}
		}
		
		List<ReferenceEntry> refEntries = inDataMap.getRefEntriesList();
		
		if( refEntries != null && !refEntries.isEmpty() )
		{
			for( ReferenceEntry refEntry : refEntries )
			{
				String field = refEntry.getField();
				DataKey key = refEntry.getKey();
				boolean lazy = refEntry.getLazy();
				
				ISTMEntryKey entryKey = parseKey( key );
				
				inParseObject.update( field, lazy ? new DataTypeLazyRef( entryKey ) : new DataTypeRef( entryKey ) );
			}
		}
	}
	
	
	public static final PersistentArrayList<Object> parseDataList( DataMap inDataMap )
	{
		PersistentArrayList<Object> perList = new PersistentArrayList<Object>();
		
		TreeMap<String, Object> sortedMap = new TreeMap<String, Object>();
		
		ArrayParseObject parseObject = new ArrayParseObject( sortedMap );
		parseData( inDataMap, parseObject );
		
		for( Map.Entry<String, Object> entry : sortedMap.entrySet() )
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
	public static final IPersistentMap<String, Object> parseDataMap( DataMap inDataMap, IPersistentMap <String, Object> inMap )
	{
		IPersistentMap<String, Object> map = inMap;
		
		MapParseObject parseObject = new MapParseObject( map );
		parseData( inDataMap, parseObject );
		
		map = parseObject.map;
		
		if( inDataMap.hasStatus() )
		{
			Integer status = inDataMap.getStatus();
			
			Status st = Status.values()[status];
			map = map.assoc( DataConstants.FIELD_STATUS, st );
		}
		
		return map;
	}
	
}
