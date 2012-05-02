package org.juxtapose.streamline.producer;

public interface IDataKey
{
	public String getKey( );
	public String getValue( String inKey );
	public String getService();
	public String getType( );
	public String getSingleValue();
}
