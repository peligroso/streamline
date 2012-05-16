package org.juxtapose.streamline.experimental.protocol.message;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeBigDecimal;
import org.juxtapose.streamline.util.data.DataTypeBoolean;
import org.juxtapose.streamline.util.data.DataTypeLong;
import org.juxtapose.streamline.util.data.DataTypeNull;
import org.juxtapose.streamline.util.data.DataTypeRef;
import org.juxtapose.streamline.util.data.DataTypeString;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

public class DataSerializer
{
	static final byte BOOLEAN_TRUE 			= 1;
	static final byte BOOLEAN_FALSE 		= 2;
	static final byte NUMBER_LONG 			= 3;
	static final byte NUMBER_INT 			= 4;
	static final byte NUMBER_SHORT 			= 5;
	static final byte NUMBER_BYTE 			= 6;
	static final byte NUMBER_LONG_NEG 		= 7;
	static final byte NUMBER_INT_NEG 		= 8;
	static final byte NUMBER_SHORT_NEG 		= 9;
	static final byte NUMBER_BYTE_NEG 		= 10;
	static final byte STRING		 		= 11;
	static final byte BIG_DEC 				= 12;
	static final byte REF	 				= 13;
	static final byte DATA	 				= 14;
	static final byte NULL	 				= 15;
	
	public static final int STRING_DESC_BYTE_LENGTH = 4;
	public static final int FIELD_BYTE_LENGTH = 4;
	
	
	public static final int getNumberByteCount( byte inKey )
	{
		int ret = 0;
		switch( inKey )
		{
		case NUMBER_BYTE_NEG : ret = 1; break;
		case NUMBER_BYTE : ret = 1; break;
		case NUMBER_SHORT_NEG : ret = 2; break;
		case NUMBER_SHORT : ret = 2; break;
		case NUMBER_INT_NEG : ret = 4; break;
		case NUMBER_INT : ret = 4; break;
		case NUMBER_LONG_NEG : ret = 8; break;
		case NUMBER_LONG : ret = 8; break;
		default:
			break;
		}
		
		return ret;
	}
	
	public static final void main( String... inArg )
	{
//		IPersistentMap<String, DataType<?>> map = PersistentHashMap.emptyMap();
//		map = map.assoc( "1", new DataTypeLong( 12l ) );
//		map = map.assoc( "2", new DataTypeBoolean( true ) );
//		map = map.assoc( "3", new DataTypeString("hej och hå") );
//		map = map.assoc( "4", new DataTypeBoolean( false ) );
//		map = map.assoc( "5", new DataTypeLong( 12345l ) );
//		map = map.assoc( "1", new DataTypeLong( -123456789l ) );
//		map = map.assoc( "6", new DataTypeBigDecimal( new BigDecimal( -0.0023456789, new MathContext( 5, RoundingMode.HALF_EVEN) )));
//		
//		IPersistentMap<String, DataType<?>> subMap = PersistentHashMap.emptyMap();
//		subMap = subMap.assoc( "1", new DataTypeLong( 1l ) );
//		subMap = subMap.assoc( "2", new DataTypeBoolean( false ) );
//		
//		PublishedData pd = new PublishedData(subMap, new HashSet(), null, null, null, 1, true);
//		DataTypeRef ref = new DataTypeRef(null, pd);
//		
//		map = map.assoc( "7", ref );
//		
//		byte[] bytes = serializeData( map );
//		
//		map = unSerializeData( bytes );
//		
//		Iterator<Map.Entry<String,DataType<?>>> iter = map.iterator();
//		
//		while( iter.hasNext() )
//		{
//			Map.Entry<String,DataType<?>> entry = iter.next();
//			if( entry.getValue() instanceof DataTypeRef )
//			{
//				Iterator<Map.Entry<String,DataType<?>>> subIter = ((DataTypeRef)entry.getValue()).getReferenceData().getDataMap().iterator();
//				while( subIter.hasNext() )
//				{
//					Map.Entry<String,DataType<?>> subEntry = subIter.next();
//					System.out.println("---key: "+subEntry.getKey()+" has value: "+subEntry.getValue());
//				}
//			}
//			else
//				System.out.println("key: "+entry.getKey()+" has value: "+entry.getValue());
//		}
//		
//		
//		Map<String, String> query = new HashMap<String, String>();
//		query.put("CCY1", "SEK");
//		query.put("CCY2", "NOK");
//		query.put("TYPE", "SP");
//		query.put("CPTY", "1");
//		
//		bytes = serializeQuery( query );
//		
//		query = unserializeQuery( bytes );
//		
//		for( Map.Entry<String, String> entry : query.entrySet() )
//		{
//			System.out.println( entry.getKey()+" = "+entry.getValue() );
//		}
	}
	
	//BOOLEAN
	public static final byte[] serializeBoolean( Integer inField, boolean inBool )
	{
		byte[] bytes = inBool ? new byte[]{BOOLEAN_TRUE} : new byte[]{ BOOLEAN_FALSE };
		
		return bytes;
		
	}
	//Number
	public static final byte[] serializeNumber( long number )
	{
		int sign = Long.signum( number );
		
		byte[] ret = getNumberProperties( number );
		
		if( sign == -1 )
			number *= -1;
		
		for( int i = ret.length-1; i > 0; i-- )
		{
			int shift = (ret.length-1) - (i);
			ret[i] = (byte)(number >>> 8 * shift);
		}
		
		return ret;
	}
	
	public static final byte[] serializeInt( int number )
	{
		byte[] ret = new byte[4];
		
		for( int i = ret.length-1; i > 0; i-- )
		{
			int shift = (ret.length-1) - (i);
			ret[i] = (byte)(number >>> 8 * shift);
		}
		
		return ret;
	}
	
	
	public static long numberFromByteArray( byte[] inBytes, int inOffset, int inLength, Integer inDescriptionByte )
	{
		long ret = 0;
		
		
		for(int i = inOffset; i < inOffset+inLength; i++){      
			  ret <<= 8;  
			  ret ^= (long)inBytes[i] & 0xFF;      
		 }
		
		if( inDescriptionByte != null )
		{
			if( isNegativeNumber( inBytes[0] ) )
				ret *= -1;
		}
		
		return ret;
	}
	
	
	public static byte[] getByteArrayFrame( byte[] inField, int inLength )
	{
		byte[] bytes = new byte[ inField.length + inLength ];
		System.arraycopy(inField, 0, bytes, 0, inField.length);
		
		return bytes;
	}
	
	public static byte[] serializeBigDecimal( byte[] inField, BigDecimal inBigDecimal )
	{
		//[Field, BIG_DEC, unscaledValue, lengthOfUnscaledValue, scale]
		BigInteger theInt = inBigDecimal.unscaledValue();
		int scale = inBigDecimal.scale();

		byte[] intBytes = theInt.toByteArray();
		//			byte[] bytes = new byte[ 1 + 4 + intBytes.length + 4 ];
		byte[] bytes = getByteArrayFrame(inField, intBytes.length + 9);

		bytes[inField.length] = BIG_DEC;
		serializeInt( bytes, inField.length+1, intBytes.length );
		System.arraycopy( intBytes, 0, bytes, inField.length+5, intBytes.length );
		serializeInt( bytes, inField.length+5+intBytes.length, scale);
		return bytes;
	}
	
	public static byte[] serializeBoolean( byte[] inField, Boolean inBoolean )
	{
		byte[] bytes = getByteArrayFrame(inField, 1);
		bytes[inField.length] = inBoolean ? BOOLEAN_TRUE : BOOLEAN_FALSE;
		
		return bytes;
	}
	
	public static byte[] serializeLong( byte[] inField, Long inLong )
	{
		long number = inLong;
		int sign = Long.signum( number );
		byte[] numberProps = getNumberProperties( number );
		
		byte[] bytes = getByteArrayFrame(inField, numberProps.length);
		
		bytes[inField.length] = numberProps[0];
				
		if( sign == -1 )
			number *= -1;
		
		for( int i = bytes.length-1; i > inField.length; i-- )
		{
			int shift = (bytes.length-1) - (i);
			bytes[i] = (byte)(number >>> 8 * shift);
		}
		
		return bytes;
	}
	
	public static byte[] serializeRef( byte[] inField, DataTypeRef inRef )
	{
		byte[] mapBytes = DataSerializer.serializeData( inRef.getReferenceData().getDataMap() );
		byte[] bytes = getByteArrayFrame(inField, mapBytes.length+5);
		bytes[inField.length] = REF;
		serializeInt( bytes, inField.length+1, mapBytes.length );
		System.arraycopy( mapBytes, 0, bytes, inField.length+5, mapBytes.length );
		
		return bytes;
	}
	
	public static byte[] serializeString( byte[] inField, String inString )
	{
		byte[] strBytes = inString.getBytes();
		byte[] bytes = getByteArrayFrame(inField, strBytes.length+5);
		bytes[inField.length] = STRING;
		serializeInt( bytes, inField.length+1, strBytes.length );
		System.arraycopy( strBytes, 0, bytes, inField.length+5, strBytes.length );
		
		return bytes;
	}
	
	public static final byte[] serializeNull( byte[] inField )
	{
		byte[] bytes = getByteArrayFrame(inField, 1);
		bytes[inField.length] = NULL;
		
		return bytes;
		
	}

	/**
	 * @param inField
	 * @param inDataEntry
	 * @return
	 */
	public static final byte[] serializeDataEntry( byte[] inField, DataType<?> inDataEntry )
	{
		if( inDataEntry instanceof DataTypeBigDecimal )
		{
			return serializeBigDecimal(inField, ((DataTypeBigDecimal)inDataEntry).get() );
		}
		else if( inDataEntry instanceof DataTypeBoolean )
		{
			return serializeBoolean( inField, ((DataTypeBoolean)inDataEntry).get() );
		}
		else if( inDataEntry instanceof DataTypeLong )
		{
			return serializeLong( inField, ((DataTypeLong)inDataEntry).get() );
		}
		else if( inDataEntry instanceof DataTypeRef )
		{
			return serializeRef( inField, (DataTypeRef)inDataEntry );
		}
		else if( inDataEntry instanceof DataTypeString )
		{
			return serializeString( inField, ((DataTypeString)inDataEntry).get() );
		}
		else if( inDataEntry instanceof DataTypeNull )
		{
			return serializeNull( inField );
		}
		else
		{
			throw new IllegalArgumentException(" No serialization for "+inDataEntry.getClass() );
		}
	}
	/**
	 * @param inData
	 * @return
	 */
	public static final byte[] serializeData( IPersistentMap<String, DataType<?>> inData )
	{
		Iterator<Map.Entry<String,DataType<?>>> iter = inData.iterator();
		
		byte[][] byteArrays = new byte[inData.count()][];
		
		int i = 0;
		int totalBytes = 0;
		while( iter.hasNext() )
		{
			Map.Entry<String,DataType<?>> entry = iter.next();
			
			byte[] fieldBytes = serializeString( entry.getKey() );
			
			byte[] bytes = serializeDataEntry( fieldBytes, entry.getValue() );
			byteArrays[ i ] = bytes;
			
			i++;
			totalBytes += bytes.length;
		}
		
		byte[] bytes = new byte[totalBytes];
		
		int offSet = 0;
		for( byte[] byteArr : byteArrays )
		{
			System.arraycopy( byteArr, 0, bytes, offSet, byteArr.length );
			offSet+=byteArr.length;
		}
		
		return bytes;
	}
	
	public static final IPersistentMap<String, DataType<?>> unSerializeData( byte[] inBytes )
	{
		IPersistentMap<String, DataType<?>> map = PersistentHashMap.emptyMap();
		
		int cursor = 0;
		
		do
		{
			int fieldLength = (int)numberFromByteArray( inBytes, cursor, FIELD_BYTE_LENGTH, null );
			
			cursor+=4;
			
			byte[] fieldCharBytes = ArrayUtils.subarray( inBytes, cursor, cursor+fieldLength );
			
			String field = new String(fieldCharBytes);
			
			cursor += fieldCharBytes.length;
			
			if( inBytes[cursor] == BOOLEAN_TRUE )
			{
				map = map.assoc( field, new DataTypeBoolean( true ) );
				cursor++;
			}
			else if( inBytes[cursor] == BOOLEAN_FALSE )
			{
				map = map.assoc( field, new DataTypeBoolean( false ) );
				cursor++;
			}
			else if( isNumber( inBytes[cursor] ))
			{
				int length = getNumberByteCount( inBytes[cursor] );
				long number = numberFromByteArray( inBytes, cursor+1, length, cursor );
				
				cursor+= length+1;
				
				map = map.assoc( field, new DataTypeLong( number ));
			}
			else if( inBytes[cursor] == STRING )
			{
				cursor++;
				int stringLenght = (int)numberFromByteArray( inBytes, cursor, STRING_DESC_BYTE_LENGTH, null );
				cursor+= STRING_DESC_BYTE_LENGTH;
				
				byte[] charBytes = ArrayUtils.subarray( inBytes, cursor, cursor+stringLenght );
				
				String str = new String(charBytes);
				
				cursor += charBytes.length;
				
				map = map.assoc( field, new DataTypeString( str ) );
			}
			else if( inBytes[cursor] == BIG_DEC )
			{
				cursor++;
				int intLenght = (int)numberFromByteArray( inBytes, cursor, 4, null );
				cursor+= 4;
				
				byte[] bdBytes = ArrayUtils.subarray( inBytes, cursor, cursor+intLenght );
				
				BigInteger bi = new BigInteger(bdBytes);
				
				cursor += bdBytes.length;
				
				int scale = (int)numberFromByteArray( inBytes, cursor, 4, null );
				DataTypeBigDecimal bd = new DataTypeBigDecimal( new BigDecimal(bi, scale) );
				
				cursor += 4;
				
				map = map.assoc( field, bd );
			}
			else if( inBytes[cursor] == REF )
			{
				cursor++;
				int mapLenght = (int)numberFromByteArray( inBytes, cursor, 4, null );
				cursor+= 4;
				
				byte[] mapBytes = ArrayUtils.subarray( inBytes, cursor, cursor+mapLenght );
				
				IPersistentMap<String, DataType<?>> subMap = unSerializeData( mapBytes );
//				PublishedData pd = new PublishedData(subMap, new HashSet(), null, null, null, 1, true);
//				
//				DataTypeRef ref = new DataTypeRef(null, pd);
//				
//				map = map.assoc( field, ref );
				
				cursor+= mapBytes.length;
			}
		}
		while( cursor < inBytes.length );
		
		return map;
	}
	
	
	public static final byte[] serializeQuery( Map<String, String> inQuery )
	{
		byte[] bytes = null;
		int offset = 0;
		
		for( Map.Entry<String, String> entry : inQuery.entrySet() )
		{
			byte[] key = serializeString(entry.getKey());
			byte[] value = serializeString(entry.getValue());
			
			if( bytes == null )
				bytes = new byte[key.length + value.length];
			else
			{
				byte[] newBytes = new byte[bytes.length+key.length+value.length];
				System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
				bytes = newBytes;
			}
			System.arraycopy( key, 0, bytes, offset, key.length);
			offset += key.length;
			System.arraycopy( value, 0, bytes, offset, value.length);
			offset+= value.length;
		}
		
		return bytes;
	}
	
	public static final Map<String, String> unserializeQuery( byte[] inQueryBytes )
	{
		HashMap<String, String> queryMap = new HashMap<String, String>(); 
		int offset = 0;
		
		if( inQueryBytes == null || inQueryBytes.length < 4 )
			return queryMap;
		
		do
		{
			int keyLength = (int)numberFromByteArray( inQueryBytes, offset, FIELD_BYTE_LENGTH, null );
			offset += FIELD_BYTE_LENGTH;
			
			byte[] charBytes = ArrayUtils.subarray( inQueryBytes, offset, offset+keyLength );			
			String key = new String(charBytes);
			offset += charBytes.length;
			
			int valueLength = (int)numberFromByteArray( inQueryBytes, offset, FIELD_BYTE_LENGTH, null );
			offset += FIELD_BYTE_LENGTH;
			
			byte[] valueCharBytes = ArrayUtils.subarray( inQueryBytes, offset, offset+valueLength );			
			String value = new String( valueCharBytes );
			offset += valueCharBytes.length;
			
			queryMap.put( key, value );
		}
		while( offset < inQueryBytes.length );
			
		return queryMap;
		
	}
	
	/**
	 * @param inString
	 * @return
	 */
	public static final byte[] serializeString( String inString )
	{
		assert (inString.length() < Integer.MAX_VALUE ) : "String is to big.. larger than Integer.MAX_VALUE";
		
		byte[] strBytes = inString.getBytes();
		byte[] bytes = new byte[strBytes.length+4];
		
		serializeInt(bytes, 0, strBytes.length);
		System.arraycopy(strBytes, 0, bytes, 4, strBytes.length);
		
		return bytes;
	}
	
	/**
	 * @param inBytes
	 * @param inOffSet
	 * @param number
	 * @return
	 */
	public static final byte[] serializeInt( byte[] inBytes, int inOffSet, int number )
	{
		assert (inBytes.length - inOffSet) <= 4 : "integer will not fit into byte array from offset";
		
		for( int i = 3; i >= 0; i-- )
		{
			int shift = (3) - (i);
			inBytes[inOffSet + i] = (byte)(number >>> 8 * shift);
		}
		return inBytes;
	}
	
	/**
	 * @param inNumber
	 * @return
	 */
	public static final byte[] getNumberProperties( long inNumber )
	{
		if( inNumber < Integer.MIN_VALUE )
		{
			byte[] ret = new byte[9];
			ret[0] = NUMBER_LONG_NEG;
			return ret;
		}
		else if( inNumber < Short.MIN_VALUE )
		{
			byte[] ret = new byte[5];
			ret[0] = NUMBER_INT_NEG;
			return ret;
		}
		else if( inNumber < Byte.MIN_VALUE )
		{
			byte[] ret = new byte[3];
			ret[0] = NUMBER_SHORT_NEG;
			return ret;
		}
		else if( inNumber < 0 )
		{
			byte[] ret = new byte[2];
			ret[0] = NUMBER_BYTE_NEG;
			return ret;
		}
		else if( inNumber < Byte.MAX_VALUE )
		{
			byte[] ret = new byte[2];
			ret[0] = NUMBER_BYTE;
			return ret;
		}
		else if( inNumber < Short.MAX_VALUE )
		{
			byte[] ret = new byte[3];
			ret[0] = NUMBER_SHORT;
			return ret;
		}
		else if( inNumber < Integer.MAX_VALUE )
		{
			byte[] ret = new byte[5];
			ret[0] = NUMBER_INT;
			return ret;
		}
		else
		{
			byte[] ret = new byte[9];
			ret[0] = NUMBER_LONG;
			return ret;
		}
	}
	
	/**
	 * @param inByte
	 * @return
	 */
	public static final boolean isNumber( byte inByte )
	{
		if( inByte == NUMBER_BYTE || inByte == NUMBER_SHORT || inByte == NUMBER_INT || inByte == NUMBER_LONG || isNegativeNumber( inByte ) )
		{
			return true;
		}
		return false;
	}
	
	public static final boolean isNegativeNumber( byte inByte )
	{
		if( inByte == NUMBER_BYTE_NEG || inByte == NUMBER_SHORT_NEG || inByte == NUMBER_INT_NEG || inByte == NUMBER_LONG_NEG )
		{
			return true;
		}
		return false;
	}

}
