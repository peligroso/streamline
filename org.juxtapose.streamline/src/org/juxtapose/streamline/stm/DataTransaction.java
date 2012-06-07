package org.juxtapose.streamline.stm;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.util.data.DataTypeRef;

public abstract class DataTransaction extends STMTransaction
{

	public DataTransaction( ISTMEntryKey inDataKey, ISTMEntryProducer inProducer )
	{
		super( inDataKey, inProducer, 0, 0 );
	}

	public void updateReferenceValue( Integer inKey, DataTypeRef inDataTypeRef )
	{
		throw new IllegalAccessError("Cannot update reference value from DataTransaction ");
	}
	
	public void addReference( Integer inKey, DataTypeRef inDataRef )
	{
		throw new IllegalAccessError("Cannot add reference value from DataTransaction ");
	}

}
