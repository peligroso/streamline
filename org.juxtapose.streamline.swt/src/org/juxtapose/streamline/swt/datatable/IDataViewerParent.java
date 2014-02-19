package org.juxtapose.streamline.swt.datatable;

import org.juxtapose.streamline.producer.ISTMEntryKey;

public interface IDataViewerParent
{
	public boolean qualifyForDelete( ISTMEntryKey inKey );
}
