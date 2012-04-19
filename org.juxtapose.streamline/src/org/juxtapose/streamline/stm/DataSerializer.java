package org.juxtapose.streamline.stm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeBigDecimal;
import org.juxtapose.streamline.util.data.DataTypeBoolean;
import org.juxtapose.streamline.util.data.DataTypeLong;
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
	
	static final int STRING_DESC_BYTE_LENGTH = 4;
	static final int FIELD_BYTE_LENGTH = 4;
	
	
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
	public static final boolean isNegativeNumber( byte inByte )
	{
		if( inByte == NUMBER_BYTE_NEG || inByte == NUMBER_SHORT_NEG || inByte == NUMBER_INT_NEG || inByte == NUMBER_LONG_NEG )
		{
			return true;
		}
		return false;
	}
	
	public static final boolean isNumber( byte inByte )
	{
		if( inByte == NUMBER_BYTE || inByte == NUMBER_SHORT || inByte == NUMBER_INT || inByte == NUMBER_LONG || isNegativeNumber( inByte ) )
		{
			return true;
		}
		return false;
	}
	
	public static final byte[] getNumberProperties( long inNumber )
	{
		if( inNumber < Integer.MIN_VALUE )
		{
			return new byte[]{ NUMBER_LONG_NEG, 0, 0, 0, 0, 0, 0, 0, 0 };
		}
		else if( inNumber < Short.MIN_VALUE )
		{
			return new byte[]{ NUMBER_INT_NEG, 0, 0, 0, 0 };
		}
		else if( inNumber < Byte.MIN_VALUE )
		{
			return new byte[]{ NUMBER_SHORT_NEG, 0, 0 };
		}
		else if( inNumber < 0 )
		{
			return new byte[]{ NUMBER_BYTE_NEG, 0 };
		}
		else if( inNumber < Byte.MAX_VALUE )
		{
			return new byte[]{ NUMBER_BYTE, 0 };
		}
		else if( inNumber < Short.MAX_VALUE )
		{
			return new byte[]{ NUMBER_SHORT, 0, 0 };
		}
		else if( inNumber < Integer.MAX_VALUE )
		{
			return new byte[]{ NUMBER_INT, 0, 0, 0, 0};
		}
		else
		{
			return new byte[]{ NUMBER_LONG, 0, 0, 0, 0, 0, 0, 0, 0 };
		}
	}

	
	public static final void main( String... inArg )
	{
		IPersistentMap<Integer, DataType<?>> map = PersistentHashMap.emptyMap();
		map = map.assoc( 1, new DataTypeLong( 12l ) );
		map = map.assoc( 2, new DataTypeBoolean( true ) );
		map = map.assoc( 3, new DataTypeString("hej och hå") );
		map = map.assoc( 4, new DataTypeBoolean( false ) );
		map = map.assoc( 5, new DataTypeLong( 12345l ) );
		map = map.assoc( 1, new DataTypeLong( 123456789l ) );
		map = map.assoc( 6, new DataTypeBigDecimal( new BigDecimal( 1.23456789, new MathContext( 5, RoundingMode.HALF_EVEN) )));
		
		byte[] bytes = serialize( map );
		
		map = unSerialize( bytes );
		
		Iterator<Map.Entry<Integer,DataType<?>>> iter = map.iterator();
		
		while( iter.hasNext() )
		{
			Map.Entry<Integer,DataType<?>> entry = iter.next();
			System.out.println("key: "+entry.getKey()+" has value: "+entry.getValue());
		}
		
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
	
	public static final byte[] serializeInt( byte[] inBytes, int inOffSet, int number )
	{
		assert (inBytes.length - inOffSet) <= 4 : "integer will not fit into byte array from offset";
		
		for( int i = inOffSet + inBytes.length-1; i > 0; i-- )
		{
			int shift = (inBytes.length-1) - (i);
			inBytes[i] = (byte)(number >>> 8 * shift);
		}
		
		return inBytes;
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
	
	public static final byte[] serializeString( String inString )
	{
		byte[] strBytes = inString.getBytes();
		byte[] size = serializeInt( strBytes.length );
		byte[] ret = new byte[ strBytes.length + size.length + 1 ];
		System.arraycopy( size, 0, ret, 1, size.length );
		System.arraycopy( strBytes, 0, ret, size.length+1, strBytes.length );
		ret[0] = STRING;
		
		return ret;
	}
	
	public static final byte[] serialize( IPersistentMap<Integer, DataType<?>> inData )
	{
		Iterator<Map.Entry<Integer,DataType<?>>> iter = inData.iterator();
		
		byte[][] byteArrays = new byte[inData.count()][];
		
		int i = 0;
		int totalBytes = 0;
		while( iter.hasNext() )
		{
			Map.Entry<Integer,DataType<?>> entry = iter.next();
			
			byte[] bytes = entry.getValue().serialize( entry.getKey() );
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
	
	public static final IPersistentMap<Integer, DataType<?>> unSerialize( byte[] inBytes )
	{
		IPersistentMap<Integer, DataType<?>> map = PersistentHashMap.emptyMap();
		
		int cursor = 0;
		
		do
		{
			int field = (int)numberFromByteArray( inBytes, cursor, FIELD_BYTE_LENGTH, null );
			
			cursor+=4;
			
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
				int stringLenght = (int)numberFromByteArray( inBytes, cursor, 4, null );
				cursor+= 4;
				
				byte[] bdBytes = ArrayUtils.subarray( inBytes, cursor, cursor+stringLenght );
				
				BigInteger bi = new BigInteger(bdBytes);
				DataTypeBigDecimal bd = new DataTypeBigDecimal( new BigDecimal(bi, 0) );
				
				cursor += bdBytes.length;
				
				map = map.assoc( field, bd );
			}
		}
		while( cursor < inBytes.length );
		
		return map;
	}

}
