package org.juxtapose.fxtradingsystem.config;

import java.util.Map;

import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.fxtradingsystem.priceengine.PriceEngineKeyConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;

import static org.juxtapose.streamline.tools.STMMessageConstants.REQUEST_NOT_SUPPORTED;
import static org.juxtapose.streamline.tools.STMUtil.*;
import org.juxtapose.streamline.stm.osgi.DataProducerService;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.tools.STMEntryKey;
import org.juxtapose.streamline.tools.STMUtil;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMRequestor;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeStatus;
import org.juxtapose.streamline.util.producerservices.DataInitializer;
import org.juxtapose.streamline.util.producerservices.DataRefContainerProducer;

import com.trifork.clj_ds.IPersistentMap;

public class ConfigService extends DataProducerService implements IConfigService 
{
	public static final ISTMEntryKey CCY_CONTAINER_KEY = STMUtil.createEntryKey( FXProducerServiceConstants.CONFIG, DataConstants.STATE_TYPE_CONTAINER, "CCY" );
	
	DataRefContainerProducer ccyContainer;
	ISTMEntryKey key;
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.osgi.DataProducerService#init()
	 */
	public void init( )
	{
		key = createEntryKey( FXProducerServiceConstants.CONFIG, DataConstants.STATE_TYPE_META, FXProducerServiceConstants.CONFIG );
		ccyContainer = new DataRefContainerProducer( CCY_CONTAINER_KEY, stm );
		super.init();
	}
	
	public DataInitializer createDataInitializer( )
	{
		DataInitializer initializer = new DataInitializer( stm, this, CCY_CONTAINER_KEY );
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
		String val = inQuery.get( DataConstants.FIELD_QUERY_KEY );
		if( val.equals( DataConstants.STATE_TYPE_META ) )
			inSubscriber.deliverKey( key, inTag );
		else
			inSubscriber.queryNotAvailible( inTag );
	}

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.ISTMEntryProducerService#getDataProducer(org.juxtapose.streamline.producer.ISTMEntryKey)
	 */
	@Override
	public ISTMEntryProducer getDataProducer( ISTMEntryKey inKey )
	{
		if( DataConstants.STATE_TYPE_META.equals( inKey.getType() ) )
		{
			return new MetaDataProducer( key, stm );
		}
		else if( CCY_CONTAINER_KEY.equals( inKey ) )
		{
			return ccyContainer;
		}
		
		return null;
	}

	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData,boolean inFullUpdate ) {
		
	}
	
	public void request( int inTag, ISTMRequestor inRequestor, String inVariable, IPersistentMap<String, DataType<?>> inData  )
	{
		if( "CCY".equals( inVariable ) )
		{
			String iso = (String)inData.valAt( "ISO" ).get();
			String name = (String)inData.valAt( "NAME" ).get();
			String conv = (String)inData.valAt( "DC" ).get();
			
			ISTMEntryKey key = STMUtil.createEntryKey( getServiceId(), "CCY", iso );
			
			/**check for errors**/
			
			IPersistentMap<String, DataType<?>> newData = inData.assoc( DataConstants.FIELD_STATUS, new DataTypeStatus( Status.OK ) );
			
			ccyContainer.addEntry( key, newData );
		}
		else
		{
			inRequestor.requestError( inTag, REQUEST_NOT_SUPPORTED );
		}
	}

}
