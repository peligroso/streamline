package org.juxtapose.streamline.protocol.message;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.juxtapose.streamline.protocol.message.StreamDataProtocol.BigDecimalEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.BooleanEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.DataMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.HashMapEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.Message;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.NullEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringEntry;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.StringMap;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeBigDecimal;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

public class Test {

	public static Map<String, String> createStringKeyValueMap()
	{
		Random rand = new Random();
		
		HashMap<String, String> keyValues = new HashMap<String, String>();
		
		for( int i = 0; i < 200; i++ )
		{
			int random = rand.nextInt(1324);

			String key = "K"+Integer.toString( i );
			String value = "V"+Integer.toString( random);
			
			keyValues.put(key, value);
		}
		
		return keyValues;
	}
	
	public static IPersistentMap<String, DataType<?>> createDataKeyValueMap()
	{
		Random rand = new Random();
		
		IPersistentMap<String, DataType<?>> keyValues = PersistentHashMap.EMPTY;
		
		for( int i = 0; i < 200; i++ )
		{
			double random = rand.nextDouble();

			String key = "K"+Integer.toString( i );
			DataTypeBigDecimal value = new DataTypeBigDecimal( random );
			
			keyValues = keyValues.assoc(key, value);
		}
		
		return keyValues;
	}
	
	
	public static void testPerformance(int inIterations ) throws InvalidProtocolBufferException
	{
		String t;
		
		long startTime = System.nanoTime();
		
		long initTime = 0;
		
		long anchor = 0l;
		
		long totalByteSize = 0;
		
		for( int i = 0; i < inIterations; i++ )
		{
			long startInit = System.nanoTime();
			IPersistentMap<String, DataType<?>> keyValues = createDataKeyValueMap();
			
//			Message mess = PreMarshaller.createUpdateMessage(1, keyValues, true);
//			
//			initTime +=( System.nanoTime() - startInit );
//			
//			byte[] bytes = mess.toByteArray();
//			
//			totalByteSize += bytes.length;
//			
//			Message mess2 = Message.parseFrom(bytes);
//			
//			anchor += mess2.getUpdateMessage().getData().getBDEntries(0).getSerializedSize();
		}
		
		long endTime = System.nanoTime();
		
		long time = endTime - startTime;
		time-=initTime;
		
		time /= 1000;
		
		System.out.println(inIterations+" calculations took "+time+" us, anchor =  "+anchor+" byteSize = "+(totalByteSize / inIterations));
		
		time /= inIterations;
		
		System.out.println("1 calculations took on average "+time+" us \n");
	}
	
	public static void main( String[] inArgs )
	{
		
		try
		{
			testPerformance(20000);
			testPerformance(20000);
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
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
