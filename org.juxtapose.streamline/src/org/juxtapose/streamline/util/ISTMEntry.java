package org.juxtapose.streamline.util;

import java.util.Set;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.util.data.DataType;

import com.trifork.clj_ds.IPersistentMap;


public interface ISTMEntry {

	public ISTMEntryProducer getProducer();
	
	public DataType<?> getValue( String inKey );
	public boolean isDeltaValue( String inKey );
	public ISTMEntry addSubscriber(ISTMEntrySubscriber inSubscriber);
	public ISTMEntry removeSubscriber(ISTMEntrySubscriber inSubscriber);
	public boolean hasSubscribers();
	public IPersistentMap<String, DataType<?>> getDataMap();
	public ISTMEntry setUpdatedData(IPersistentMap<String, DataType<?>> stateInstruction, Set<String> deltaState, Status inStatus, boolean completeUpdate);
	public void updateSubscribers(ISTMEntryKey dataKey);
	public ISTMEntry putDataValue( String inKey, DataType<?> inValue )throws Exception;
	public Status getStatus();
	public long getSequenceID();
	public boolean isCompleteVersion();
	public Set<String> getDeltaSet();
	public int getHighPriosubscriberCount();
	public ISTMEntry changeSubscriberPriority( ISTMEntrySubscriber inSubscriber, int inNewPriority );
	public int getPriority();
}
