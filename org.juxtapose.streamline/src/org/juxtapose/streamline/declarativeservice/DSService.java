package org.juxtapose.streamline.declarativeservice;

import static org.juxtapose.streamline.tools.DataConstants.FIELD_KEYS;
import static org.juxtapose.streamline.tools.DataConstants.FIELD_QUERY_KEY;
import static org.juxtapose.streamline.tools.DataConstants.FIELD_STATUS;
import static org.juxtapose.streamline.tools.DataConstants.REQUEST_TYPE_CREATE;
import static org.juxtapose.streamline.tools.DataConstants.REQUEST_TYPE_DELETE;
import static org.juxtapose.streamline.tools.DataConstants.REQUEST_TYPE_UPDATE;
import static org.juxtapose.streamline.tools.DataConstants.RESPONSE_TYPE_ERROR;
import static org.juxtapose.streamline.tools.DataConstants.STATE_TYPE_META;
import static org.juxtapose.streamline.tools.STMMessageConstants.REQUEST_MISSING_FIELDS;
import static org.juxtapose.streamline.tools.STMUtil.createEntryKey;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.stm.osgi.DataProducerService;
import org.juxtapose.streamline.tools.ProducerServiceConstants;
import org.juxtapose.streamline.tools.STMUtil;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMRequestor;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeRef;
import org.juxtapose.streamline.util.producerservices.DataInitializer;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

public class DSService extends DataProducerService implements IDSService
{
	public ISTMEntryKey metaDataKey;
	
	private HashMap<String, MetaDataObjectContext> nameToContext;
	private IPersistentMap<String, Object> metaDataMap;
	
	private String serviceID;
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.osgi.DataProducerService#init()
	 */
	public void init( ComponentContext inContext )
	{
		serviceID = (String)inContext.getProperties().get( ProducerServiceConstants.DE_ID_KEY );
		
		if( serviceID == null )
		{
			stm.logError( "ServiceID is missing from Service Declaration" );
			return;
		}
		
		metaDataKey = createEntryKey( serviceID, STATE_TYPE_META, serviceID );
		
		metaDataMap = PersistentHashMap.EMPTY;
		parseDelarativeFile();
		super.init( inContext );
	}
	
	@Override
	public String getServiceId()
	{
		return serviceID;
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.ISTMEntryProducerService#getDataKey(org.juxtapose.streamline.util.ISTMEntryRequestSubscriber, java.lang.Object, java.util.Map)
	 */
	@Override
	public void getDataKey( ISTMEntryRequestSubscriber inSubscriber, Object inTag, Map<String, String> inQuery )
	{
		String val = inQuery.get( FIELD_QUERY_KEY );
		if( val.equals( STATE_TYPE_META ) )
			inSubscriber.deliverKey( metaDataKey, inTag );
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
			return new MetaDataProducer( metaDataKey, stm, metaDataMap );
		}
		for( MetaDataObjectContext ocjContext : nameToContext.values() )
		{
			if( inKey.equals( ocjContext.containerKey ))
			{
				return ocjContext.container;
			}
		}
		
		return null;
	}
	
	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData,boolean inFullUpdate ) {
		
	}

	
	/**
	 * 
	 */
	private void parseDelarativeFile()
	{
		HashMap<String, MetaDataObjectContext> contextMap = new HashMap<String, MetaDataObjectContext>();
		
		Bundle bundle = Platform.getBundle("org.juxtapose.fxtradingsystem");
		URL fileURL = bundle.getEntry("data/config.json");
		File file = null;
		
		String json;
		
		try 
		{
			file = new File(FileLocator.resolve(fileURL).toURI());

			BufferedReader br = new BufferedReader( new FileReader( file ) );

			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append('\n');
				line = br.readLine();
			}
			json = sb.toString();

			br.close();

			JSONParser parser=new JSONParser();
			Object obj = parser.parse(json);
			JSONObject jsObj = (JSONObject)obj;
			Set<String> keySet = jsObj.keySet();
			
			for( String key : keySet )
			{
				System.out.println(key+"\n");
				Object valObj = jsObj.get( key );
				
				if( valObj instanceof JSONArray )
				{
					JSONArray arrObj = (JSONArray)valObj;
					
					//EnumerationObject
					if( arrObj.size() > 0 && (arrObj.get( 0 ) instanceof String ) )
					{
						String[] values = new String[arrObj.size()];
						int i = 0;
						for( Object o : arrObj )
						{
							values[i++] = (String)o;
						}
						
						PersistentArrayList<String> arr = new PersistentArrayList<String>( values );
						metaDataMap = metaDataMap.assoc( key, arr );
					}
				}
				else if( valObj instanceof JSONObject )
				{
					JSONObject o = (JSONObject)valObj;
					
					MetaDataObjectContext context = new MetaDataObjectContext( serviceID, key, o, stm );
					contextMap.put( key, context );
					
					metaDataMap = metaDataMap.assoc( key, context.entryMap );
				}
			}
			
			nameToContext = contextMap;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.osgi.DataProducerService#createDataInitializer()
	 */
	public DataInitializer createDataInitializer( )
	{
		//Initialize all containers
		ISTMEntryKey[] keyArr = new ISTMEntryKey[ nameToContext.size() ];
		int i = 0;
		for( MetaDataObjectContext context : nameToContext.values() )
		{
			keyArr[i++] = context.containerKey;
		}
		DataInitializer initializer = new DataInitializer( stm, this, keyArr );
		return initializer;
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.osgi.DataProducerService#request(int, long, org.juxtapose.streamline.util.ISTMRequestor, java.lang.String, com.trifork.clj_ds.IPersistentMap)
	 */
	public void request( int inTag, long inType, ISTMRequestor inRequestor, String inVariable, IPersistentMap<String, Object> inData  )
	{
		MetaDataObjectContext context = nameToContext.get( inVariable );
		
		if( context == null )
		{
			stm.logError( "Request for Unknown variable "+inVariable );
			return;
		}
		
		if( inType == REQUEST_TYPE_CREATE )
		{
			try
			{
				ISTMEntryKey key = STMUtil.createEntryKey( getServiceId(), inVariable, inData, context.primary );
				IPersistentMap<String, Object> newData = inData.assoc( FIELD_STATUS, Status.OK );
				context.container.addEntry( key, newData );
			}
			catch( Exception e )
			{
				stm.logError( e.getMessage(), e );
			}
		}
		else if( inType == REQUEST_TYPE_UPDATE || inType == REQUEST_TYPE_DELETE )
		{
			DataTypeRef ref = (DataTypeRef)inData.valAt( FIELD_KEYS );
			if( ref == null )
				inRequestor.reply( inTag, RESPONSE_TYPE_ERROR, REQUEST_MISSING_FIELDS, null );
			
			try
			{
				if( inType == REQUEST_TYPE_UPDATE )
				{
					IPersistentMap<String, Object> data = inData.without( FIELD_KEYS );
					context.container.updateEntry( ref.get(), data );
				}
				else
				{
					context.container.removeEntry( ref.get() );
				}
				
			}
			catch( Exception e )
			{
				stm.logError( e.getMessage(), e );
			}
		}		
	}
}
