package org.juxtapose.fxtradingsystem.config;

import java.util.Map;

import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.fxtradingsystem.priceengine.PriceEngineKeyConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import static org.juxtapose.streamline.tools.STMUtil.*;
import org.juxtapose.streamline.stm.osgi.DataProducerService;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.tools.STMUtil;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.producerservices.DataInitializer;
import org.juxtapose.streamline.util.producerservices.DataRefContainerProducer;

public class ConfigService extends DataProducerService implements IConfigService 
{
	ISTMEntryKey key;
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.osgi.DataProducerService#init()
	 */
	public void init( )
	{
		key = createDataKey( FXProducerServiceConstants.CONFIG, DataConstants.STATE_TYPE_META, FXProducerServiceConstants.CONFIG );
		super.init();
	}
	
	public DataInitializer createDataInitializer( )
	{
		DataInitializer initializer = new DataInitializer( stm, this, STMUtil.createDataKey( FXProducerServiceConstants.CONFIG, "CCY", "CCY" ) );
		return initializer;
	}
	
	@Override
	public String getServiceId()
	{
		return FXProducerServiceConstants.CONFIG;
	}

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.ISTMEntryProducerService#getDataKey(org.juxtapose.streamline.util.ISTMEntryRequestSubscriber, java.lang.Object, java.util.Map)
	 */
	@Override
	public void getDataKey( ISTMEntryRequestSubscriber inSubscriber, Object inTag, Map<String, String> inQuery )
	{
		String val = inQuery.get( DataConstants.FIELD_QUERY_KEY );
		if( val.equals( DataConstants.STATE_TYPE_META ) )
			inSubscriber.deliverKey( key, inTag );
		else
			inSubscriber.queryNotAvailible( inTag );
	}

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.ISTMEntryProducerService#getDataProducer(org.juxtapose.streamline.producer.ISTMEntryKey)
	 */
	@Override
	public ISTMEntryProducer getDataProducer( ISTMEntryKey inDataKey )
	{
		if( DataConstants.STATE_TYPE_META.equals( inDataKey.getType() ) )
		{
			return new MetaDataProducer( key, stm );
		}
		else if( "CCY".equals( inDataKey.getType() ) )
		{
			return new DataRefContainerProducer( inDataKey, stm );
		}
		
		return null;
	}

	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData,boolean inFirstUpdate ) {
		
	}

}
