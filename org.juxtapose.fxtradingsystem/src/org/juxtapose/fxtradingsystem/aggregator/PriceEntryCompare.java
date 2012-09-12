package org.juxtapose.fxtradingsystem.aggregator;

import java.util.Comparator;

import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.data.DataTypeArrayList;
import org.juxtapose.streamline.util.data.DataTypeBigDecimal;

public class PriceEntryCompare implements Comparator<DataTypeArrayList>
{
	boolean bid;

	PriceEntryCompare( boolean inBid )
	{
		bid = inBid;
	}

	@Override
	public int compare( DataTypeArrayList o1, DataTypeArrayList o2 )
	{
		PersistentArrayList<DataTypeBigDecimal> arr1 = (PersistentArrayList<DataTypeBigDecimal>)o1.get();
		PersistentArrayList<DataTypeBigDecimal> arr2 = (PersistentArrayList<DataTypeBigDecimal>)o2.get();

		if( arr1 == null || arr1.size() == 0 || arr2 == null || arr2.size() == 0 )
			return 0;

		if( bid )
			return arr1.get( 0 ).get().compareTo( arr2.get( 0 ).get() );
		else
			return arr2.get( 0 ).get().compareTo( arr1.get( 0 ).get() );
	}
}


