package org.juxtapose.fxtradingclient;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.juxtapose.fxtradingclient.tools.ImageConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.tools.CollectionMethods;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.util.ISTMContainerListener;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeArrayList;
import org.juxtapose.streamline.util.data.DataTypeLong;
import org.juxtapose.streamline.util.data.DataTypeString;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

/**
 * @author Pontus Jörgne
 * 1 jun 2013
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class DataViewer extends Composite implements ISTMContainerListener
{
	String typeKey;
	String serviceKey;
	
	TableViewer viewer;
	MetaDataControl metaDataControl;
	
	IPersistentMap<String, DataType<?>> metaData;
	
	Set<ViewDataObject> viewObjects = new HashSet<ViewDataObject>();
	
	
	public DataViewer( String inServiceKey, Composite parent, int style, IPersistentMap<String, DataType<?>> inData, String inTypeKey, MetaDataControl inMetaDataControl ) 
	{
		super( parent, style );
		
		typeKey = inTypeKey;
		serviceKey = inServiceKey;
		
		metaDataControl = inMetaDataControl;
		
		metaData = inData;
		
		setLayout( new FillLayout() );
		createViewer( this, inData );
		
		ContainerSubscriber containerSub = metaDataControl.getContainerSubscriber( typeKey );
		containerSub.addContainerListener( this );
		
	}

	
	
	private void createViewer(Composite parent, IPersistentMap<String, DataType<?>> inData ) 
	{
	    viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
	    createColumns(parent, viewer, inData);
	    final Table table = viewer.getTable();
	    table.setHeaderVisible(true);
	    table.setLinesVisible(true);

	    viewer.setContentProvider(ArrayContentProvider.getInstance());
	    // Get the content for the viewer, setInput will call getElements in the
	    // contentProvider
//	    viewer.setInput(ModelProvider.INSTANCE.getPersons());
	    // Make the selection available to other views
	    
	    // Set the sorter for the table

	    // Layout the viewer
//	    GridData gridData = new GridData();
//	    gridData.verticalAlignment = GridData.FILL;
//	    gridData.horizontalSpan = 2;
//	    gridData.grabExcessHorizontalSpace = true;
//	    gridData.grabExcessVerticalSpace = true;
//	    gridData.horizontalAlignment = GridData.FILL;
//	    viewer.getControl().setLayoutData(gridData);
	  }

	  public TableViewer getViewer() {
	    return viewer;
	  }
	  
	  public void setInput( ViewDataObject[] inData )
	  {
		  viewer.setInput( inData );
	  }
	  
	  public ViewDataObject[] getObjects()
	  {
		  ViewDataObject[] objects = new ViewDataObject[viewer.getTable().getItemCount()];
		 for( int i = 0; i < viewer.getTable().getItemCount(); i++)
		 {
			 objects[i] = (ViewDataObject)viewer.getElementAt( i );
		 }
		 return objects;  
	  }
	  
	  public void addEntry()
	  {
		  ViewDataObject viewObject = new ViewDataObject( serviceKey, typeKey, PersistentHashMap.EMPTY, metaData );
		  viewer.add( viewObject );
		  
		  viewObjects.add( viewObject );
	  }
	  	
	  
	/**
	 * @param parent
	 * @param viewer
	 * @param inData
	 */
	private void createColumns(final Composite parent, final TableViewer viewer, IPersistentMap<String, DataType<?>> inData) 
	{
		DataTypeArrayList keyList = (DataTypeArrayList)inData.valAt( DataConstants.FIELD_KEYS );
		
	    Iterator<Map.Entry<String, DataType<?>>> iter = inData.iterator();
	    
	    int i = 0;
	    
	    while( iter.hasNext() )
	    {
	    	Map.Entry<String, DataType<?>> entry = iter.next();
	    	
	    	final String key = entry.getKey();
	    	
	    	if( DataConstants.FIELD_KEYS.equals(key) )
	    	{
	    		
	    	}
	    	else if( !DataConstants.FIELD_STATUS.equals(key) )
	    	{
	    		DataType<?> val = entry.getValue();
	    		TableViewerColumn col = createTableViewerColumn(key, 100, i);
	    		
	    		col.setLabelProvider(new ColumnLabelProvider() 
	    		{
	    		      @Override
	    		      public String getText(Object element) 
	    		      {
	    		    	ViewDataObject viewObj = (ViewDataObject)element;
	    		    	IPersistentMap<String, DataType<?>> map = viewObj.getData();
	    		    	DataType<?> data = map.valAt( key );
	    		    	
	    		    	if( data == null )
	    		    		return "";
	    		    	
	    		    	return data.get().toString();
	    		      }
	    		    });
	    		
	    		if( val instanceof DataTypeString )
	    		{
	    			String valStr = ((DataTypeString)val).get();
	    			if( valStr.isEmpty() )
	    			{
	    				col.setEditingSupport( new DataEditingSupport( viewer, key, CollectionMethods.contains( keyList, new DataTypeString(key) ) ) );
	    			}
	    			else
	    			{
	    				//value is a predefined type
	    				InputContainer input = metaDataControl.getInputContainer( valStr );
	    				col.setEditingSupport( new DataEditingSupportEnum( viewer, key, input, parent.getDisplay(), CollectionMethods.contains( keyList, new DataTypeString(key) ) ) );
	    			}
	    		}
	    		else if( val instanceof DataTypeLong )
	    		{
	    			col.setEditingSupport( new DataEditingSupportLong( viewer, key, CollectionMethods.contains( keyList, new DataTypeString(key) ) ) );
	    		}
	    		else if( val instanceof DataTypeArrayList )
	    		{
	    			InputContainer input = metaDataControl.getInputContainer( key );
    				col.setEditingSupport( new DataEditingSupportEnum( viewer, key, input, parent.getDisplay(), CollectionMethods.contains( keyList, new DataTypeString(key) ) ) );
	    		}
	    	}
	    	i++;
	    }
	    
	    TableViewerColumn col = createTableViewerColumn("", 100, i);
	
	    col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public Image getImage( Object inElement )
            {
            	Image im = ImageConstants.getImage( ImageConstants.TEST );
            	return im;
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
	
	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) 
	{
	    final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
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
				ViewDataObject viewObject = new ViewDataObject( serviceKey, typeKey, inEntry.getDataMap(), metaData );
				
				for( ViewDataObject existingObject : viewObjects )
				{
					if( existingObject.getKey() != null && viewObject.getKey().equals( existingObject.getKey() ) )
					{
						existingObject.setData( inEntry.getDataMap() );
						viewer.update( existingObject, new String[]{} );
						return;
					}
				}
				viewer.add( viewObject );
			}
			
		});
		
	}



	@Override
	public void onContainerRefUpdated( ISTMEntryKey inKey, ISTMEntry inEntry )
	{
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onContainerRefRemoved( ISTMEntryKey inKey, ISTMEntry inEntry )
	{
		// TODO Auto-generated method stub
		
	}




}
