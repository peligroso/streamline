package org.juxtapose.streamline.protocol.message;

import java.math.BigDecimal;

import org.juxtapose.streamline.protocol.message.StreamDataProtocol.BigDecimalEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.BooleanEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.DataMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.HashMapEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.NullEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringMap;

import com.google.protobuf.ByteString;

public class Test {

	public static void main( String[] inArgs )
	{
//		SubQueryMessage.Builder builder = SubQueryMessage.newBuilder();
//		
//		StringMap.Builder dataBuilder = StringMap.newBuilder();
//		addStringValue( dataBuilder, "CCY1", "EUR" );
//		addStringValue( dataBuilder, "CCY2", "SEK" );
//		
//		addBigDecimalValue( dataBuilder, "BID", new BigDecimal("0.01") );
//		addBigDecimalValue( dataBuilder, "ASK", new BigDecimal("-0.0123") );
//		
//		builder.setDataMap( dataBuilder.build() );
//		SubQueryMessage notice = builder.build();
//		
//		byte[] bytes = notice.toByteArray();
//		
//		try 
//		{
//			notice = SubQueryMessage.parseFrom( bytes );
//			System.out.println(notice.getType());
//			
//			for( int i = 0; i < notice.getDataMap().getStringEntriesCount(); i++ )
//			{
//				StringEntry entry = notice.getDataMap().getStringEntries(i);
//				
//				System.out.println( entry.getField()+" = "+entry.getData() );
//			}
//			
//			for( int i = 0; i < notice.getDataMap().getBDEntriesCount(); i++ )
//			{
//				BigDecimalEntry entry = notice.getDataMap().getBDEntries(i);
//				
//				BigDecimal bd = new BigDecimal( new BigInteger( entry.getIntBytes().toByteArray() ), entry.getScale() );
//				System.out.println( entry.getField()+" = "+bd );
//			}
//		} 
//		catch (InvalidProtocolBufferException e) 
//		{
//			e.printStackTrace();
//		}
	}
	
	public static void addStringValue( StringMap.Builder inData, String inField, String inValue )
	{
		StringEntry.Builder strEntryBuilder = StringEntry.newBuilder();
		strEntryBuilder.setData( inValue );
		strEntryBuilder.setField( inField );
		
		inData.addStringEntries( strEntryBuilder.build() );
	}
	
	public static void addBigDecimalValue( DataMap.Builder inData, String inField, BigDecimal inValue )
	{
		BigDecimalEntry.Builder biBuilder = BigDecimalEntry.newBuilder();
		biBuilder.setIntBytes( ByteString.copyFrom( inValue.unscaledValue().toByteArray()));
		biBuilder.setScale( inValue.scale() );
		biBuilder.setField( inField );
		
		inData.addBDEntries( biBuilder.build() );
	}
	
	public static void addBooleanValue( DataMap.Builder inData, String inField, Boolean inValue )
	{
		BooleanEntry.Builder boolEntryBuilder = BooleanEntry.newBuilder();
		boolEntryBuilder.setData( inValue );
		boolEntryBuilder.setField( inField );
		
		inData.addBoolEntries( boolEntryBuilder.build() );
	}
	
	public static void addNullValue( DataMap.Builder inData, String inField )
	{
		NullEntry.Builder nullEntryBuilder = NullEntry.newBuilder();
		nullEntryBuilder.setField( inField );
		
		inData.addNullEntries( nullEntryBuilder.build() );
	}
	
	public static void addHashMapValue( DataMap.Builder inData, String inField, DataMap inValue )
	{
		HashMapEntry.Builder hmBuilder = HashMapEntry.newBuilder();
		hmBuilder.setData( inValue );
		hmBuilder.setField( inField );
		
		inData.addHashMapEntries( hmBuilder.build() );
	}
}
