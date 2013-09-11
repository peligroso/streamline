package org.juxtapose.fxtradingclient;

import static org.juxtapose.streamline.tools.STMUtil.isServiceStatusUpdatedToOk;

import java.util.HashMap;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.tools.KeyConstants;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMRequestor;
import org.juxtapose.streamline.util.data.DataTypeRef;

import com.trifork.clj_ds.IPersistentMap;

public class EditView extends ViewPart implements ISTMEntryRequestSubscriber, ISTMRequestor
{
	public static final String ID = "org.juxtapose.fxtradingclient.editview";

	private HashMap<String, DataViewer> typeToViewer = new HashMap<String, DataViewer>();
	private Composite parent;

	ISTM stm;
	
	private ISTMEntryKey configMetaKey; 
	boolean subscribedMetaData = false;
	
	TabFolder tabFolder;
	
	
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
	
	private void createMenu( Composite inComp )
	{
		Composite menuComp = new Composite( inComp, SWT.NONE );
		menuComp.setLayout( new RowLayout() );
		
		Button syncButt = new Button( parent, SWT.PUSH );
		syncButt.setText( "Synch" );
		syncButt.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent sev )
			{
				uploadRecord();
			}
		});
		
		Button newButt = new Button(parent, SWT.PUSH);
		newButt.addSelectionListener( new SelectionAdapter(){
			public void widgetSelected( SelectionEvent sev )
			{
				TabItem[] items = tabFolder.getSelection();
				
				if( items != null && items.length != 0 )
				{
					DataViewer viewer = (DataViewer)items[0].getControl();
					viewer.addEntry();
				}
//				
			}
		});
		
		newButt.setText( "New Entry" );
		
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
		
		parent.setLayout( new GridLayout(1, false) );
		
		createMenu( parent );
		
		tabFolder = new TabFolder(parent, SWT.NONE);
		
		GridData tabData = new GridData();
		tabData.grabExcessHorizontalSpace = true;
		tabData.grabExcessVerticalSpace = true;
		tabData.horizontalAlignment = GridData.FILL;
		tabData.verticalAlignment = GridData.FILL;

		tabFolder.setLayoutData(tabData);
		
		stm.subscribeToData( KeyConstants.PRODUCER_SERVICE_KEY, this);
		
	}
	
	private void uploadRecord()
	{
		TabItem[] selectedItems = tabFolder.getSelection();
		
		if( selectedItems == null || selectedItems.length == 0 )
			return;
		
		TabItem selected = selectedItems[0];
		
		DataViewer viewer = (DataViewer)selected.getControl();
		ViewDataObject[] objects = viewer.getObjects();
		
		if( objects == null )
			return;
		
		for( ViewDataObject obj : objects )
		{
			if( obj.getState() == ViewDataObjectState.CREATED )
			{
				IPersistentMap<String, Object> data = obj.getData();
				stm.request( FXProducerServiceConstants.CONFIG, 1, DataConstants.REQUEST_TYPE_CREATE, this, viewer.getType(), data );
			}
			else if( obj.getState() == ViewDataObjectState.UPDATED )
			{
				IPersistentMap<String, Object> data = obj.getUpdateData();
				data = data.assoc( DataConstants.FIELD_KEYS, new DataTypeRef( obj.getKey() ) );
				stm.request( FXProducerServiceConstants.CONFIG, 1, DataConstants.REQUEST_TYPE_UPDATE, this, viewer.getType(), data );
			}
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
			Object peVal = inData.getUpdatedValue( FXProducerServiceConstants.PRICE_ENGINE );
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
	
	public void addViewer( final String inFieldKey, final IPersistentMap<String, Object> inData, final MetaDataControl inMetaDataControl )
	{
		parent.getDisplay().asyncExec( new Runnable()
		{
			@Override
			public void run() 
			{
				TabItem viewTabItem = new TabItem(tabFolder, SWT.NONE);
				
//				Button b = new Button(parent, SWT.PUSH);
				
				final DataViewer viewer = new DataViewer( FXProducerServiceConstants.CONFIG, tabFolder, SWT.NONE, inData, inFieldKey, inMetaDataControl );
				typeToViewer.put( inFieldKey, viewer );
				
//				b.addSelectionListener( new SelectionAdapter(){
//					public void widgetSelected( SelectionEvent sev )
//					{
//						viewer.addEntry();
//					}
//				});
				
				viewTabItem.setControl(viewer);
				viewTabItem.setText(inFieldKey);
				
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
	public void reply( int inTag, long inType, String inMessage, IPersistentMap<String, Object> inData )
	{
		// TODO Auto-generated method stub
		
	}

}