package org.juxtapose.streamline.swt.datatable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import static org.juxtapose.streamline.swt.spl.ClientViewMethods.*;
import org.juxtapose.streamline.swt.spl.ImageConstants;
import org.juxtapose.streamline.tools.CollectionMethods;
import static org.juxtapose.streamline.tools.DataConstants.*;
import org.juxtapose.streamline.util.ContainerSubscriber;
import org.juxtapose.streamline.util.ISTMContainerListener;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.PersistentArrayList;

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

	IPersistentMap<String, Object> metaData;

	Set<ViewDataObject> viewObjects = new HashSet<ViewDataObject>();


	public DataViewer( String inServiceKey, Composite parent, int style, IPersistentMap<String, Object> inData, String inTypeKey, MetaDataControl inMetaDataControl ) 
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



	private void createViewer(Composite parent, IPersistentMap<String, Object> inData ) 
	{
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(parent, viewer, inData);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(ArrayContentProvider.getInstance());
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
	 private void createColumns(final Composite parent, final TableViewer viewer, IPersistentMap<String, Object> inData) 
	 {
		 PersistentArrayList<Object> keyList = (PersistentArrayList<Object>)inData.valAt( FIELD_KEYS );

		 Iterator<Map.Entry<String, Object>> iter = inData.iterator();

		 int i = 0;

		 while( iter.hasNext() )
		 {
			 Map.Entry<String, Object> entry = iter.next();

			 final String key = entry.getKey();

			 if( !FIELD_STATUS.equals(key) && !FIELD_KEYS.equals(key) )
			 {
				 Object val = entry.getValue();
				 TableViewerColumn col = createTableViewerColumn(key, 100, i);

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
				 EditingSupport editSupport = getEditingSupport( val, input, keyList, key, parent.getDisplay(), viewer );
				 
				 if( editSupport != null )
					 col.setEditingSupport( editSupport );
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
				 ViewDataObject viewObject = new ViewDataObject( serviceKey, typeKey, inEntry.getDataMap(), metaData, inKey );

				 for( ViewDataObject existingObject : viewObjects )
				 {
					 if( existingObject.getKey() != null && inKey.equals( existingObject.getKey() ) )
					 {
						 existingObject.setData( inEntry.getDataMap() );
						 viewer.update( existingObject, new String[]{"NAME"} );
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
