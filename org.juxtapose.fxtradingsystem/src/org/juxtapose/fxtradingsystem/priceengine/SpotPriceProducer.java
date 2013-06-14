package org.juxtapose.fxtradingsystem.priceengine;

import static org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants.STATE_EUR;
import static org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants.STATE_SEK;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Random;

import org.juxtapose.fxtradingsystem.constants.FXDataConstants;
import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.fxtradingsystem.marketdata.MarketDataConstants;
import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.stm.DataProducerDependencyController;
import org.juxtapose.streamline.stm.DataTransaction;
import org.juxtapose.streamline.stm.DependencyTransaction;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.STMTransaction;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeBigDecimal;
import org.juxtapose.streamline.util.data.DataTypeLong;
import org.juxtapose.streamline.util.data.DataTypeRef;

/**
 * @author Pontus Jörgne
 * 6 okt 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public final class SpotPriceProducer extends STMEntryProducer implements ISTMEntryRequestSubscriber
{
	final String ccy1;
	final String ccy2;

	final long reutersTag = 0;
	final long bloombergTag = 1;
	
	ISTMEntryKey reutersDataKey;
	ISTMEntryKey bloombergDataKey;

	/**
	 * @param inKey
	 * @param inCcy1
	 * @param inCcy2
	 * @param inSTM
	 */
	public SpotPriceProducer( ISTMEntryKey inKey, String inCcy1, String inCcy2, ISTM inSTM )
	{
		super( inKey, inSTM );
		ccy1 = inCcy1;
		ccy2 = inCcy2;
	}

	public void linkStaticData()
	{
		stm.commit( new STMTransaction( entryKey, SpotPriceProducer.this, 1, 0 )
		{
			@Override
			public void execute()
			{
				addReference( PriceEngineDataConstants.FIELD_STATIC_DATA, new DataTypeRef( PriceEngineKeyConstants.CCY_SEK_KEY ) );
			}
		});
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

		query.put( MarketDataConstants.FIELD_SOURCE, "BLOOMBERG" );

		stm.getDataKey( FXProducerServiceConstants.MARKET_DATA, this, bloombergTag, query );
	}

	public void start()
	{
		linkStaticData();
		subscribe();
	}

	/**
	 * @param inRand
	 * @param inTransaction
	 */
	public void addPriceUpdate( final Random inRand, STMTransaction inTransaction )
	{
		DataTypeBigDecimal bid = new DataTypeBigDecimal( inRand.nextDouble() );
		DataTypeBigDecimal ask = new DataTypeBigDecimal( inRand.nextDouble() );


		final DataTypeBigDecimal spread = new DataTypeBigDecimal( ask.get().subtract( bid.get() ) );

		inTransaction.putValue(FXDataConstants.FIELD_BID, bid );
		inTransaction.putValue(FXDataConstants.FIELD_ASK, ask );
		inTransaction.putValue(FXDataConstants.FIELD_SPREAD, spread );
	}


	@Override
	public void updateData( ISTMEntryKey inKey, final ISTMEntry inData, boolean inFirstUpdate )
	{
		if( reutersDataKey == null || bloombergDataKey == null )
			return;
		
		if( inData.getStatus() == Status.OK )
		{
			stm.commit( new DataTransaction( entryKey, SpotPriceProducer.this )
			{
				@Override
				public void execute()
				{
					DataTypeRef ref = (DataTypeRef)get( PriceEngineDataConstants.FIELD_STATIC_DATA );
					ISTMEntry reutData = stm.getData( reutersDataKey.getKey() );
					ISTMEntry bloomData = stm.getData( bloombergDataKey.getKey() );

					if( reutData == null || bloomData == null || ref == null )
					{
						dispose();
						return;
					}
					Long dec = PriceEngineUtil.getDecimals( ref.getReferenceData() );
					BigDecimal[] reutBidAsk = PriceEngineUtil.getBidAskFromData( reutData );
					BigDecimal[] bloomBidAsk = PriceEngineUtil.getBidAskFromData( bloomData );

					if( reutBidAsk == null || bloomBidAsk == null || dec == null )
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

					bid = bid.round( new MathContext( dec.intValue(), RoundingMode.DOWN) );
					ask = ask.round( new MathContext( dec.intValue(), RoundingMode.UP) );

					final DataTypeBigDecimal spread = new DataTypeBigDecimal( ask.subtract( bid ) );

					putValue(FXDataConstants.FIELD_BID, new DataTypeBigDecimal( bid ) );
					putValue(FXDataConstants.FIELD_ASK, new DataTypeBigDecimal( ask ) );
					putValue(FXDataConstants.FIELD_SPREAD, spread );

				}
			});
		}
	}

	@Override
	public void deliverKey(ISTMEntryKey inDataKey, Object inTag)
	{
		if( inTag.equals( reutersTag ) )
		{
			reutersDataKey = inDataKey;
		}
		else if( inTag.equals( bloombergTag ) )
		{
			bloombergDataKey = inDataKey;
		}
		
		if( reutersDataKey != null && bloombergDataKey != null )
		{
			stm.commit( new DependencyTransaction( entryKey, SpotPriceProducer.this, 2, 0 )
			{

				@Override
				public void execute()
				{
					addDependency( reutersDataKey.getKey(), new DataProducerDependencyController( SpotPriceProducer.this, stm, reutersDataKey ) );
					addDependency( bloombergDataKey.getKey(), new DataProducerDependencyController( SpotPriceProducer.this, stm, bloombergDataKey ) );
				}
			});
		}
	}

	@Override
	public void queryNotAvailible(Object inTag)
	{
		setStatus( Status.NA );
		stm.logError( "could not retrieve datakey from market data" );
		return;
		
	}


}

