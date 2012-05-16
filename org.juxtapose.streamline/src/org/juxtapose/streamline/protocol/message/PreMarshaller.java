package org.juxtapose.streamline.protocol.message;

import java.util.Map;

import org.juxtapose.streamline.protocol.message.StreamDataProtocol.DataMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.Message;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringEntry;

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
	public static Message createSubQuery( String inService, Map<String, String> inQuery )
	{
		Message.Builder builder = Message.newBuilder();
		builder.setService( inService );
		builder.setType( MessageConstants.SUB_QUERY );
		
		DataMap.Builder dataBuilder = DataMap.newBuilder();
		
		for( Map.Entry<String, String> keyVal : inQuery.entrySet() )
		{
			addStringValue( dataBuilder, keyVal.getKey(), keyVal.getValue() );
		}
		
		builder.setDataMap( dataBuilder.build() );
			
		return builder.build();
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
}
