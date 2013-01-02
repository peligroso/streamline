package org.juxtapose.streamline.stm.de;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.ISTMEntryProducerService;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.producerservices.ProducerServiceConstants;

/**
 * @author Pontus Jörgne
 * Oct 19, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class DeclarativeEntriesService implements ISTMEntryProducerService 
{
	ISTM m_stm;
	
	public void init( ISTM inSTM )
	{
		m_stm = inSTM;
		
		String dir = "D:\\peligroso\\ccy.csv";
		try
		{
			DeclarativeEntriesProtocol.parseFile( dir );
		}
		catch( Exception e )
		{
			m_stm.logError( e.getMessage(), e );
		}
		m_stm.updateProducerStatus( this, Status.OK );
	}
	
	
	
	@Override
	public String getServiceId() 
	{
		return ProducerServiceConstants.DE_SERVICE_KEY;
	}

	@Override
	public void getDataKey( ISTMEntryRequestSubscriber inSubscriber, Object inTag, Map<String, String> inQuery ) 
	{
		
	}

	@Override
	public ISTMEntryProducer getDataProducer( ISTMEntryKey inDataKey ) 
	{
		return null;
	}

}
