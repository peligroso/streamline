package org.juxtapose.fxtradingclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.tools.STMUtil;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.STMEntrySubscriber;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeArrayList;
import org.juxtapose.streamline.util.data.DataTypeHashMap;
import org.juxtapose.streamline.util.data.DataTypeString;

public class MetaDataControl extends STMEntrySubscriber
{
	boolean metaDataInitiated = false;
	
	final EditView editView;
	
	HashMap<String, ContainerSubscriber> typeToContainer = new HashMap<String, ContainerSubscriber>();
	
	public MetaDataControl( EditView inView )
	{
		editView = inView;
	}
	
	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFullUpdate )
	{
		if( !STMUtil.isStatusUpdatedToOk( inData, inFullUpdate ) || metaDataInitiated )
			return;
		
		stm.logInfo( "Config data recieved for meta data "+inData.getDataMap() );
		
		Iterator<Entry<String, DataType<?>>> iter = inData.getDataMap().iterator();
		
		while( iter.hasNext() )
		{
			Entry<String, DataType<?>> entry = iter.next();
			DataType<?> value = entry.getValue();
			
			if( value instanceof DataTypeArrayList )
			{
				//Enums
				PersistentArrayList<DataType<?>> list = (PersistentArrayList<DataType<?>>)value.get();
				ArrayList<String> enumValues = new ArrayList<String>();
				
				for( int i = 0; i < list.size(); i++ )
				{
					DataTypeString data = (DataTypeString)list.get(i);
					enumValues.add( data.get() );
				}
				
				EnumInput enumInput = new EnumInput( enumValues.toArray( new String[]{} ) );
			}
			else if( value instanceof DataTypeHashMap )
			{
				//Container value
				final String fieldKey = entry.getKey();
				ISTMEntryKey containerEntryKey = STMUtil.createEntryKey( FXProducerServiceConstants.CONFIG, DataConstants.STATE_TYPE_CONTAINER, fieldKey );
				ContainerSubscriber containerSub = new ContainerSubscriber();
				
				ReferenceInput refInput = new ReferenceInput( containerSub );
				containerSub.initialize( stm, containerEntryKey );
				
				typeToContainer.put( fieldKey, containerSub );
				
				editView.addViewer( fieldKey, inData.getDataMap() );
				
//				parent.getDisplay().asyncExec( new Runnable()
//				{
//					@Override
//					public void run() 
//					{
//						viewer = new DataViewer( parent, SWT.NONE, inData.getDataMap(), fieldKey );
//						parent.layout();
//						parent.update();
//					}
//				});
			}
		}
		
		metaDataInitiated = true;
//		
//				viewer = new DataViewer( parent, SWT.NONE, inData.getDataMap(), "CCY" );
//				parent.layout();
//				parent.update();
//			
//	}	
//				
////				IPersistentMap<String, DataType<?>> testPrice = PersistentHashMap.EMPTY;
////				testPrice = testPrice.assoc( FXDataConstants.FIELD_DECIMALS, new DataTypeLong(4l) );
////				testPrice = testPrice.assoc( FXDataConstants.FIELD_PIP, new DataTypeLong(10000l) );
////				testPrice = testPrice.assoc( FXDataConstants.FIELD_CCY1, new DataTypeString("EUR") );
////				testPrice = testPrice.assoc( FXDataConstants.FIELD_CCY2, new DataTypeString("SEK") );
////				viewer.setInput( new ViewDataObject[]{ new ViewDataObject(testPrice) } );
//				
//				IPersistentMap<String, DataType<?>> testCcy = PersistentHashMap.EMPTY;
//				testCcy = testCcy.assoc( "NAME", new DataTypeString("European currency") );
//				testCcy = testCcy.assoc( "DC", new DataTypeString("30_360_Accual") );
//				testCcy = testCcy.assoc( "ISO", new DataTypeString("EUR") );
//				viewer.setInput( new ViewDataObject[]{ new ViewDataObject(testCcy) } );
//			}
//		});
		
	}

	@Override
	public void queryNotAvailible( Object inTag )
	{
		// TODO Auto-generated method stub
		
	}


}
