package org.juxtapose.fxtradingsystem.marketdata;

public interface IMarketDataSubscriber
{
	public void marketDataUpdated( QPMessage inMessage );
}
