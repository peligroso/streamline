package org.juxtapose.streamline.protocol.message;

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
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubQueryMessage;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubQueryResponseMessage;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubscribeMessage;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.UpdateMessage;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeArrayList;
import org.juxtapose.streamline.util.data.DataTypeBigDecimal;
import org.juxtapose.streamline.util.data.DataTypeBoolean;
import org.juxtapose.streamline.util.data.DataTypeHashMap;
import org.juxtapose.streamline.util.data.DataTypeLong;
import org.juxtapose.streamline.util.data.DataTypeNull;
import org.juxtapose.streamline.util.data.DataTypeRef;
import org.juxtapose.streamline.util.data.DataTypeStatus;
import org.juxtapose.streamline.util.data.DataTypeString;

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
		IPersistentMap<String, DataType<?>> dataMap = inEntry.getDataMap();
		
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
	
	public static void parseValueToMap( String inKey, DataType<?> inData, DataMap.Builder inBuilder )
	{
		if( inData instanceof DataTypeString )
		{
			StringEntry.Builder strBuilder = StringEntry.newBuilder();
			strBuilder.setField( inKey );
			strBuilder.setData( ((DataTypeString)inData).get() );
			inBuilder.addStringEntries( strBuilder.build() );
		}
		else if( inData instanceof DataTypeBigDecimal )
		{
			DataTypeBigDecimal bd = (DataTypeBigDecimal)inData;
			BigDecimalEntry.Builder bdBuilder = BigDecimalEntry.newBuilder();
			bdBuilder.setField( inKey );
			bdBuilder.setScale( bd.get().scale());
			bdBuilder.setIntBytes( ByteString.copyFrom( bd.get().unscaledValue().toByteArray() ));
			inBuilder.addBDEntries( bdBuilder.build() );
		}
		if( inData instanceof DataTypeLong )
		{
			LongEntry.Builder longBuilder = LongEntry.newBuilder();
			longBuilder.setField( inKey );
			longBuilder.setData( ((DataTypeLong)inData).get() );
			inBuilder.addLongEntries( longBuilder.build() );
		}
		if( inData instanceof DataTypeBoolean)
		{
			BooleanEntry.Builder booleanBuilder = BooleanEntry.newBuilder();
			booleanBuilder.setField( inKey );
			booleanBuilder.setData( ((DataTypeBoolean)inData).get() );
			inBuilder.addBoolEntries( booleanBuilder.build() );
		}
		if( inData instanceof DataTypeNull )
		{
			NullEntry.Builder nullBuilder = NullEntry.newBuilder();
			nullBuilder.setField( inKey );
			inBuilder.addNullEntries( nullBuilder.build() );
		}
		if( inData instanceof DataTypeHashMap )
		{
			HashMapEntry.Builder dataMapBuilder = HashMapEntry.newBuilder();
			dataMapBuilder.setField( inKey );
			
			DataTypeHashMap hashMap = (DataTypeHashMap)inData;
			DataMap.Builder hashMapBuilder = DataMap.newBuilder();
			
			parseMapValues( hashMap.get(), hashMapBuilder );
			
			dataMapBuilder.setData( hashMapBuilder.build() );
			
			inBuilder.addHashMapEntries( dataMapBuilder.build() );
		}
		if( inData instanceof DataTypeArrayList )
		{
			HashMapEntry.Builder dataMapBuilder = HashMapEntry.newBuilder();
			dataMapBuilder.setField( inKey );
			dataMapBuilder.setList( true );
			
			DataTypeArrayList list = (DataTypeArrayList)inData;
			DataMap.Builder hashMapBuilder = DataMap.newBuilder();
			
			parseListValues( (PersistentArrayList<DataType<?>>) list.get(), hashMapBuilder );
			
			dataMapBuilder.setData( hashMapBuilder.build() );
			
			inBuilder.addHashMapEntries( dataMapBuilder.build() );
		}
		else if( inData instanceof DataTypeStatus )
		{
			inBuilder.setStatus( ((DataTypeStatus)inData).get().ordinal() );
		}
		else if( inData instanceof DataTypeRef )
		{
			ISTMEntryKey entryKey = ((DataTypeRef)inData).get();
			DataKey key = createDataKey( entryKey );
			
			ReferenceEntry.Builder refBuilder = ReferenceEntry.newBuilder();
			
			refBuilder.setField( inKey );
			refBuilder.setKey( key );
			
			inBuilder.addRefEntries( refBuilder.build() );
		}
	}
	/**
	 * @param inDataMap
	 * @param inBuilder
	 */
	public static void parseMapValues( IPersistentMap<String, DataType<?>> inDataMap, DataMap.Builder inBuilder )
	{
		Iterator<Map.Entry<String, DataType<?>>> iterator = inDataMap.iterator();
		
		while( iterator.hasNext() )
		{
			Map.Entry<String, DataType<?>> entry = iterator.next();
			
			parseValueToMap( entry.getKey(), entry.getValue(), inBuilder );
		}
	}
	
	public static void parseDeltaMapValues( IPersistentMap<String, DataType<?>> inDataMap, Set<String> inDeltaSet, DataMap.Builder inBuilder )
	{
		Iterator<String> iterator = inDeltaSet.iterator();
		
		while( iterator.hasNext() )
		{
			String key = iterator.next();
			DataType<?> val = inDataMap.valAt( key );
			
			parseValueToMap( key, val, inBuilder );
		}
	}
	
	public static void parseListValues( PersistentArrayList<DataType<?>> inList, DataMap.Builder inBuilder )
	{
		for( int i = 0; i < inList.size(); i++ )
		{
			DataType<?> data = inList.get( i );
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
