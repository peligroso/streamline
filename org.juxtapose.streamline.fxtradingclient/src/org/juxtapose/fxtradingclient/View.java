package org.juxtapose.fxtradingclient;

import java.util.HashMap;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.DataConstants;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.KeyConstants;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;

public class View extends ViewPart implements ISTMEntryRequestSubscriber
{
	public static final String ID = "org.juxtapose.fxtradingclient.view";

	private TableViewer viewer;

	ISTM stm;
	
	private ISTMEntryKey configMetaKey; 
	boolean subscribedMetaData = false;
	
	/**
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */
	class ViewContentProvider implements IStructuredContentProvider 
	{
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (parent instanceof Object[]) {
				return (Object[]) parent;
			}
	        return new Object[0];
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		// Provide the input to the ContentProvider
		viewer.setInput(new String[] {"One", "Two", "Three"});
		
		stm = STMUtil.getSTM();
		
		stm.subscribeToData( KeyConstants.PRODUCER_SERVICE_KEY, this);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFirstUpdate ) 
	{
		if( inKey.equals( KeyConstants.PRODUCER_SERVICE_KEY ) && !subscribedMetaData )
		{
			DataType<?> dataValue = inData.getUpdatedValue( FXProducerServiceConstants.CONFIG );
			if( dataValue != null )
			{
				if( dataValue.get() == Status.OK.toString() )
				{
					stm.logInfo( "requesting key for config metadata" );
					HashMap<String, String> queryMap = new HashMap<String, String>();
					queryMap.put( DataConstants.FIELD_QUERY_KEY, DataConstants.STATE_TYPE_META );
					stm.getDataKey( FXProducerServiceConstants.CONFIG, this, FXProducerServiceConstants.CONFIG, queryMap );
//					subscribedMetaData = true;
				}
			}
		}
		else if( inKey.equals( configMetaKey ))
		{
			stm.logInfo( "Config data recieved for meta data "+inData.getDataMap() );
		}
	}

	@Override
	public int getPriority() 
	{
		return 0;
	}

	@Override
	public void deliverKey( ISTMEntryKey inDataKey, Object inTag ) 
	{
		stm.logInfo( "key for config metadata delivered" );
		configMetaKey = inDataKey;
		stm.subscribeToData( inDataKey, this );
		
	}

	@Override
	public void queryNotAvailible( Object inTag ) 
	{
		stm.logInfo( "config metadata not availible" );
	}
}