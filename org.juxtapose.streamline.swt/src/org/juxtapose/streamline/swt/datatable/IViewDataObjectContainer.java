package org.juxtapose.streamline.swt.datatable;

import org.eclipse.swt.widgets.Shell;
import org.juxtapose.streamline.producer.ISTMEntryKey;

public interface IViewDataObjectContainer
{
	public ViewDataObject addEntry( String inKey );
	public Shell getShell();
	public boolean validateKey( String inKey, ISTMEntryKey inEntryKey );
}
