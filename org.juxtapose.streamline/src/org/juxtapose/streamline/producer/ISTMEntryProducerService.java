package org.juxtapose.streamline.producer;

import java.util.Map;

import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMRequestor;

import com.trifork.clj_ds.IPersistentMap;

public interface ISTMEntryProducerService
{
	public String getServiceId();
	/**
	 * @param inQuery query for key
	 * @return datakey or null if no datakey can be created from query
	 */
	public void getDataKey( ISTMEntryRequestSubscriber inSubscriber, Object inTag, Map<String, String> inQuery );
	public ISTMEntryProducer getDataProducer( ISTMEntryKey inDataKey );
	
	public void request( int inTag, long inType, ISTMRequestor inRequestor, String inVariable, IPersistentMap<String, Object> inData );

}
