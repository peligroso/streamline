package org.juxtapose.fxtradingsystem.ordermanager;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.juxtapose.fxtradingsystem.FXDataConstants;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.MultiThreadedClaimStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;

public class ClientConnector
{
	final OrderManager manager;
	
	Disruptor<ClientEvent> disruptor;
	RingBuffer<ClientEvent> ringBuffer;
	
	Random rand = new Random();
	
	long tag = 0;
	
	long lastRFQTime = 0;
	
	long maxTimeBetweenRFQ = 100l * 1000000l;
	
	int maxRFQs = 3500;
	int warmup = 2000;
	
	int avgPriceUpdates = 10;
	
//	String[][] instruments = new String[][]{{"EUR", "SEK"}, {"EUR", "NOK"}, {"EUR", "USD"}, {"EUR", "DKK"}, {"EUR", "GBP"}, {"EUR", "TRY"}, {"EUR", "RUB"}, {"EUR", "AUD"}, {"EUR", "CHF"},{"EUR", "NZD"}, {"EUR", "CAD"}, {"EUR", "SGD"}, {"EUR", "JPY"}};
	
	String[][] instruments = new String[][]{{"SEK", "NOK"}};
	
	Map<Long, Long> updateToCount = new TreeMap<Long, Long>();
	Map<Long, Long> firstTakeToCount = new TreeMap<Long, Long>();
	
	ScheduledThreadPoolExecutor executor;
	
	private void init()
	{
		initStatsContainer( updateToCount );
		initStatsContainer( firstTakeToCount );
		
		executor = new ScheduledThreadPoolExecutor( 2 );
	}
	
	private void initStatsContainer( Map<Long, Long> inMap )
	{
		inMap.put( 10000l, 0l );
		inMap.put( 15000l, 0l );
		inMap.put( 20000l, 0l );
		inMap.put( 30000l, 0l );
		inMap.put( 40000l, 0l );
		inMap.put( 60000l, 0l );
		inMap.put( 80000l, 0l );
		inMap.put( 100000l, 0l );
		inMap.put( 150000l, 0l );
		inMap.put( 200000l, 0l );
		inMap.put( 300000l, 0l );
		inMap.put( 500000l, 0l );
		inMap.put( 1000000l, 0l );
		inMap.put( 2000000l, 0l );
		inMap.put( 5000000l, 0l );
		inMap.put( 1000000000l, 0l );
	}
	
	public void printStats()
	{
		System.out.println("FirstTake stats: ");
		printStats( firstTakeToCount );
		
		System.out.println();
		
		System.out.println("Undate stats: ");
		printStats( updateToCount );
	}
	
	public void printStats( Map<Long, Long> inMap )
	{
		for( Long benchMark : inMap.keySet() )
		{
			Long count = inMap.get(  benchMark );
			
			System.out.println(benchMark+" : "+count);
		}
	}
	
	private void addSample( Long inTime, Map<Long, Long> inStatContainer )
	{
		for( Long benchMark : inStatContainer.keySet() )
		{
			if( inTime < benchMark )
			{
				Long count = inStatContainer.get( benchMark )+1;
				inStatContainer.put( benchMark, count );
				return;
			}
		}
	}
	public ClientConnector( OrderManager inManager )
	{
		init();
		
		EventHandler<ClientEvent> clientEventHandler = new EventHandler<ClientEvent>()
		{
			@Override
			public void onEvent(ClientEvent event, long sequence, boolean endOfBatch) throws Exception
			{
				RFQMessage inCommingMess = event.message;

				if( inCommingMess.messageType == RFQMessage.TYPE_PRICING )
				{
					if( inCommingMess.tag > warmup )
					{
						if( inCommingMess.firstTakeTime != null )
						{
							addSample( inCommingMess.firstTakeTime, firstTakeToCount );
//							System.out.println( "FirstTakeTime for rfq "+inCommingMess.tag+" = "+inCommingMess.firstTakeTime+" with price "+inCommingMess.bidPrice+" / "+inCommingMess.askPrice+" sequence "+inCommingMess.sequence  );
						}
						else
						{
							addSample( inCommingMess.updateTime, updateToCount );
//							System.out.println( "Price "+inCommingMess.ccy1+inCommingMess.ccy2+" is "+inCommingMess.bidPrice+" / "+inCommingMess.askPrice+" sequence "+inCommingMess.sequence+" updatetime: "+inCommingMess.updateTime+"   id: "+inCommingMess.tag);
						}
					}

					if( inCommingMess.sequence == avgPriceUpdates )//rand.nextInt( avgPriceUpdates ) == 1 )
					{
						RFQMessage dr = new RFQMessage( RFQMessage.TYPE_DR, inCommingMess.ccy1, inCommingMess.ccy2, inCommingMess.tag, inCommingMess.bidPrice, inCommingMess.askPrice, null, null, 0, BigDecimal.ONE );
						manager.sendDR( dr );
						
						if( inCommingMess.tag == maxRFQs -1 )
						{
							printStats();
						}
					}
				}
			}

		};
		
		disruptor = new Disruptor<ClientEvent>(ClientEvent.EVENT_FACTORY, executor, new MultiThreadedClaimStrategy(2048), new SleepingWaitStrategy());
		
		/**Code to use multiple consumers for disruptor**/
//		ClientEventHandler handler1 = new ClientEventHandler(0, 2);
//		ClientEventHandler handler2 = new ClientEventHandler(1, 2);
//		disruptor.handleEventsWith(handler1, handler2);
		
		disruptor.handleEventsWith( clientEventHandler );
		ringBuffer = disruptor.start();
		
		manager = inManager;
		
		startRFQThread();
	}
	
	private void startRFQThread()
	{
		Thread rfqThread = new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					int i = 0;
					for(;;)
					{
//						RFQMessage inCommingMess = incomming.poll( 100, TimeUnit.MILLISECONDS );
//						
//						if( inCommingMess != null )
//						{
//							if( inCommingMess.messageType == RFQMessage.TYPE_PRICING )
//							{
//								if( inCommingMess.firstTakeTime != null )
//									System.out.println( "FirstTakeTime for rfq "+inCommingMess.tag+" = "+inCommingMess.firstTakeTime+" with price "+inCommingMess.bidPrice+" / "+inCommingMess.askPrice+" sequence "+inCommingMess.sequence  );
//								else
//									System.out.println( "Price is "+inCommingMess.bidPrice+" / "+inCommingMess.askPrice+" sequence "+inCommingMess.sequence+" updatetime: "+inCommingMess.updateTime+"   id: "+inCommingMess.tag);
//								
//							}
//						}
						if( lastRFQTime == 0 )
						{
							sendRFQ();
						}
						else
						{
							long now = System.nanoTime();
							long timeSinceLast = now - lastRFQTime;
							if( timeSinceLast > maxTimeBetweenRFQ && tag < maxRFQs )
							{
//								System.out.println("it has been "+timeSinceLast+" since last RFQ.. placing another ("+timeSinceLast+" > "+maxTimeBetweenRFQ+")"); 
								sendRFQ();
							}
						}
						
						i++;
					}
				} catch ( Throwable t )
				{
					t.printStackTrace();
				}
			}
			
		}, "RFQ Requestor");
		
		rfqThread.start();
	}
	
	private void sendRFQ( )
	{
		lastRFQTime = System.nanoTime();
		String[] instrument = instruments[ rand.nextInt( instruments.length ) ];
		RFQMessage rfq = new RFQMessage( instrument[0], instrument[1], FXDataConstants.STATE_INSTRUMENT_SPOT, null, null, tag++, new BigDecimal( 7000000 ) );
		manager.sendRFQ( rfq );
	}
	
	public void updateRFQ( RFQMessage inMessage )
	{
		long sequence = ringBuffer.next();
		ClientEvent event = ringBuffer.get(sequence);

		event.setMessage( inMessage );
		
		ringBuffer.publish(sequence);
		
//		incomming.offer( inMessage );
	}
}
