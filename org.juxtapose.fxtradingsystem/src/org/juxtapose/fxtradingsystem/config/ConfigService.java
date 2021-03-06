package org.juxtapose.fxtradingsystem.config;

import java.util.Map;

import org.juxtapose.fxtradingsystem.constants.FXDataConstants;
import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.fxtradingsystem.priceengine.PriceEngineKeyConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;

import static org.juxtapose.streamline.tools.STMMessageConstants.*;
import static org.juxtapose.streamline.tools.STMUtil.*;
import org.juxtapose.streamline.stm.osgi.DataProducerService;
import static org.juxtapose.streamline.tools.DataConstants.*;
import org.juxtapose.streamline.tools.STMEntryKey;
import org.juxtapose.streamline.tools.STMUtil;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMRequestor;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeRef;
import org.juxtapose.streamline.util.data.DataTypeStatus;
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
	
	public void request( int inTag, long inType, ISTMRequestor inRequestor, String inVariable, IPersistentMap<String, DataType<?>> inData  )
	{
		if( "CCY".equals( inVariable ) && inType == REQUEST_TYPE_CREATE )
		{
			String iso = (String)inData.valAt( "ISO" ).get();
			String name = (String)inData.valAt( "NAME" ).get();
			String conv = (String)inData.valAt( "DC" ).get();
			
			ISTMEntryKey key = STMUtil.createEntryKey( getServiceId(), "CCY", iso );
			
			/**check for errors**/
			
			IPersistentMap<String, DataType<?>> newData = inData.assoc( FIELD_STATUS, new DataTypeStatus( Status.OK ) );
			
			ccyContainer.addEntry( key, newData );
		}
		else if( "CCY".equals( inVariable ) && inType == REQUEST_TYPE_UPDATE )
		{
			DataTypeRef ref = (DataTypeRef)inData.valAt( FIELD_KEYS );
			
			if( ref == null )
				inRequestor.reply( inTag, RESPONSE_TYPE_ERROR, REQUEST_MISSING_FIELDS, null );
			
			try
			{
				IPersistentMap<String, DataType<?>> data = inData.without( FIELD_KEYS );
				ccyContainer.updateEntry( ref.get(), data );
			}
			catch( Exception e )
			{
				stm.logError( e.getMessage(), e );
			}
		}
		if( "PRC".equals( inVariable ) && inType == REQUEST_TYPE_CREATE )
		{
			String ccy1 = (String)inData.valAt( FXDataConstants.FIELD_CCY1 ).get();
			String ccy2 = (String)inData.valAt( FXDataConstants.FIELD_CCY2 ).get();
			
			ISTMEntryKey key = STMUtil.createEntryKey( getServiceId(), "PRC", 
						new String[]{FXDataConstants.FIELD_CCY1, FXDataConstants.FIELD_CCY2},
						new String[]{ccy1, ccy2});
			
			/**check for errors**/
			
			IPersistentMap<String, DataType<?>> newData = inData.assoc( FIELD_STATUS, new DataTypeStatus( Status.OK ) );
			
			prcContainer.addEntry( key, newData );
		}
		else if( "PRC".equals( inVariable ) && inType == REQUEST_TYPE_UPDATE )
		{
			DataTypeRef ref = (DataTypeRef)inData.valAt( FIELD_KEYS );
			
			if( ref == null )
				inRequestor.reply( inTag, RESPONSE_TYPE_ERROR, REQUEST_MISSING_FIELDS, null );
			
			try
			{
				IPersistentMap<String, DataType<?>> data = inData.without( FIELD_KEYS );
				prcContainer.updateEntry( ref.get(), data );
			}
			catch( Exception e )
			{
				stm.logError( e.getMessage(), e );
			}
		}
	}

}
