package org.juxtapose.fxtradingclient;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.swt.dataeditor.GenericEditor;

public class EditView extends ViewPart
{
	public static final String ID = "org.juxtapose.fxtradingclient.editview";

	private Composite parent;

	private ISTM stm;
	
	private GenericEditor editor;
	

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) 
	{
		
		this.parent = parent;
		
		stm = STMStatic.getSTM();
		
		parent.setLayout( new FillLayout() );
		
		editor = new GenericEditor( parent, SWT.NONE, stm, FXProducerServiceConstants.CONFIG );
		
	}
	
	

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
//		viewer.getControl().setFocus();
	}

}