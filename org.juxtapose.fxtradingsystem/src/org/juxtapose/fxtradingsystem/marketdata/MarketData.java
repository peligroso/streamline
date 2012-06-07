package org.juxtapose.fxtradingsystem.marketdata;

import java.util.Map;

import org.juxtapose.fxtradingsystem.FXDataConstants;
import org.juxtapose.fxtradingsystem.FXProducerServiceConstants;
import org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.ProducerUtil;
import org.juxtapose.streamline.stm.osgi.DataProducerService;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMEntry;

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
	public void getDataKey( ISTMEntryRequestSubscriber inSubscriber, Long inTag, Map<String, String> inQuery )
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

			ISTMEntryKey key = ProducerUtil.createDataKey( getServiceId(), MarketDataConstants.STATE_TYPE_INSTRUMENT, new String[]{MarketDataConstants.FIELD_SOURCE, FXDataConstants.FIELD_CCY1, FXDataConstants.FIELD_CCY2, FXDataConstants.FIELD_PERIOD},new String[]{source, ccy1, ccy2, period} );
			inSubscriber.deliverKey( key, inTag );
		}
		else
		{
			inSubscriber.queryNotAvailible( inTag );
		}
	}

	@Override
	public ISTMEntryProducer getDataProducer(ISTMEntryKey inDataKey)
	{
		return new MarketDataProducer( inDataKey, stm );
	}

	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFirstUpdate )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getServiceId()
	{
		return FXProducerServiceConstants.MARKET_DATA;
	}

}
