package org.juxtapose.fxtradingsystem.config;

import static org.juxtapose.streamline.tools.DataConstants.*;
import static org.juxtapose.streamline.tools.STMMessageConstants.REQUEST_MISSING_FIELDS;
import static org.juxtapose.streamline.tools.STMUtil.createEntryKey;

import java.util.Map;

import org.juxtapose.fxtradingsystem.constants.FXDataConstants;
import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.stm.osgi.DataProducerService;
import org.juxtapose.streamline.tools.STMUtil;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMRequestor;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeLazyRef;
import org.juxtapose.streamline.util.data.DataTypeRef;
import org.juxtapose.streamline.util.producerservices.DataInitializer;
import org.juxtapose.streamline.util.producerservices.DataRefContainerProducer;

import com.trifork.clj_ds.IPersistentMap;

public class ConfigService extends DataProducerService implements IConfigService 
{
	public static final ISTMEntryKey CCY_CONTAINER_KEY = STMUtil.createEntryKey( FXProducerServiceConstants.CONFIG, STATE_TYPE_CONTAINER, "CCY" );
	public static final ISTMEntryKey PRC_CONTAINER_KEY = STMUtil.createEntryKey( FXProducerServiceConstants.CONFIG, STATE_TYPE_CONTAINER, "PRC" );
	public static final ISTMEntryKey META_DATA_KEY = createEntryKey( FXProducerServiceConstants.CONFIG, STATE_TYPE_META, FXProducerServiceConstants.CONFIG );
	
	DataRefContainerProducer ccyContainer;
	DataRefContainerProducer prcContainer;
	
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.osgi.DataProducerService#init()
	 */
	public void init( )
	{
		ccyContainer = new DataRefContainerProducer( CCY_CONTAINER_KEY, stm );
		prcContainer = new DataRefContainerProducer( PRC_CONTAINER_KEY, stm );
		super.init();
	}
	
	public DataInitializer createDataInitializer( )
	{
		DataInitializer initializer = new DataInitializer( stm, this, CCY_CONTAINER_KEY, PRC_CONTAINER_KEY );
		return initializer;
	}
	
	@Override
	public String getServiceId()
	{
		return FXProducerServiceConstants.CONFIG;
	}

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.ISTMEntryProducerService#getDataKey(org.juxtapose.streamline.util.ISTMEntryRequestSubscriber, java.lang.Object, java.util.Map)
	 */
	@Override
	public void getDataKey( ISTMEntryRequestSubscriber inSubscriber, Object inTag, Map<String, String> inQuery )
	{
		String val = inQuery.get( FIELD_QUERY_KEY );
		if( val.equals( STATE_TYPE_META ) )
			inSubscriber.deliverKey( META_DATA_KEY, inTag );
		else
			inSubscriber.queryNotAvailible( inTag );
	}

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.ISTMEntryProducerService#getDataProducer(org.juxtapose.streamline.producer.ISTMEntryKey)
	 */
	@Override
	public ISTMEntryProducer getDataProducer( ISTMEntryKey inKey )
	{
		if( STATE_TYPE_META.equals( inKey.getType() ) )
		{
			return new MetaDataProducer( META_DATA_KEY, stm );
		}
		else if( CCY_CONTAINER_KEY.equals( inKey ) )
		{
			return ccyContainer;
		}
		else if( PRC_CONTAINER_KEY.equals( inKey ) )
		{
			return prcContainer;
		}
		
		return null;
	}

	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData,boolean inFullUpdate ) {
		
	}
	
	public void request( int inTag, long inType, ISTMRequestor inRequestor, String inVariable, IPersistentMap<String, Object> inData  )
	{
		if( "CCY".equals( inVariable ) && inType == REQUEST_TYPE_CREATE )
		{
			String iso = (String)inData.valAt( "ISO" );
			String name = (String)inData.valAt( "NAME" );
			String conv = (String)inData.valAt( "DC" );
			
			ISTMEntryKey key = STMUtil.createEntryKey( getServiceId(), "CCY", iso );
			
			/**check for errors**/
			
			IPersistentMap<String, Object> newData = inData.assoc( FIELD_STATUS, Status.OK );
			
			ccyContainer.addEntry( key, newData );
		}
		else if( "CCY".equals( inVariable ) && inType == REQUEST_TYPE_UPDATE )
		{
			DataTypeRef ref = (DataTypeRef)inData.valAt( FIELD_KEYS );
			
			if( ref == null )
				inRequestor.reply( inTag, RESPONSE_TYPE_ERROR, REQUEST_MISSING_FIELDS, null );
			
			try
			{
				IPersistentMap<String, Object> data = inData.without( FIELD_KEYS );
				ccyContainer.updateEntry( ref.get(), data );
			}
			catch( Exception e )
			{
				stm.logError( e.getMessage(), e );
			}
		}
		else if( "CCY".equals( inVariable ) && inType == REQUEST_TYPE_DELETE )
		{
			DataTypeRef ref = (DataTypeRef)inData.valAt( FIELD_KEYS );
			
			if( ref == null )
				inRequestor.reply( inTag, RESPONSE_TYPE_ERROR, REQUEST_MISSING_FIELDS, null );
			
			try
			{
				ccyContainer.removeEntry( ref.get() );
			}
			catch( Exception e )
			{
				stm.logError( e.getMessage(), e );
			}
		}
		else if( "PRC".equals( inVariable ) && inType == REQUEST_TYPE_CREATE )
		{
			DataTypeLazyRef ccy1 = (DataTypeLazyRef)inData.valAt( FXDataConstants.FIELD_CCY1 );
			DataTypeLazyRef ccy2 = (DataTypeLazyRef)inData.valAt( FXDataConstants.FIELD_CCY2 );
			
			ISTMEntryKey key = STMUtil.createEntryKey( getServiceId(), "PRC", 
						new String[]{FXDataConstants.FIELD_CCY1, FXDataConstants.FIELD_CCY2},
						new String[]{ccy1.get().getSymbolicName(), ccy2.get().getSymbolicName()});
			
			/**check for errors**/
			
			IPersistentMap<String, Object> newData = inData.assoc( FIELD_STATUS, Status.OK );
			
			prcContainer.addEntry( key, newData );
		}
		else if( "PRC".equals( inVariable ) && inType == REQUEST_TYPE_UPDATE )
		{
			DataTypeRef ref = (DataTypeRef)inData.valAt( FIELD_KEYS );
			
			if( ref == null )
				inRequestor.reply( inTag, RESPONSE_TYPE_ERROR, REQUEST_MISSING_FIELDS, null );
			
			try
			{
				IPersistentMap<String, Object> data = inData.without( FIELD_KEYS );
				prcContainer.updateEntry( ref.get(), data );
			}
			catch( Exception e )
			{
				stm.logError( e.getMessage(), e );
			}
		}
		else if( "PRC".equals( inVariable ) && inType == REQUEST_TYPE_DELETE )
		{
			DataTypeRef ref = (DataTypeRef)inData.valAt( FIELD_KEYS );
			
			if( ref == null )
				inRequestor.reply( inTag, RESPONSE_TYPE_ERROR, REQUEST_MISSING_FIELDS, null );
			
			try
			{
				prcContainer.removeEntry( ref.get() );
			}
			catch( Exception e )
			{
				stm.logError( e.getMessage(), e );
			}
		}
	}

}
