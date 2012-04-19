package org.juxtapose.streamline.producer;

public interface IDataKey
{
	public String getKey( );
	public String getValue( Integer inKey );
	public Integer getService();
	public String getType( );
	public String getSingleValue();
}
