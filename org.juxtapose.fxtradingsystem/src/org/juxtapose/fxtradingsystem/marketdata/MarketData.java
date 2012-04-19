package org.juxtapose.fxtradingsystem.marketdata;

import java.util.HashMap;

import org.juxtapose.fxtradingsystem.FXDataConstants;
import org.juxtapose.fxtradingsystem.FXProducerServiceConstants;
import org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants;
import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.producer.IDataProducer;
import org.juxtapose.streamline.producer.ProducerUtil;
import org.juxtapose.streamline.stm.osgi.DataProducerService;
import org.juxtapose.streamline.util.IDataRequestSubscriber;
import org.juxtapose.streamline.util.IDataSubscriber;
import org.juxtapose.streamline.util.IPublishedData;

/**
 * @author Pontus Jörgne
 * Feb 22, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class MarketData extends DataProducerService implements IMarketDataService
{

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.IDataProducerService#getDataKey(org.juxtapose.streamline.util.IDataSubscriber, java.lang.Long, java.util.HashMap)
	 */
	@Override
	public void getDataKey( IDataRequestSubscriber inSubscriber, Long inTag, HashMap<Integer, String> inQuery )
	{
		String type = inQuery.get( PriceEngineDataConstants.FIELD_TYPE );

		if( MarketDataConstants.STATE_TYPE_INSTRUMENT.equals( type ) )
		{
			String source = inQuery.get( MarketDataConstants.FIELD_SOURCE );
			String ccy1 = inQuery.get( MarketDataConstants.FIELD_CCY1 );
			String ccy2 = inQuery.get( MarketDataConstants.FIELD_CCY2 );
			String period = inQuery.get( MarketDataConstants.FIELD_PERIOD );

			if( source == null || ccy1 == null || ccy2 == null || period == null )
			{
				stm.logError( "Missing attribute for dataKey "+inQuery );
				inSubscriber.queryNotAvailible( inTag );
			}

			IDataKey key = ProducerUtil.createDataKey( getServiceId(), MarketDataConstants.STATE_TYPE_INSTRUMENT, new Integer[]{MarketDataConstants.FIELD_SOURCE, FXDataConstants.FIELD_CCY1, FXDataConstants.FIELD_CCY2, FXDataConstants.FIELD_PERIOD},new String[]{source, ccy1, ccy2, period} );
			inSubscriber.deliverKey( key, inTag );
		}
		else
		{
			inSubscriber.queryNotAvailible( inTag );
		}
	}

	@Override
	public IDataProducer getDataProducer(IDataKey inDataKey)
	{
		return new MarketDataProducer( inDataKey, stm );
	}

	@Override
	public void updateData( IDataKey inKey, IPublishedData inData, boolean inFirstUpdate )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Integer getServiceId()
	{
		return FXProducerServiceConstants.MARKET_DATA;
	}

}
