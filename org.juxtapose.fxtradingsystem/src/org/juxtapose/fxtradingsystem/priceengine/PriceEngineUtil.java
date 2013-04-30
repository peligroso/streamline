package org.juxtapose.fxtradingsystem.priceengine;

import java.math.BigDecimal;
import java.util.HashMap;

import org.juxtapose.fxtradingsystem.constants.FXDataConstants;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeLong;

public class PriceEngineUtil
{
	public static HashMap<String, String> getSpotPriceQuery( String inCcy1, String inCcy2 )
	{
		HashMap<String, String> query = new HashMap<String, String>();
		query.put( PriceEngineDataConstants.FIELD_TYPE, PriceEngineDataConstants.STATE_TYPE_PRICE );
		query.put( PriceEngineDataConstants.FIELD_INSTRUMENT, PriceEngineDataConstants.STATE_INSTRUMENT_SPOT );
		query.put( FXDataConstants.FIELD_CCY1, inCcy1 );
		query.put( FXDataConstants.FIELD_CCY2, inCcy2 );
		
		return query;
	}
	
	public static HashMap<String, String> getFwdPriceQuery( String inCcy1, String inCcy2, String inPeriod )
	{
		HashMap<String, String> query = new HashMap<String, String>();
		query.put( PriceEngineDataConstants.FIELD_TYPE, PriceEngineDataConstants.STATE_TYPE_PRICE );
		query.put( PriceEngineDataConstants.FIELD_INSTRUMENT, PriceEngineDataConstants.STATE_INSTRUMENT_FWD );
		query.put( FXDataConstants.FIELD_CCY1, inCcy1 );
		query.put( FXDataConstants.FIELD_CCY2, inCcy2 );
		query.put( FXDataConstants.FIELD_PERIOD, inPeriod );
		
		return query;
	}
	
	public static HashMap<String, String> getSwapPriceQuery( String inCcy1, String inCcy2, String inPeriod1, String inPeriod2 )
	{
		HashMap<String, String> query = new HashMap<String, String>();
		query.put( PriceEngineDataConstants.FIELD_TYPE, PriceEngineDataConstants.STATE_TYPE_PRICE );
		query.put( PriceEngineDataConstants.FIELD_INSTRUMENT, PriceEngineDataConstants.STATE_INSTRUMENT_SWAP );
		query.put( FXDataConstants.FIELD_CCY1, inCcy1 );
		query.put( FXDataConstants.FIELD_CCY2, inCcy2 );
		
		return query;
	}
	
	/**
	 * @param inData
	 * @return
	 */
	public static BigDecimal[] getBidAskFromData( ISTMEntry inData )
	{
		if( inData == null )
			return null;
		
		DataType<?> bid = inData.getValue( FXDataConstants.FIELD_BID );
		DataType<?> ask = inData.getValue( FXDataConstants.FIELD_ASK );
		
		if( bid == null || ask == null )
			return null;

		BigDecimal bidVal = (BigDecimal)bid.get();
		BigDecimal askVal = (BigDecimal)ask.get();
		
		return new BigDecimal[]{bidVal, askVal};
	}
	
	/**
	 * @param inData
	 * @return
	 */
	public static Long getDecimals( ISTMEntry inData )
	{
		if( inData == null )
			return null;
		
		DataTypeLong dec = (DataTypeLong)inData.getValue( FXDataConstants.FIELD_DECIMALS );
		
		if( dec == null )
			return null;

		return dec.get();
	}
}
