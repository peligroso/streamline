package org.juxtapose.fxtradingsystem.config;

import java.util.Map;

import org.juxtapose.fxtradingsystem.FXProducerServiceConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.ISTMEntryProducerService;
import org.juxtapose.streamline.producer.ProducerUtil;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.DataConstants;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;

public class ConfigService implements ISTMEntryProducerService
{
	MetaDataProducer metaProducer;
	
	/**
	 * @param inSTM
	 */
	ConfigService( ISTM inSTM )
	{
		metaProducer = new MetaDataProducer( ProducerUtil.createDataKey( FXProducerServiceConstants.CONFIG, DataConstants.STATE_TYPE_META, FXProducerServiceConstants.CONFIG ), inSTM );
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

}
