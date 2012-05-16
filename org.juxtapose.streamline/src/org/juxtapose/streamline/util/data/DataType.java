package org.juxtapose.streamline.util.data;

/**
 * @author Pontus Jörgne
 * Dec 30, 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 * @param <T>
 */
public abstract class DataType<T> {
	
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
		throw new IllegalAccessError( "This method has not been properly subclassed by datatype");
	}
	
	public byte[] getByteArrayFrame( byte[] inField, int inLength )
	{
		byte[] bytes = new byte[ inField.length + inLength ];
		System.arraycopy(inField, 0, bytes, 0, inField.length);
		
		return bytes;
	}
	
}