package org.juxtapose.fxtradingsystem.priceengine;

import static org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants.STATE_EUR;
import static org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants.STATE_SEK;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;

import org.juxtapose.fxtradingsystem.constants.FXDataConstants;
import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.fxtradingsystem.marketdata.MarketDataConstants;
import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.stm.DataTransaction;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.DataConstants;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeBigDecimal;
import org.juxtapose.streamline.util.data.DataTypeLong;

/**
 * @author Pontus Jörgne
 * 6 okt 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class SwapPriceProducer  extends STMEntryProducer implements ISTMEntryRequestSubscriber
{
	final long reutersTag = 0;
	final long bloombergTag = 1;
	
	ISTMEntryKey reutersDataKey;
	ISTMEntryKey bloombergDataKey;
	
	final String ccy1;
	final String ccy2;
	final String period;
	
	public SwapPriceProducer(ISTMEntryKey inKey, String inCcy1, String inCcy2, String inPeriod, ISTM inSTM)
	{
		super( inKey, inSTM );
		ccy1 = inCcy1;
		ccy2 = inCcy2;
		period = inPeriod;
	}
	
	public void subscribe()
	{
		HashMap<String, String> query = new HashMap<String, String>();
		query.put( MarketDataConstants.FIELD_TYPE, MarketDataConstants.STATE_TYPE_INSTRUMENT );
		query.put( MarketDataConstants.FIELD_CCY1, ccy1 );
		query.put( MarketDataConstants.FIELD_CCY2, ccy2 );
		query.put( MarketDataConstants.FIELD_PERIOD, FXDataConstants.STATE_PERIOD_SP );
		query.put( MarketDataConstants.FIELD_SOURCE, "REUTERS" );
		
		PriceEngineUtil.getSpotPriceQuery( STATE_EUR, STATE_SEK );
		stm.getDataKey( FXProducerServiceConstants.MARKET_DATA, this, reutersTag, query );
		
		HashMap<String, String> queryB = new HashMap<String, String>();
		queryB.putAll( query );
		queryB.put( MarketDataConstants.FIELD_SOURCE, "BLOOMBERG" );
		
		stm.getDataKey( FXProducerServiceConstants.MARKET_DATA, this, bloombergTag, queryB );
	}
	
	@Override
	public void deliverKey(ISTMEntryKey inDataKey, Object inTag)
	{
		if( inTag.equals( reutersTag ) )
		{
			reutersDataKey = inDataKey;
			stm.subscribeToData( reutersDataKey, this );
		}
		else if( inTag.equals( bloombergTag ) )
		{
			bloombergDataKey = inDataKey;
			stm.subscribeToData( bloombergDataKey, this );
		}
		
	}

	@Override
	public void updateData( ISTMEntryKey inKey, final ISTMEntry inData, boolean inFirstUpdate )
	{
		if( reutersDataKey == null || bloombergDataKey == null )
			return;
		
		if( inData.getStatus() == Status.OK )
		{
			stm.commit( new DataTransaction( dataKey, SwapPriceProducer.this )
			{
				@Override
				public void execute()
				{
					
					ISTMEntry reutData = stm.getData( reutersDataKey.getKey() );
					ISTMEntry bloomData = stm.getData( bloombergDataKey.getKey() );
					
					if( reutData == null || bloomData == null )
					{
						dispose();
						return;
					}
					BigDecimal[] reutBidAsk = PriceEngineUtil.getBidAskFromData( reutData );
					BigDecimal[] bloomBidAsk = PriceEngineUtil.getBidAskFromData( bloomData );
					
					if( reutBidAsk == null || bloomBidAsk == null )
					{
						dispose();
						return;
					}
					if( getStatus() == Status.ON_REQUEST)
						setStatus( Status.OK );
					
					BigDecimal bid = (reutBidAsk[0].add( bloomBidAsk[0] )).divide( new BigDecimal( 2 ) ); 
					BigDecimal ask = (reutBidAsk[1].add( bloomBidAsk[1] )).divide( new BigDecimal( 2 ) );
					
					DataTypeLong timeStamp = (DataTypeLong)inData.getValue( DataConstants.FIELD_TIMESTAMP );
					
					putValue( MarketDataConstants.FIELD_TIMESTAMP, timeStamp);
					
					bid = bid.round( new MathContext( 3, RoundingMode.DOWN) );
					ask = ask.round( new MathContext( 3, RoundingMode.UP) );
					
					final DataTypeBigDecimal spread = new DataTypeBigDecimal( ask.subtract( bid ) );
					
					putValue(FXDataConstants.FIELD_BID, new DataTypeBigDecimal( bid ) );
					putValue(FXDataConstants.FIELD_ASK, new DataTypeBigDecimal( ask ) );
					putValue(FXDataConstants.FIELD_SPREAD, spread );
				}
			});
		}
	}

	@Override
	protected void start()
	{
		subscribe();
	}
	
	@Override
	public void stop()
	{
		stm.unsubscribeToData( reutersDataKey, this );
		stm.unsubscribeToData( bloombergDataKey, this );
	}

	@Override
	public void queryNotAvailible(Object inTag)
	{
		setStatus( Status.NA );
		stm.logError( "could not retrieve datakey from market data" );
		return;
	}
	
}
