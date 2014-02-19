package org.juxtapose.streamline.swt.datatable;

import org.eclipse.swt.widgets.Shell;
import org.juxtapose.streamline.producer.ISTMEntryKey;

import com.trifork.clj_ds.IPersistentMap;

public interface IViewDataObjectContainer
{
	public ViewDataObject addEntry( String inKey );
	public Shell getShell();
	public boolean validateKey( String inKey, ISTMEntryKey inEntryKey );
	public void updateChild( IPersistentMap<String, Object> inData, String inKey );
}
