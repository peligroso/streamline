package org.juxtapose.streamline.tools;

import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeArrayList;

public class CollectionMethods
{
	public static boolean contains( DataTypeArrayList inList, DataType<?> inDataType )
	{
		if( inList == null )
			return false;
		
		PersistentArrayList<DataType<?>> list = (PersistentArrayList<DataType<?>>) inList.get();
		
		for( int i = 0; i < list.size(); i++ )
		{
			DataType<?> data = list.get( i );
			
			if( data.equals( inDataType ))
				return true;
		}
		
		return false;
	}
}
