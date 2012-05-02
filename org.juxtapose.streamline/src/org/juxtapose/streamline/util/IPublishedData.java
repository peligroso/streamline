package org.juxtapose.streamline.util;

import java.util.Set;

import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.producer.IDataProducer;
import org.juxtapose.streamline.util.data.DataType;

import com.trifork.clj_ds.IPersistentMap;


public interface IPublishedData {

	public IDataProducer getProducer();
	
	public DataType<?> getValue( String inKey );
	public boolean isDeltaValue( String inKey );
	public IPublishedData addSubscriber(IDataSubscriber inSubscriber);
	public IPublishedData removeSubscriber(IDataSubscriber inSubscriber);
	public boolean hasSubscribers();
	public IPersistentMap<String, DataType<?>> getDataMap();
	public IPublishedData setUpdatedData(IPersistentMap<String, DataType<?>> stateInstruction, Set<String> deltaState, Status inStatus, boolean completeUpdate);
	public void updateSubscribers(IDataKey dataKey);
	public IPublishedData putDataValue( String inKey, DataType<?> inValue )throws Exception;
	public Status getStatus();
	public long getSequenceID();
	public boolean isCompleteVersion();
	public Set<String> getDeltaSet();
}
