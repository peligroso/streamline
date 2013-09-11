package org.juxtapose.fxtradingsystem.ordermanager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import org.juxtapose.fxtradingsystem.constants.FXDataConstants;
import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.STMTransaction;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.Status;

public class RFQLiquidityProducer extends STMEntryProducer implements ISTMEntryRequestSubscriber
{
	final static long priceTag = 0;
	final String ccy1;
	final String ccy2;
	final String nearPeriod;
	final String farPeriod;
	final BigDecimal amt;
	
	ISTMEntryKey subscribeKey;
	
	public RFQLiquidityProducer( ISTMEntryKey inKey, ISTM inSTM, String inCcy1, String inCcy2, String inNearPeriod, String inFarPeriod, BigDecimal inAmt )
	{
		super( inKey, inSTM );
		ccy1 = inCcy1;
		ccy2 = inCcy2;
		nearPeriod = inNearPeriod;
		farPeriod = inFarPeriod;
		amt = inAmt;
	}

	@Override
	protected void start()
	{
		stm.commit( new STMTransaction( entryKey, RFQLiquidityProducer.this, 1, 0, true )
		{
			@Override
			public void execute()
			{
				putValue( FXDataConstants.FIELD_CCY1, ccy1 );
				putValue( FXDataConstants.FIELD_CCY2, ccy2 );
				
				if( nearPeriod != null )
					putValue( FXDataConstants.FIELD_NEAR_SWAP, nearPeriod);
				if( farPeriod != null )
					putValue( FXDataConstants.FIELD_FAR_SWAP, farPeriod);
			}
		});
		
		HashMap<String, String> query = new HashMap<String, String>();
		query.put( FXDataConstants.FIELD_INSTRUMENT, FXDataConstants.STATE_INSTRUMENT_SPOT );
		query.put( FXDataConstants.FIELD_CCY1, ccy1 );
		query.put( FXDataConstants.FIELD_CCY2, ccy2 );
		query.put( FXDataConstants.FIELD_TYPE, PriceEngineDataConstants.STATE_TYPE_LIQUIDITY );
		query.put( FXDataConstants.FIELD_SOURCE, "*" );
		
		stm.getDataKey( FXProducerServiceConstants.AGGREGATOR, RFQLiquidityProducer.this, priceTag, query );
	}

	@Override
	public void deliverKey( final ISTMEntryKey inDataKey, Object inTag)
	{
		if( inTag.equals( priceTag ))
		{
			subscribeKey = inDataKey;
			stm.subscribeToData( inDataKey, this );
		}
	}

	@Override
	public void queryNotAvailible(Object inTag)
	{
		setStatus( Status.NA );
		stm.logError( "could not retrieve datakey from price engine" );
		return;
	}
	
	public void updateData( ISTMEntryKey inKey, final ISTMEntry inData, boolean inFullUpdate )
	{
		if( inData.getStatus() == Status.ON_REQUEST )
			return;
		
		stm.commit( new STMTransaction( entryKey, RFQLiquidityProducer.this, inFullUpdate )
		{
			@Override
			public void execute()
			{
				PersistentArrayList<Object> bidSide = (PersistentArrayList<Object>)inData.getValue( FXDataConstants.FIELD_BID );
				PersistentArrayList<Object> askSide = (PersistentArrayList<Object>)inData.getValue( FXDataConstants.FIELD_ASK );
				
				if( bidSide == null || askSide == null )
				{
					dispose();
					return;
				}
				BigDecimal bid = getBestExecutablePrice( bidSide, amt, true );
				BigDecimal ask = getBestExecutablePrice( askSide, amt, false );
				
				if( bid == null || ask == null )
				{
					dispose();
					return;
				}
				
				putValue( FXDataConstants.FIELD_BID, bid );
				putValue( FXDataConstants.FIELD_ASK, ask );
				
				putValue( DataConstants.FIELD_TIMESTAMP, inData.getValue( DataConstants.FIELD_TIMESTAMP ) );
				
				Boolean priced = (Boolean)get( FXDataConstants.FIELD_FIRST_UPDATE );
				
				if( priced == null )
				{
					putValue( FXDataConstants.FIELD_FIRST_UPDATE, new Boolean(true) );
				}
				else if( priced )
				{
					putValue( FXDataConstants.FIELD_FIRST_UPDATE, new Boolean(false) );
				}
				
				if( getStatus() == Status.ON_REQUEST )
				{
					setStatus( Status.OK );
				}
					
			}
		} );
	}
	

	//static aggergator methods
	private static BigDecimal getBestExecutablePrice( PersistentArrayList<Object> inEntries, BigDecimal inAmt, boolean inBid )
	{
		BigDecimal workPrice = BigDecimal.ZERO;
		BigDecimal amtLeft = inAmt;
		
		boolean done = false;
		
		for( int i = 0; i < inEntries.size(); i++ )
		{
			PersistentArrayList<Object> arrayList = (PersistentArrayList<Object>)inEntries.get(i);
			BigDecimal price = (BigDecimal)arrayList.get( 0 );
			BigDecimal size = (BigDecimal)arrayList.get( 1 );
			
			if( size.compareTo( amtLeft ) > 0 )
			{
				BigDecimal multiplicator = price.multiply( amtLeft );
				workPrice = workPrice.add( multiplicator );
				done = true;
				break;
			}
			else
			{
				BigDecimal multiplicator = price.multiply( size );
				workPrice = workPrice.add( multiplicator );
				amtLeft = amtLeft.subtract( size );
			}
		}
		
		if( !done )
			return null;
		
		RoundingMode rm = inBid ? RoundingMode.FLOOR : RoundingMode.CEILING;
		
		workPrice = workPrice.divide( inAmt, 4, rm );
		return workPrice;
	}
	
	protected void stop()
	{
		super.stop();
		stm.unsubscribeToData( subscribeKey, this );
	}
	
}
