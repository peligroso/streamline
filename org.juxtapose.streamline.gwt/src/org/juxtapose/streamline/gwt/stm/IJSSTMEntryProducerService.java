package org.juxtapose.streamline.gwt.stm;

import java.util.Map;

/**
 * @author Pontus Jörgne
 * 20 apr 2015
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public interface IJSSTMEntryProducerService {
	
	public String getServiceId();
	
	public void getDataKey( IJSSTMEntrySubscriber inSubscriber, Object inTag, Map<String, String> inQuery );
	public IJSSTMEntryProducer getDataProducer( String inDataKey );

}
