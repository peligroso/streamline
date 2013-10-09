package org.juxtapose.streamline.protocol.message;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.BigDecimalEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.BooleanEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.DataKey;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.DataMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.HashMapEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.LongEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.Message;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.NullEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.ReferenceEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.RequestMessage;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubQueryMessage;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubQueryResponseMessage;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubscribeMessage;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.UnsubscribeMessage;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.UpdateMessage;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeLazyRef;
import org.juxtapose.streamline.util.data.DataTypeNull;
import org.juxtapose.streamline.util.data.DataTypeRef;

import com.google.protobuf.ByteString;
import com.trifork.clj_ds.IPersistentMap;


/**
 * @author Pontus Jörgne
 * May 16, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class PreMarshaller 
{
	/**
	 * @param inService
	 * @param inQuery
	 * @return
	 */
	public static Message createSubQuery( String inService, int inTag, Map<String, String> inQuery )
	{
		SubQueryMessage.Builder builder = SubQueryMessage.newBuilder();
		builder.setService( inService );
		builder.setTag( inTag );
		
		StringMap.Builder dataBuilder = StringMap.newBuilder();
		
		for( Map.Entry<String, String> keyVal : inQuery.entrySet() )
		{
			addStringValue( dataBuilder, keyVal.getKey(), keyVal.getValue() );
		}
		
		builder.setQueryMap( dataBuilder.build() );
		
		Message.Builder messBuilder = Message.newBuilder();
		messBuilder.setSubQueryMessage( builder.build() );
		messBuilder.setType( Message.Type.SubQueryMessage );
			
		return messBuilder.build();
	}
	
	/**
	 * @param inReference
	 * @return
	 */
	public static Message createSubscriptionMessage( int inReference )
	{
		SubscribeMessage.Builder subMessB = SubscribeMessage.newBuilder();
		subMessB.setReference( inReference );
		
		Message.Builder messB = Message.newBuilder();
		messB.setSubscribeMessage( subMessB );
		messB.setType( Message.Type.SubscribeMessage );
		
		return messB.build();
	}
	
	public static Message createUnSubscribeMessage( int inReference )
	{
		UnsubscribeMessage.Builder unSubMessB = UnsubscribeMessage.newBuilder();
		unSubMessB.setReference( inReference );
		
		Message.Builder messB = Message.newBuilder();
		messB.setUnsubscribeMessage( unSubMessB );
		messB.setType( Message.Type.UnSubscribeMessage );
		
		return messB.build();
		
	}
	
	public static Message createSubscriptionMessage( int inReference, ISTMEntryKey inKey )
	{
		SubscribeMessage.Builder subMessB = SubscribeMessage.newBuilder();
		
		DataKey key = createDataKey( inKey );
		subMessB.setKey( key );
		subMessB.setReference( inReference );
		
		Message.Builder messB = Message.newBuilder();
		messB.setSubscribeMessage( subMessB );
		messB.setType( Message.Type.SubscribeMessage );
		
		return messB.build();
	}
	
	/**
	 * @param inRef
	 * @param inDataMap
	 * @return
	 */
	public static Message createUpdateMessage( int inRef, ISTMEntry inEntry, boolean inFullUpdate, ISTMEntryKey inKey )
	{
		IPersistentMap<String, Object> dataMap = inEntry.getDataMap();
		
		UpdateMessage.Builder builder = UpdateMessage.newBuilder();
		builder.setReference( inRef );
		builder.setFullupdate( inFullUpdate );
		
		DataMap.Builder dataMapBuilder = DataMap.newBuilder();
		
		if( inFullUpdate )
		{
			parseMapValues( dataMap, dataMapBuilder );
		}
		else
		{
			parseDeltaMapValues( dataMap, inEntry.getDeltaSet(), dataMapBuilder );
		}
		
		builder.setData( dataMapBuilder.build() );
		
		if( inKey != null )
		{
			DataKey dKey = createDataKey( inKey );
			
		}
		
		Message.Builder messBuilder = Message.newBuilder();
		try
		{
			messBuilder.setUpdateMessage( builder.build() );
		}catch( Throwable t )
		{
			t.printStackTrace();
		}
		messBuilder.setType( Message.Type.UpdateMessage );
		
		return messBuilder.build();
		
	}
	
	public static Message createUpdateMessage( int inRef, ISTMEntry inEntry, boolean inFullUpdate )
	{
		return createUpdateMessage( inRef, inEntry, inFullUpdate, null );
	}
	

	/**
	 * @param inTag
	 * @param inRef
	 * @return
	 */
	public static Message createSubResponse( Long inTag, int inRef, Status inStatus )
	{
		SubQueryResponseMessage.Builder builder = SubQueryResponseMessage.newBuilder();
		builder.setReference( inRef );
		builder.setTag( inTag.intValue() );
		builder.setStatus( inStatus.ordinal() );
		
		Message.Builder messBuilder = Message.newBuilder();
		messBuilder.setSubQueryResponseMessage( builder.build() );
		messBuilder.setType( Message.Type.SubQueryResponseMessage );
		
		
		return messBuilder.build();
	}
	
	/**
	 * @param inTag
	 * @param inRef
	 * @param inStatus
	 * @param inEntry
	 * @return
	 */
	public static Message createSubResponse( Long inTag, int inRef, Status inStatus, ISTMEntryKey inKey, ISTMEntry inEntry )
	{
		
		SubQueryResponseMessage.Builder builder = SubQueryResponseMessage.newBuilder();
		builder.setReference( inRef );
		builder.setTag( inTag.intValue() );
		builder.setStatus( inStatus.ordinal() );
		
		if( inEntry != null )
		{
			DataMap.Builder dataBuilder = DataMap.newBuilder();
			parseMapValues( inEntry.getDataMap(), dataBuilder );
			builder.setData( dataBuilder.build() );
		}
		
		if( inKey != null )
		{
			DataKey dKey = createDataKey( inKey );
			
			builder.setKey( dKey );
		}
		
		Message.Builder messBuilder = Message.newBuilder();
		messBuilder.setSubQueryResponseMessage( builder.build() );
		messBuilder.setType( Message.Type.SubQueryResponseMessage );
		
		return messBuilder.build();
	}
	
	/**
	 * @param inTag
	 * @param inVariable
	 * @param inData
	 * @return
	 */
	public static Message createRequestMessage( int inTag, long inType, String inService, String inVariable, IPersistentMap<String, Object> inData )
	{
		
		RequestMessage.Builder builder = RequestMessage.newBuilder();
		builder.setTag( inTag );
		builder.setService( inService );
		
		if( inVariable != null)
			builder.setVariable( inVariable );
		
		if( inData != null )
		{
			DataMap.Builder dataMapBuilder = DataMap.newBuilder();
			parseMapValues( inData, dataMapBuilder );
			
			builder.setData( dataMapBuilder.build() );
			builder.setType( inType );
		}
		
		Message.Builder messBuilder = Message.newBuilder();
		messBuilder.setType( Message.Type.RequestMessage );
		messBuilder.setRequestMessage( builder.build() );
		
		
		return messBuilder.build();
	}
	
	/**
	 * @param inKey
	 * @return
	 */
	public static DataKey createDataKey( ISTMEntryKey inKey )
	{
		DataKey.Builder dataKeyB = DataKey.newBuilder();
		dataKeyB.setType( inKey.getType() );
		dataKeyB.setService( inKey.getService() );
		
		for( String key : inKey.getKeys() )
		{
			String val = inKey.getValue( key );
			
			StringEntry.Builder entryB = StringEntry.newBuilder();
			entryB.setField( key );
			entryB.setData( val );
			
			dataKeyB.addStringEntries( entryB.build() );
		}
		
		return dataKeyB.build();
	}
	
	/**
	 * @param inKey
	 * @param inData
	 * @param inBuilder
	 */
	public static void parseValueToMap( String inKey, Object inData, DataMap.Builder inBuilder )
	{
		if( inData == null )
		{
			NullEntry.Builder nullBuilder = NullEntry.newBuilder();
			nullBuilder.setField( inKey );
			inBuilder.addNullEntries( nullBuilder.build() );
		}
		else if( inData instanceof String )
		{
			StringEntry.Builder strBuilder = StringEntry.newBuilder();
			strBuilder.setField( inKey );
			strBuilder.setData( (String)inData );
			inBuilder.addStringEntries( strBuilder.build() );
		}
		else if( inData instanceof BigDecimal )
		{
			BigDecimal bd = (BigDecimal)inData;
			BigDecimalEntry.Builder bdBuilder = BigDecimalEntry.newBuilder();
			bdBuilder.setField( inKey );
			bdBuilder.setScale( bd.scale());
			bdBuilder.setIntBytes( ByteString.copyFrom( bd.unscaledValue().toByteArray() ));
			inBuilder.addBDEntries( bdBuilder.build() );
		}
		else if( inData instanceof Long )
		{
			LongEntry.Builder longBuilder = LongEntry.newBuilder();
			longBuilder.setField( inKey );
			longBuilder.setData( ((Long)inData) );
			inBuilder.addLongEntries( longBuilder.build() );
		}
		else if( inData instanceof Boolean)
		{
			BooleanEntry.Builder booleanBuilder = BooleanEntry.newBuilder();
			booleanBuilder.setField( inKey );
			booleanBuilder.setData( ((Boolean)inData) );
			inBuilder.addBoolEntries( booleanBuilder.build() );
		}
		else if( inData instanceof DataTypeNull )
		{
			NullEntry.Builder nullBuilder = NullEntry.newBuilder();
			nullBuilder.setField( inKey );
			inBuilder.addNullEntries( nullBuilder.build() );
		}
		else if( inData instanceof IPersistentMap<?, ?> )
		{
			HashMapEntry.Builder dataMapBuilder = HashMapEntry.newBuilder();
			dataMapBuilder.setField( inKey );
			
			IPersistentMap<String, Object> hashMap = (IPersistentMap<String, Object>)inData;
			DataMap.Builder hashMapBuilder = DataMap.newBuilder();
			
			parseMapValues( hashMap, hashMapBuilder );
			
			dataMapBuilder.setData( hashMapBuilder.build() );
			
			inBuilder.addHashMapEntries( dataMapBuilder.build() );
		}
		else if( inData instanceof PersistentArrayList<?> )
		{
			HashMapEntry.Builder dataMapBuilder = HashMapEntry.newBuilder();
			dataMapBuilder.setField( inKey );
			dataMapBuilder.setList( true );
			
			PersistentArrayList<Object> list = (PersistentArrayList<Object>)inData;
			DataMap.Builder hashMapBuilder = DataMap.newBuilder();
			
			parseListValues( (PersistentArrayList<Object>) list, hashMapBuilder );
			
			dataMapBuilder.setData( hashMapBuilder.build() );
			
			inBuilder.addHashMapEntries( dataMapBuilder.build() );
		}
		else if( inData instanceof Status )
		{
			inBuilder.setStatus( ((Status)inData).ordinal() );
		}
		else if( inData instanceof DataTypeRef )
		{
			ISTMEntryKey entryKey = ((DataTypeRef)inData).get();
			parseReference( inKey, entryKey, inBuilder, false );
		}
		else if( inData instanceof DataTypeLazyRef )
		{
			ISTMEntryKey entryKey = ((DataTypeLazyRef)inData).get();
			parseReference( inKey, entryKey, inBuilder, true );
		}
	}
	
	private static void parseReference( String inFieldKey, ISTMEntryKey entryKey, DataMap.Builder inBuilder, boolean inLazy )
	{
		ReferenceEntry.Builder refBuilder = ReferenceEntry.newBuilder();
		
		DataKey key = createDataKey( entryKey );
		
		refBuilder.setField( inFieldKey );
		refBuilder.setKey( key );
		refBuilder.setLazy( inLazy );
		
		inBuilder.addRefEntries( refBuilder.build() );
	}
	/**
	 * @param inDataMap
	 * @param inBuilder
	 */
	public static void parseMapValues( IPersistentMap<String, Object> inDataMap, DataMap.Builder inBuilder )
	{
		Iterator<Map.Entry<String, Object>> iterator = inDataMap.iterator();
		
		while( iterator.hasNext() )
		{
			Map.Entry<String, Object> entry = iterator.next();
			
			parseValueToMap( entry.getKey(), entry.getValue(), inBuilder );
		}
	}
	
	public static void parseDeltaMapValues( IPersistentMap<String, Object> inDataMap, Set<String> inDeltaSet, DataMap.Builder inBuilder )
	{
		Iterator<String> iterator = inDeltaSet.iterator();
		
		while( iterator.hasNext() )
		{
			String key = iterator.next();
			Object val = inDataMap.valAt( key );
			
			parseValueToMap( key, val, inBuilder );
		}
	}
	
	public static void parseListValues( PersistentArrayList<Object> inList, DataMap.Builder inBuilder )
	{
		for( int i = 0; i < inList.size(); i++ )
		{
			Object data = inList.get( i );
			parseValueToMap( Integer.toString( i ), data, inBuilder );
		}
	}
	
	/**
	 * @param inData
	 * @param inField
	 * @param inValue
	 */
	public static void addStringValue( DataMap.Builder inData, String inField, String inValue )
	{
		StringEntry.Builder strEntryBuilder = StringEntry.newBuilder();
		strEntryBuilder.setData( inValue );
		strEntryBuilder.setField( inField );
		
		inData.addStringEntries( strEntryBuilder.build() );
	}
	
	public static void addStringValue( StringMap.Builder inData, String inField, String inValue )
	{
		StringEntry.Builder strEntryBuilder = StringEntry.newBuilder();
		strEntryBuilder.setData( inValue );
		strEntryBuilder.setField( inField );
		
		inData.addStringEntries( strEntryBuilder.build() );
	}
	

}
