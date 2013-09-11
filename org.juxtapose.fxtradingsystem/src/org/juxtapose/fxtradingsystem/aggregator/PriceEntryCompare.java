package org.juxtapose.fxtradingsystem.aggregator;

import java.math.BigDecimal;
import java.util.Comparator;

import org.juxtapose.streamline.util.PersistentArrayList;

public class PriceEntryCompare implements Comparator<PersistentArrayList<?>>
{
	boolean bid;

	PriceEntryCompare( boolean inBid )
	{
		bid = inBid;
	}

	@Override
	public int compare( PersistentArrayList<?> o1, PersistentArrayList<?> o2 )
	{
		PersistentArrayList<BigDecimal> arr1 = (PersistentArrayList<BigDecimal>)o1;
		PersistentArrayList<BigDecimal> arr2 = (PersistentArrayList<BigDecimal>)o2;

		if( arr1 == null || arr1.size() == 0 || arr2 == null || arr2.size() == 0 )
			return 0;

		if( bid )
			return arr1.get( 0 ).compareTo( arr2.get( 0 ) );
		else
			return arr2.get( 0 ).compareTo( arr1.get( 0 ) );
	}
}


