package org.juxtapose.streamline.stm.osgi;

import org.juxtapose.streamline.producer.executor.BlockingQueueExecutor;
import org.juxtapose.streamline.producer.executor.StickyHashDisruptorExecutor;
import org.juxtapose.streamline.stm.BlockingSTM;
import org.juxtapose.streamline.stm.ISTMEntryFactory;
import org.osgi.service.component.ComponentContext;

public class BSTMActivator extends BlockingSTM
{
	public static String PROP_DATA_FACTORY_CLASS = "PROP_DATA_FACTORY_CLASS";
	
	public void activate( ComponentContext inContext )
	{
		Object temp = inContext.getProperties().get( PROP_DATA_FACTORY_CLASS );
		
		if( temp != null )
		{
			String classStr = (String)temp;
			try
			{
				Class<?> c = Class.forName( classStr );
				ISTMEntryFactory dataFactory =  (ISTMEntryFactory)c.newInstance();
				
				setDataFactory( dataFactory );
			} 
			catch (ClassNotFoundException e)
			{
				logError( e.getMessage() );
			} 
			catch (InstantiationException e)
			{
				logError( e.getMessage() );
			} 
			catch (IllegalAccessException e)
			{
				logError( e.getMessage() );
			}
		}
		init( new BlockingQueueExecutor( 3, 3, 2, 2 ), true);
//		init( new StickyHashDisruptorExecutor( 3, 3, 2, 2, true ));
	}

}
