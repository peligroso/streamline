package org.juxtapose.fxtradingclient;

import static org.juxtapose.streamline.tools.STMUtil.isServiceStatusUpdatedToOk;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.juxtapose.fxtradingsystem.constants.FXDataConstants;
import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.tools.KeyConstants;
import org.juxtapose.streamline.tools.STMEntryKey;
import org.juxtapose.streamline.tools.STMUtil;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMRequestor;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeArrayList;
import org.juxtapose.streamline.util.data.DataTypeHashMap;
import org.juxtapose.streamline.util.data.DataTypeLong;
import org.juxtapose.streamline.util.data.DataTypeString;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

public class EditView extends ViewPart implements ISTMEntryRequestSubscriber, ISTMRequestor
{
	public static final String ID = "org.juxtapose.fxtradingclient.view";

	private HashMap<String, DataViewer> typeToViewer = new HashMap<String, DataViewer>();
	private Composite parent;

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

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider 
	{
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
	public void createPartControl(Composite parent) 
	{
//		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		
//		viewer.setContentProvider(new ViewContentProvider());
//		viewer.setLabelProvider(new ViewLabelProvider());
		// Provide the input to the ContentProvider
		
		this.parent = parent;
		
		stm = STMStatic.getSTM();
		
		Button syncButt = new Button( parent, SWT.PUSH );
		syncButt.setText( "Synch" );
		syncButt.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent sev )
			{
				uploadRecord();
			}
		});
		
		stm.subscribeToData( KeyConstants.PRODUCER_SERVICE_KEY, this);
		
	}
	
	private void uploadRecord()
	{
		DataViewer ccyViewer = typeToViewer.get( "CCY" );
		ViewDataObject[] objects = ccyViewer.getObjects();
		
		if( objects == null )
			return;
		
		for( ViewDataObject obj : objects )
		{
			IPersistentMap<String, DataType<?>> data = obj.getData();
			stm.request( FXProducerServiceConstants.CONFIG, 1, this, "CCY", data );
		}
	}
	

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
//		viewer.getControl().setFocus();
	}

	@Override
	public void updateData( final ISTMEntryKey inKey, final ISTMEntry inData, final boolean inFullUpdate ) 
	{
		if( inKey.equals( KeyConstants.PRODUCER_SERVICE_KEY ) && !subscribedMetaData )
		{
			if( isServiceStatusUpdatedToOk( FXProducerServiceConstants.CONFIG, inData ))
			{
				stm.logInfo( "requesting key for config metadata" );
				HashMap<String, String> queryMap = new HashMap<String, String>();
				queryMap.put( DataConstants.FIELD_QUERY_KEY, DataConstants.STATE_TYPE_META );
				stm.getDataKey( FXProducerServiceConstants.CONFIG, this, FXProducerServiceConstants.CONFIG, queryMap );
				
			}
			else if( isServiceStatusUpdatedToOk( FXProducerServiceConstants.PRICE_ENGINE, inData ))
			{
//				PriceSubscriber ps = new PriceSubscriber( stm );
			}
			DataType<?> peVal = inData.getUpdatedValue( FXProducerServiceConstants.PRICE_ENGINE );
		}
		
	}
	
	public void metaDataRecieved( final ISTMEntry inData, final boolean inFullUpdate )
	{
		
	}

	@Override
	public int getPriority() 
	{
		return 0;
	}

	@Override
	public void deliverKey( ISTMEntryKey inDataKey, Object inTag ) 
	{
		configMetaKey = inDataKey;
		
		MetaDataControl metaDataControl = new MetaDataControl( this );
		metaDataControl.initialize( stm, configMetaKey );
		
	}
	
	public void addViewer( final String inFieldKey, final IPersistentMap<String, DataType<?>> inData )
	{
		parent.getDisplay().asyncExec( new Runnable()
		{
			@Override
			public void run() 
			{
				Button b = new Button(parent, SWT.PUSH);
				
				final DataViewer viewer = new DataViewer( parent, SWT.NONE, inData, inFieldKey );
				typeToViewer.put( inFieldKey, viewer );
				
				b.addSelectionListener( new SelectionAdapter(){
					public void widgetSelected( SelectionEvent sev )
					{
						viewer.addEntry();
					}
				});
				
				parent.layout();
				parent.update();
			}
		});
	}

	@Override
	public void queryNotAvailible( Object inTag ) 
	{
		stm.logInfo( "config metadata not availible" );
	}

	@Override
	public void reply( int inTag, IPersistentMap<String, DataType<?>> inData )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void requestError( int inTag, String inError )
	{
		// TODO Auto-generated method stub
		
	}
}