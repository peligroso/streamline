package org.juxtapose.streamline.producer;

import java.util.Map;

import org.juxtapose.streamline.util.IDataRequestSubscriber;

public interface IDataProducerService
{
	public String getServiceId();
	/**
	 * @param inQuery query for key
	 * @return datakey or null if no datakey can be created from query
	 */
	public void getDataKey( IDataRequestSubscriber inSubscriber, Long inTag, Map<String, String> inQuery );
	public IDataProducer getDataProducer( IDataKey inDataKey );

}
