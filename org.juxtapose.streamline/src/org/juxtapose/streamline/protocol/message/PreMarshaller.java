package org.juxtapose.streamline.protocol.message;

import java.util.Map;

import org.juxtapose.streamline.protocol.message.StreamDataProtocol.DataMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.Message;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubQueryMessage;

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
