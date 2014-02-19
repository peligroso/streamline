package org.juxtapose.streamline.swt.datatable;

import static org.juxtapose.streamline.swt.spl.ClientViewMethods.getDataLabel;
import static org.juxtapose.streamline.swt.spl.ClientViewMethods.getEditingSupport;
import static org.juxtapose.streamline.tools.DataConstants.FIELD_KEYS;
import static org.juxtapose.streamline.tools.DataConstants.FIELD_STATUS;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.swt.dataeditor.GenericEditor;
import org.juxtapose.streamline.swt.spl.ImageConstants;
import org.juxtapose.streamline.util.ContainerSubscriber;
import org.juxtapose.streamline.util.ISTMContainerListener;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.BucketMap;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

/**
 * @author Pontus Jörgne
 * 1 jun 2013
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class DataViewer extends Composite implements ISTMContainerListener, IViewDataObjectContainer
{
	public static String STATUS_FIELD_NAME = "";
	
	String typeKey;
	String serviceKey;

	TableViewer viewer;
	MetaDataControl metaDataControl;

	IPersistentMap<String, Object> metaData;

	Set<ViewDataObject> viewObjects = new HashSet<ViewDataObject>();
	
	BucketMap<String, String> referenceDependencies = new BucketMap<String, String>();
	
	GenericEditor editor;
	
	/**
	 * @param inServiceKey
	 * @param parent
	 * @param style
	 * @param inData
	 * @param inTypeKey
	 * @param inMetaDataControl
	 */
	public DataViewer( GenericEditor inGenEd, String inServiceKey, Composite parent, int style, IPersistentMap<String, Object> inData, String inTypeKey, MetaDataControl inMetaDataControl ) 
	{
		super( parent, style );
		
		editor = inGenEd;
		typeKey = inTypeKey;
		serviceKey = inServiceKey;

		metaDataControl = inMetaDataControl;

		metaData = inData;

		setLayout( new GridLayout(1, false) );
		createViewer( this, inData );

		ContainerSubscriber containerSub = metaDataControl.getContainerSubscriber( typeKey );
		if( containerSub != null )
			containerSub.addContainerListener( this );

	}

	/**
	 * @return
	 */
	public ISTMEntryKey getContainerKey()
	{
		ContainerSubscriber containerSub = metaDataControl.getContainerSubscriber( typeKey );
		if( containerSub != null )
			return containerSub.getEntryKey();
		
		return null;
	}


	/**
	 * @param parent
	 * @param inData
	 */
	private void createViewer(Composite parent, IPersistentMap<String, Object> inData ) 
	{
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(parent, viewer, inData);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(ArrayContentProvider.getInstance());
		
		table.setLayoutData( new GridData( GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL ) );

	}

	/**
	 * @return
	 */
	public TableViewer getViewer() {
		return viewer;
	}

	/**
	 * @param inData
	 */
	public void setInput( ViewDataObject[] inData )
	{
		viewer.setInput( inData );
	}

	/**
	 * @return
	 */
	public ViewDataObject[] getObjects()
	{
		ViewDataObject[] objects = new ViewDataObject[viewer.getTable().getItemCount()];
		for( int i = 0; i < viewer.getTable().getItemCount(); i++)
		{
			objects[i] = (ViewDataObject)viewer.getElementAt( i );
		}
		return objects;  
	}

	/**
	 * 
	 */
	public ViewDataObject addEntry( String inKey )
	{
		ViewDataObject viewObject = new ViewDataObject( serviceKey, typeKey, getEmptyMap(), metaData, this );
		viewer.add( viewObject );

		viewObjects.add( viewObject );
		
		return viewObject;
	}
	
	public IPersistentMap<String, Object> getEmptyMap()
	{
		IPersistentMap<String, Object> data = PersistentHashMap.EMPTY;
		
		for( Map.Entry<String,Object> entry : metaData )
		{
			if( entry.getValue() instanceof IPersistentMap )
			{
				data = data.assoc( entry.getKey(), PersistentHashMap.EMPTY );
			}
		}
		
		return data;
	}
	
	public void deleteEntry()
	{
		ISelection sel = viewer.getSelection();
		if( sel != null && !sel.isEmpty() )
		{
			Iterator iter = ((IStructuredSelection)sel).iterator();
			
			while( iter.hasNext() )
			{
				ViewDataObject obj = (ViewDataObject)iter.next();
				
				if( obj.getState() == ViewDataObjectState.CREATED )
				{
					viewObjects.remove( obj );
					viewer.remove( obj );
					return;
				}
				
				if( ! editor.qualifyForDelete( obj.getKey() ))
				{
					MessageDialog.openWarning( getShell(), "Warning", "This object cannot be deleted since other objects is dependent ion it" );
					return;
				}
				
				obj.setDeleted();
				viewer.update( obj, new String[]{STATUS_FIELD_NAME} );
			}
		}
	}
	
	private ViewDataObject getSelectedObject()
	{
		ISelection sel = viewer.getSelection();
		if( sel != null && !sel.isEmpty() )
		{
			Iterator iter = ((IStructuredSelection)sel).iterator();
			
			while( iter.hasNext() )
			{
				ViewDataObject obj = (ViewDataObject)iter.next();
				
				return obj;
			}
		}
		
		return null;
	}

	/**
	 * @param parent
	 * @param viewer
	 * @param inData
	 */
	private void createColumns(final Composite parent, final TableViewer viewer, IPersistentMap<String, Object> inData) 
	{
		PersistentArrayList<Object> keyList = (PersistentArrayList<Object>)inData.valAt( FIELD_KEYS );

		Iterator<Map.Entry<String, Object>> iter = inData.iterator();

		int i = 0;

		while( iter.hasNext() )
		{
			Map.Entry<String, Object> entry = iter.next();

			final String key = entry.getKey();

			if( entry.getValue() instanceof PersistentHashMap< ?, ? >)
			{
				final IPersistentMap< String, Object > subMap = (IPersistentMap< String, Object >)entry.getValue();
				Label tableName = new Label( parent, SWT.NONE );
				tableName.setText( key );
				
				Button newEntryButt = new Button(parent, SWT.PUSH);
				newEntryButt.setText( "New Entry" );
				
				/**New**/
				final DataViewer subView = new DataViewer( editor, serviceKey, parent, SWT.NONE, subMap, key, metaDataControl );
				/**End**/
				
//				final TableViewer subView = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
//				createColumns( parent, subView, subMap );
//				final Table table = subView.getTable();
//				table.setHeaderVisible(true);
//				table.setLinesVisible(true);

//				subView.setContentProvider(ArrayContentProvider.getInstance());
				
//				table.setLayoutData( new GridData( GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL ) );
				
				newEntryButt.addSelectionListener( new SelectionAdapter(){
					/* (non-Javadoc)
					 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
					 */
					public void widgetSelected( SelectionEvent sev )
					{
						ViewDataObject obj = getSelectedObject();
						if( obj == null )
						{
							MessageDialog.openWarning( getShell(), "Warning", "No root object is selected" );
							return;
						}
						
						subView.addEntry( key );
//						ViewDataObject viewObject = obj.addEntry( key );
//						subView.add( viewObject );

						getViewer().update( obj, null );
					}
				});
			}
			else if( !FIELD_STATUS.equals(key) && !FIELD_KEYS.equals(key) )
			{
				Object val = entry.getValue();
				TableViewerColumn col = createTableViewerColumn(viewer, key, 100, i);
				
				if( val instanceof PersistentHashMap<?, ?> )
				{
					continue;
				}
				
				col.setLabelProvider(new ColumnLabelProvider() 
				{
					@Override
					public String getText(Object element) 
					{
						ViewDataObject viewObj = (ViewDataObject)element;
						IPersistentMap<String, Object> map = viewObj.getData();
						Object data = map.valAt( key );

						if( data == null )
							return "";

						return getDataLabel( data );
					}
				});

				InputContainer input = metaDataControl.getInputContainer( val.toString() );
				if( input instanceof ReferenceInput )
				{
					referenceDependencies.put( val.toString(), key );
				}
				EditingSupport editSupport = getEditingSupport( val, input, keyList, key, parent.getDisplay(), viewer );

				if( editSupport != null )
					col.setEditingSupport( editSupport );
			}
			i++;
		}

		TableViewerColumn col = createTableViewerColumn(viewer, "", 250, i);

		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage( Object inElement )
			{
				if( inElement instanceof ViewDataObject )
				{
					ViewDataObject obj = (ViewDataObject)inElement;
					String validate = obj.validate();
					if( validate != null && !("".equals(validate)) )
						return ImageConstants.getImage( ImageConstants.WARNING );
					else
					{
						ViewDataObjectState state = obj.getState();
						switch( state )
						{
						case MIRROR : return ImageConstants.getImage( ImageConstants.OK );
						case DELETED : return ImageConstants.getImage( ImageConstants.DELETE );
						default : return ImageConstants.getImage( ImageConstants.EDITED );
						}
					}
				}
				return null;
			}

			public String getText( Object inElement )
			{
				if( inElement instanceof ViewDataObject )
				{
					ViewDataObject obj = (ViewDataObject)inElement;
					String validate = obj.validate();
					if( validate != null )
						return validate;
				}
				return "";
			}
		});
		
	}
	

	private TableViewerColumn createTableViewerColumn(TableViewer inViewer, String title, int bound, final int colNumber) 
	{
		final TableViewerColumn viewerColumn = new TableViewerColumn(inViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}



	@Override
	public void onContainerRefAdded( final ISTMEntryKey inKey, final ISTMEntry inEntry )
	{
		getDisplay().asyncExec( new Runnable(){

			@Override
			public void run()
			{
				ViewDataObject viewObject = new ViewDataObject( serviceKey, typeKey, inEntry.getDataMap(), metaData, inKey, DataViewer.this );

				for( ViewDataObject existingObject : viewObjects )
				{
					if( existingObject.getKey() != null && inKey.equals( existingObject.getKey() ) )
					{
						existingObject.setData( inEntry.getDataMap() );
						viewer.update( existingObject, new String[]{STATUS_FIELD_NAME} );
						return;
					}
				}
				viewer.add( viewObject );
				viewObjects.add( viewObject );
			}

		});

	}


	public String getType()
	{
		return typeKey;
	}

	/**
	 * @param inKey
	 * @return
	 */
	public boolean validateKey( String inKey, ISTMEntryKey inEntryKey )
	{
		for( ViewDataObject viewObj : viewObjects )
		{
			if( viewObj.getKey() != null && viewObj.getKey().equals( inKey ))
				return false;	
		}

		return true;
	}	


	@Override
	public void onContainerRefUpdated( ISTMEntryKey inKey, ISTMEntry inEntry )
	{
		// TODO Auto-generated method stub

	}



	@Override
	public void onContainerRefRemoved( final ISTMEntryKey inKey )
	{
		getDisplay().asyncExec( new Runnable(){

			@Override
			public void run()
			{
				Iterator<ViewDataObject> iter = viewObjects.iterator();
				while( iter.hasNext() )
				{
					ViewDataObject existingObject = iter.next();
					
					if( existingObject.getKey() != null && inKey.equals( existingObject.getKey() ) )
					{
						iter.remove();
						viewer.remove( existingObject );
						return;
					}
				}
			}

		});
	}

	public Set<String> getTypeDependentFields( String inType )
	{
		return referenceDependencies.get( inType );
	}

	@Override
	public void updateChild( IPersistentMap<String, Object> inData, String inKey )
	{
		// Not used
	}


}
