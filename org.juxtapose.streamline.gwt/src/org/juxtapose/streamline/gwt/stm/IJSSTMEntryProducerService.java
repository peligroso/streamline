package org.juxtapose.streamline.gwt.stm;

import java.util.Map;

/**
 * @author Pontus J�rgne
 * 20 apr 2015
 * Copyright (c) Pontus J�rgne. All rights reserved
 */
public interface IJSSTMEntryProducerService {
	
	public String getServiceId();
	
	public void getDataKey( IJSSTMEntrySubscriber inSubscriber, String inTag, Map<String, String> inQuery );
	public IJSSTMEntryProducer getDataProducer( String inDataKey );

}
