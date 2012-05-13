package org.juxtapose.streamline.util.message;

import org.juxtapose.streamline.util.IPublishedData;

public class SubscriptionEvent extends Message 
{
	long ref;
	IPublishedData m_data;
	boolean fullUpdate;
}
