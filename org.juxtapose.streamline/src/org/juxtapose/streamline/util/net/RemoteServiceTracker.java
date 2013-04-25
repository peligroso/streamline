package org.juxtapose.streamline.util.net;

import java.util.HashMap;

import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.Status;

/**
 * @author Pontus Jörgne
 * 25 apr 2013
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class RemoteServiceTracker
{
	HashMap<String, RemoteServiceProxy> serviceProxies = new HashMap<String, RemoteServiceProxy>();

	ISTM stm;
	
	public RemoteServiceTracker( ISTM inSTM )
	{
		stm = inSTM;
	}
	
	/**
	 * @param inService
	 * @param inStatus
	 */
	public void statusUpdated( String inService, Status inStatus )
	{
		RemoteServiceProxy serviceProxy = serviceProxies.get( inService );
		
		if( serviceProxy == null )
		{
			serviceProxy = new RemoteServiceProxy( inService, stm, inStatus );
		}
		
	}
	
}
