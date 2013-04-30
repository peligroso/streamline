package org.juxtapose.fxtradingsystem.config;

import java.util.Map;

import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.ProducerUtil;
import org.juxtapose.streamline.stm.osgi.DataProducerService;
import org.juxtapose.streamline.util.DataConstants;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;

public class ConfigService extends DataProducerService implements IConfigService 
{
	MetaDataProducer metaProducer;
	
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.osgi.DataProducerService#init()
	 */
	public void init( )
	{
		metaProducer = new MetaDataProducer( ProducerUtil.createDataKey( FXProducerServiceConstants.CONFIG, DataConstants.STATE_TYPE_META, FXProducerServiceConstants.CONFIG ), stm );
		super.init();
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
			inSubscriber.deliverKey( metaProducer.getKey(), inTag );
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
			return metaProducer;
		}
		
		return null;
	}

	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData,boolean inFirstUpdate ) {
		
	}

}
