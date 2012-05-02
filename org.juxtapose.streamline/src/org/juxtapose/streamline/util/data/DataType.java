package org.juxtapose.streamline.util.data;

/**
 * @author Pontus Jörgne
 * Dec 30, 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 * @param <T>
 */
public abstract class DataType<T> {
	
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
	
	final T m_value;
	
	DataType( T inValue )
	{
		m_value = inValue;
	}
	
	public T get()
	{
		return m_value;
	}
	
	public String toString()
	{
		return get().toString();
	}
	
	public boolean equals( DataType<?> inObject )
	{
		return get().equals( inObject.get() );
	}
	
	public byte[] serialize( byte[] inField )
	{
		return new byte[]{};
	}
	
	public byte[] getByteArrayFrame( byte[] inField, int inLength )
	{
		byte[] bytes = new byte[ inField.length + inLength ];
		System.arraycopy(inField, 0, bytes, 0, inField.length);
		
		return bytes;
	}
	
	/**Serialization methods**/
	
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
	
	public static final byte[] serializeString( String inString )
	{
		assert (inString.length() < Integer.MAX_VALUE ) : "String is to big.. larger than Integer.MAX_VALUE";
		
		byte[] strBytes = inString.getBytes();
		byte[] bytes = new byte[strBytes.length+4];
		
		serializeInt(bytes, 0, strBytes.length);
		System.arraycopy(strBytes, 0, bytes, 4, strBytes.length);
		
		return bytes;
	}


}
