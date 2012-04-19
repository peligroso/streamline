package org.juxtapose.fxtradingsystem.marketdata;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class MarketDataSource
{
	static ConcurrentHashMap<String, MarketDataSource> nameToSource = new ConcurrentHashMap<String, MarketDataSource>();
	
	static Object mutex = new Object();
	
	public static void addSubscriber( IMarketDataSubscriber inSubscriber, QPMessage inSubscribeMessage, String inSource )
	{
		MarketDataSource source;
		
		synchronized( mutex )
		{
			source = nameToSource.get( inSource );

			if( source == null )
			{
				source = new MarketDataSource( inSource );
				
				nameToSource.put( inSource, source );
			}
		}
		
		source.addSubscriber( inSubscriber, inSubscribeMessage );
	}
	
	public static void removeSubscriber( IMarketDataSubscriber inSubscriber, String inSource )
	{
		MarketDataSource source;
		
		synchronized( mutex )
		{
			source = nameToSource.get( inSource );

			if( source != null )
			{
				source.removeSubscriber( inSubscriber );
			}
		}
	}
	
	String name;
	ConcurrentHashMap<IMarketDataSubscriber, QPMessage> subscribedInstruments = new ConcurrentHashMap<IMarketDataSubscriber, QPMessage>();
	Random rand = new Random();
	
	MarketDataSource( String inSourceName )
	{
		name = inSourceName;
		
		Thread updateThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					for(;;)
					{
						for(IMarketDataSubscriber sub : subscribedInstruments.keySet().toArray( new IMarketDataSubscriber[]{} ) )
						{
							QPMessage instDef = subscribedInstruments.get( sub );
							double bid = rand.nextDouble();
							double ask = rand.nextDouble();
							
							if( instDef == null )
								continue;
							
							QPMessage quoteMessage = new QPMessage( QPMessage.QUOTE, instDef.ccy1, instDef.ccy2, instDef.period, bid, ask );
							
							sub.marketDataUpdated( quoteMessage );

							int sleepTime = rand.nextInt( 10 );

							Thread.sleep( sleepTime );

						}
						int sleepTime = rand.nextInt( 10 );

						Thread.sleep( sleepTime );
					}
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			
		});
		
		updateThread.start();
	}
	
	private void addSubscriber( IMarketDataSubscriber inSubscriber, QPMessage inSubscribeMessage )
	{
		subscribedInstruments.put( inSubscriber, inSubscribeMessage );
	}
	
	private void removeSubscriber( IMarketDataSubscriber inSubscriber )
	{
		subscribedInstruments.remove( inSubscriber );
	}
	
}
