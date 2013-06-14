package org.juxtapose.streamline.producer;

import java.util.Set;

public interface ISTMEntryKey
{
	public String getKey( );
	public String getValue( String inKey );
	public String getService();
	public String getType( );
	public String getSingleValue();
	public Set<String> getKeys();
	public String toString();
}
