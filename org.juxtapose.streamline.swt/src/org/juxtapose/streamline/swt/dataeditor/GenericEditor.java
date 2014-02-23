package org.juxtapose.streamline.swt.dataeditor;

import static org.juxtapose.streamline.tools.STMUtil.isServiceStatusUpdatedToOk;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.swt.datatable.DataViewer;
import org.juxtapose.streamline.swt.datatable.IDataViewerParent;
import org.juxtapose.streamline.swt.datatable.MetaDataControl;
import org.juxtapose.streamline.swt.datatable.ViewDataObject;
import org.juxtapose.streamline.swt.datatable.ViewDataObjectState;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.tools.KeyConstants;
import org.juxtapose.streamline.tools.STMQueryMethods;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMRequestor;
import org.juxtapose.streamline.util.data.DataTypeRef;

import com.trifork.clj_ds.IPersistentMap;

public class GenericEditor extends Composite implements ISTMEntryRequestSubscriber, ISTMRequestor, IDataViewerParent
{
	public static final String SYNCH = "Synch";
	public static final String DELETE = "Delete";
	
	private final HashMap<String, DataViewer> typeToViewer = new HashMap<String, DataViewer>();
	private final Composite parent;

	private final ISTM stm;
	
	private final TabFolder tabFolder;
	
	private final String serviceKey;
	
	private ISTMEntryKey configMetaKey; 
	boolean subscribedMetaData = false;
	
	public GenericEditor( Composite parent, int style, ISTM inSTM, String inServiceKey )
	{
		super( parent, style );
		
		this.parent = parent;
		
		stm = inSTM;
		
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
		
		serviceKey = inServiceKey;
	}
	
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
		syncButt.setText( SYNCH );
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
					viewer.addEntry( viewer.getType() );
				}
//				
			}
		});
		
		newButt.setText( "New Entry" );
		
		Button delButt = new Button(parent, SWT.PUSH);
		delButt.addSelectionListener( new SelectionAdapter(){
			public void widgetSelected( SelectionEvent sev )
			{
				TabItem[] items = tabFolder.getSelection();
				
				if( items != null && items.length != 0 )
				{
					DataViewer viewer = (DataViewer)items[0].getControl();
					viewer.deleteEntry();
				}
//				
			}
		});
		
		delButt.setText( "Delete Entry" );
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
				stm.request( serviceKey, 1, DataConstants.REQUEST_TYPE_CREATE, this, viewer.getType(), data );
			}
			else if( obj.getState() == ViewDataObjectState.UPDATED )
			{
				IPersistentMap<String, Object> data = obj.getUpdateData();
				data = data.assoc( DataConstants.FIELD_KEYS, new DataTypeRef( obj.getKey() ) );
				stm.request( serviceKey, 1, DataConstants.REQUEST_TYPE_UPDATE, this, viewer.getType(), data );
			}
			else if( obj.getState() == ViewDataObjectState.DELETED )
			{
				IPersistentMap<String, Object> data = obj.getUpdateData();
				data = data.assoc( DataConstants.FIELD_KEYS, new DataTypeRef( obj.getKey() ) );
				stm.request( serviceKey, 1, DataConstants.REQUEST_TYPE_DELETE, this, viewer.getType(), data );
			}
		}
	}
	


	@Override
	public void updateData( final ISTMEntryKey inKey, final ISTMEntry inData, final boolean inFullUpdate ) 
	{
		if( inKey.equals( KeyConstants.PRODUCER_SERVICE_KEY ) && !subscribedMetaData )
		{
			if( isServiceStatusUpdatedToOk( serviceKey, inData ))
			{
				stm.logInfo( "requesting key for config metadata" );
				HashMap<String, String> queryMap = new HashMap<String, String>();
				queryMap.put( DataConstants.FIELD_QUERY_KEY, DataConstants.STATE_TYPE_META );
				stm.getDataKey( serviceKey, this, serviceKey, queryMap );
				
			}
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
		
		MetaDataControl metaDataControl = new MetaDataControl( this, serviceKey );
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
				
				final DataViewer viewer = new DataViewer( GenericEditor.this, serviceKey, tabFolder, SWT.NONE, inData, inFieldKey, inMetaDataControl );
				typeToViewer.put( inFieldKey, viewer );
				
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
	
	public boolean qualifyForDelete( ISTMEntryKey inKey )
	{
		String type = inKey.getType();
		
		for( DataViewer viewer : typeToViewer.values() )
		{
			Set<String> fields = viewer.getTypeDependentFields( type );
			
			if( fields != null )
			{
				ISTMEntryKey containerKey = viewer.getContainerKey();
				if( STMQueryMethods.containsReferences( stm, containerKey, inKey.getKey(), fields.toArray( new String[]{} ) ) )
					return false;
			}
		}
		return true;
	}

}
