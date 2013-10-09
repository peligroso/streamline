package org.juxtapose.streamline.tools;

import java.util.Iterator;
import java.util.Map.Entry;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.data.DataTypeLazyRef;
import org.juxtapose.streamline.util.data.DataTypeRef;

import com.trifork.clj_ds.IPersistentMap;

public class STMQueryMethods
{
	public static boolean containsReferences( ISTM inSTM, ISTMEntryKey inContainerKey, String inReferenceKey, String... inFields )
	{
		ISTMEntry entry = inSTM.getData( inContainerKey.getKey() );
		
		if( entry == null )
			return false;
		
		Iterator<Entry<String, Object>> iter = entry.getDataMap().iterator();
		
		while( iter.hasNext() )
		{
			Entry<String, Object> row = iter.next();
			
			Object obj = row.getValue();
			if( obj instanceof DataTypeLazyRef )
			{
				DataTypeLazyRef ref = (DataTypeLazyRef)obj;
				
				ISTMEntry containerEntry = inSTM.getData( ref.get().getKey() );
				
				if( containerEntry == null )
					continue;
				
				for( String field : inFields )
				{
					Object o = containerEntry.getDataMap().valAt( field );
					
					if( o instanceof DataTypeRef )
					{
						ISTMEntryKey existingKey = ((DataTypeRef)o).get();
						if( existingKey.getKey().equals( inReferenceKey ) )
							return true;
					}
					else if( o instanceof DataTypeLazyRef )
					{
						ISTMEntryKey existingKey = ((DataTypeLazyRef)o).get();
						if( existingKey.getKey().equals( inReferenceKey ) )
							return true;
					}
				}
			}
		}
		return false;
	}
}
