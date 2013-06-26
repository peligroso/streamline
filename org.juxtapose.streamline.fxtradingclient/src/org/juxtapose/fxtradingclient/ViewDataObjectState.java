package org.juxtapose.fxtradingclient;

public enum ViewDataObjectState
{
	MIRROR, //Untouched by view client, mirrored from service
	CREATED, //Created by view client and not synchronized to service
	DELETED, //Deleted by view client but not synchronized to service
	UPDATED, //Updated by view client and not synchronized to service 
}
