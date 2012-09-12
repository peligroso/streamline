package org.juxtapose.streamline.protocol.message;

import java.util.Iterator;
import java.util.Map;

import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.BigDecimalEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.BooleanEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.DataMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.HashMapEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.LongEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.Message;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.NullEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubQueryMessage;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubQueryResponseMessage;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.UpdateMessage;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeBigDecimal;
import org.juxtapose.streamline.util.data.DataTypeBoolean;
import org.juxtapose.streamline.util.data.DataTypeHashMap;
import org.juxtapose.streamline.util.data.DataTypeLong;
import org.juxtapose.streamline.util.data.DataTypeNull;
import org.juxtapose.streamline.util.data.DataTypeString;

import com.google.protobuf.ByteString;
import com.trifork.clj_ds.IPersistentMap;

/**
 * @author Pontus J�rgne
 * May 16, 2012
 * Copyright (c) Pontus J�rgne. All rights reserved
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
	 * @param inRef
	 * @param inDataMap
	 * @return
	 */
	public static Message createUpdateMessage( int inRef, IPersistentMap<String, DataType<?>> inDataMap)
	{
		UpdateMessage.Builder builder = UpdateMessage.newBuilder();
		builder.setType( inRef );
		
		DataMap.Builder dataMapBuilder = DataMap.newBuilder();
		
		parseMapValues( inDataMap, dataMapBuilder );
		builder.setData( dataMapBuilder.build() );
		
		
		Message.Builder messBuilder = Message.newBuilder();
		messBuilder.setUpdateMessage( builder.build() );
		messBuilder.setType( Message.Type.SubQueryMessage );
		
		return messBuilder.build();
		
	}
	
	/**
	 * @param inTag
	 * @param inRef
	 * @return
	 */
	public static Message createSubResponse( Long inTag, int inRef )
	{
		SubQueryResponseMessage.Builder builder = SubQueryResponseMessage.newBuilder();
		builder.setReference( inRef );
		builder.setTag( inTag.intValue() );
		
		Message.Builder messBuilder = Message.newBuilder();
		messBuilder.setSubQueryResponseMessage( builder.build() );
		messBuilder.setType( Message.Type.SubQueryResponseMessage );
		
		return messBuilder.build();
	}
	
	public static void parseMapValues( IPersistentMap<String, DataType<?>> inDataMap, DataMap.Builder inBuilder )
	{
		Iterator<Map.Entry<String, DataType<?>>> iterator = inDataMap.iterator();
		
		while( iterator.hasNext() )
		{
			Map.Entry<String, DataType<?>> entry = iterator.next();
			
			if( entry.getValue() instanceof DataTypeString )
			{
				StringEntry.Builder strBuilder = StringEntry.newBuilder();
				strBuilder.setField( entry.getKey() );
				strBuilder.setData( ((DataTypeString)entry.getValue()).get() );
				inBuilder.addStringEntries( strBuilder.build() );
			}
			else if( entry.getValue() instanceof DataTypeBigDecimal )
			{
				DataTypeBigDecimal bd = (DataTypeBigDecimal)entry.getValue();
				BigDecimalEntry.Builder bdBuilder = BigDecimalEntry.newBuilder();
				bdBuilder.setField( entry.getKey() );
				bdBuilder.setScale( bd.get().scale());
				bdBuilder.setIntBytes( ByteString.copyFrom( bd.get().unscaledValue().toByteArray() ));
				inBuilder.addBDEntries( bdBuilder.build() );
			}
			if( entry.getValue() instanceof DataTypeLong )
			{
				LongEntry.Builder longBuilder = LongEntry.newBuilder();
				longBuilder.setField( entry.getKey() );
				longBuilder.setData( ((DataTypeLong)entry.getValue()).get() );
				inBuilder.addLongEntries( longBuilder.build() );
			}
			if( entry.getValue() instanceof DataTypeBoolean)
			{
				BooleanEntry.Builder booleanBuilder = BooleanEntry.newBuilder();
				booleanBuilder.setField( entry.getKey() );
				booleanBuilder.setData( ((DataTypeBoolean)entry.getValue()).get() );
				inBuilder.addBoolEntries( booleanBuilder.build() );
			}
			if( entry.getValue() instanceof DataTypeNull )
			{
				NullEntry.Builder nullBuilder = NullEntry.newBuilder();
				nullBuilder.setField( entry.getKey() );
				inBuilder.addNullEntries( nullBuilder.build() );
			}
			if( entry.getValue() instanceof DataTypeHashMap )
			{
				HashMapEntry.Builder dataMapBuilder = HashMapEntry.newBuilder();
				dataMapBuilder.setField( entry.getKey() );
				
				DataTypeHashMap hashMap = (DataTypeHashMap)entry.getValue();
				DataMap.Builder hashMapBuilder = DataMap.newBuilder();
				
				parseMapValues( hashMap.get(), hashMapBuilder );
				
				dataMapBuilder.setData( hashMapBuilder.build() );
			}
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
