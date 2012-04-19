package org.juxtapose.fxtradingsystem.priceengine;

import java.util.HashMap;

import org.juxtapose.fxtradingsystem.FXDataConstants;
import org.juxtapose.fxtradingsystem.FXProducerServiceConstants;
import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.producer.IDataProducer;
import org.juxtapose.streamline.producer.ProducerUtil;
import org.juxtapose.streamline.stm.osgi.DataProducerService;
import org.juxtapose.streamline.util.IDataRequestSubscriber;
import org.juxtapose.streamline.util.IPublishedData;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.producerservices.DataInitializer;

/**
 * @author Pontus J�rgne
 * 17 okt 2011
 * Copyright (c) Pontus J�rgne. All rights reserved
 */
public class PriceEngine extends DataProducerService implements IPriceEngine
{

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.osgi.DataProducerService#createDataInitializer()
	 */
	public DataInitializer createDataInitializer( )
	{
		DataInitializer initializer = new DataInitializer( stm, this, PriceEngineKeyConstants.CCY_MODEL_KEY );
		return initializer;
	}

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.IDataProducerService#getDataKey(java.util.HashMap)
	 */
	@Override
	public void getDataKey( IDataRequestSubscriber inSubscriber, Long inTag, HashMap<Integer, String> inQuery )
	{
		String type = inQuery.get( PriceEngineDataConstants.FIELD_TYPE );
		if( type == null )
		{
			stm.logError( "No type defined for dataKey "+inQuery );
			inSubscriber.queryNotAvailible( inTag );
		}
		if( type.equals( PriceEngineDataConstants.STATE_TYPE_CCYMODEL ))
			inSubscriber.deliverKey( PriceEngineKeyConstants.CCY_MODEL_KEY, inTag );
		
		else if( type.equals( PriceEngineDataConstants.STATE_TYPE_PRICE ))
		{
			String instrumentType = inQuery.get( FXDataConstants.FIELD_INSTRUMENT );
			String ccy1 = inQuery.get( FXDataConstants.FIELD_CCY1 );
			String ccy2 = inQuery.get( FXDataConstants.FIELD_CCY2 );
		
			inSubscriber.deliverKey(  ProducerUtil.createDataKey( getServiceId(), PriceEngineDataConstants.STATE_TYPE_PRICE, new Integer[]{FXDataConstants.FIELD_CCY1, FXDataConstants.FIELD_CCY2, FXDataConstants.FIELD_INSTRUMENT},new String[]{ccy1, ccy2, instrumentType} ), inTag);
		}
		else
		{
			inSubscriber.queryNotAvailible( inTag );
		}
	}

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.IDataProducerService#getDataProducer(org.juxtapose.streamline.producer.IDataKey)
	 * May return null on invalid key
	 */
	@Override
	public IDataProducer getDataProducer(IDataKey inDataKey)
	{
		String type = inDataKey.getType();
		
		if( type == PriceEngineDataConstants.STATE_TYPE_CCY )
		{
			String ccy = inDataKey.getSingleValue();
			assert( ccy != null && ccy.length() == 3 ) : "Error in ccy key, ccy value = "+ccy;
			if( ccy == null )
			{
				stm.logError( "No ccy defined in ccy key" );
				return null;
			}
			return new CcyProducer( stm, inDataKey, ccy );
		}
		if( type == PriceEngineDataConstants.STATE_TYPE_CCYMODEL )
		{
			return new CcyModelProducer( stm );
		}
		
		String ccy1 = inDataKey.getValue( FXDataConstants.FIELD_CCY1 );
		String ccy2 = inDataKey.getValue( FXDataConstants.FIELD_CCY2 );
		String instrumentType = inDataKey.getValue( FXDataConstants.FIELD_INSTRUMENT );
		
		if( FXDataConstants.STATE_INSTRUMENT_SPOT.equals( instrumentType ) )
		{
			return new SpotPriceProducer(inDataKey, ccy1, ccy2, stm);
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.util.IDataSubscriber#updateData(java.lang.String, org.juxtapose.streamline.util.IPublishedData, boolean)
	 */
	@Override
	public void updateData( IDataKey iKey, IPublishedData inData, boolean inFirstUpdate)
	{
		DataType<?> dataValue = inFirstUpdate ? inData.getValue( FXProducerServiceConstants.ORDER_MANAGER ) : inData.getValue( FXProducerServiceConstants.ORDER_MANAGER );
		if( dataValue != null )
		{
			stm.logInfo( "OrderService is registered with status: "+dataValue);
		}
		else
		{
			stm.logInfo( "OrderService is not registered");
		}
	}

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.osgi.DataProducerService#getServiceId()
	 */
	@Override
	public Integer getServiceId()
	{
		return FXProducerServiceConstants.PRICE_ENGINE;
	}

}
