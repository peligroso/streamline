package org.juxtapose.streamline.tools;

import org.juxtapose.streamline.util.PersistentArrayList;

public class CollectionMethods
{
	public static boolean contains( PersistentArrayList inList, Object inDataType )
	{
		if( inList == null )
			return false;
		
		for( int i = 0; i < inList.size(); i++ )
		{
			Object data = inList.get( i );
			
			if( data.equals( inDataType ))
				return true;
		}
		
		return false;
	}
	
}
