package org.juxtapose.fxtradingsystem.aggregator;

import java.util.Map;

import org.juxtapose.fxtradingsystem.constants.FXDataConstants;
import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import static org.juxtapose.streamline.tools.STMUtil.*;
import org.juxtapose.streamline.stm.osgi.DataProducerService;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;

public class LiquidityService extends DataProducerService implements IAggregator{

	@Override
	public void getDataKey(ISTMEntryRequestSubscriber inSubscriber, Object inTag, Map<String, String> inQuery) 
	{
		String type = inQuery.get( PriceEngineDataConstants.FIELD_TYPE );
		if( type == null )
		{
			stm.logError( "No type defined for dataKey "+inQuery );
			inSubscriber.queryNotAvailible( inTag );
		}
		
		else if( type.equals( PriceEngineDataConstants.STATE_TYPE_LIQUIDITY ))
		{
			String instrumentType = inQuery.get( FXDataConstants.FIELD_INSTRUMENT );
			String ccy1 = inQuery.get( FXDataConstants.FIELD_CCY1 );
			String ccy2 = inQuery.get( FXDataConstants.FIELD_CCY2 );
			String source = inQuery.get( FXDataConstants.FIELD_SOURCE );
		
			inSubscriber.deliverKey(  createEntryKey( getServiceId(), PriceEngineDataConstants.STATE_TYPE_LIQUIDITY, 
					new String[]{FXDataConstants.FIELD_CCY1, FXDataConstants.FIELD_CCY2, FXDataConstants.FIELD_INSTRUMENT, FXDataConstants.FIELD_SOURCE},
					new String[]{ccy1, ccy2, instrumentType, source} ), inTag);
		}
		else
		{
			inSubscriber.queryNotAvailible( inTag );
		}
	}

	@Override
	public ISTMEntryProducer getDataProducer(ISTMEntryKey inDataKey) 
	{
		String ccy1 = inDataKey.getValue( FXDataConstants.FIELD_CCY1 );
		String ccy2 = inDataKey.getValue( FXDataConstants.FIELD_CCY2 );
		String instrumentType = inDataKey.getValue( FXDataConstants.FIELD_INSTRUMENT );
		String source = inDataKey.getValue( FXDataConstants.FIELD_SOURCE );
		
		if( FXDataConstants.STATE_INSTRUMENT_SPOT.equals( instrumentType ) )
		{
			return new LiquidityPoolProducer(inDataKey, stm, source, ccy1, ccy2, FXDataConstants.STATE_PERIOD_SP );
		}
		
		return null;
	}

	@Override
	public void updateData(ISTMEntryKey inKey, ISTMEntry inData, boolean inFullUpdate) 
	{
		
	}

	@Override
	public String getServiceId() 
	{
		return FXProducerServiceConstants.AGGREGATOR;
	}

}
