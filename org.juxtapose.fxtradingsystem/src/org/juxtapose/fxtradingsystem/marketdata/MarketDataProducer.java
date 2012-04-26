package org.juxtapose.fxtradingsystem.marketdata;

import org.juxtapose.streamline.producer.DataProducer;
import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.stm.DataTransaction;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.STMTransaction;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeBigDecimal;
import org.juxtapose.streamline.util.data.DataTypeLong;

/**
 * @author Pontus J�rgne
 * Jan 22, 2012
 * Copyright (c) Pontus J�rgne. All rights reserved
 */
public class MarketDataProducer extends DataProducer implements IMarketDataSubscriber
{
	final String source;
	final String ccy1;
	final String ccy2;
	final String period;
	
	/**
	 * @param inKey
	 * @param inSTM
	 */
	public MarketDataProducer( IDataKey inKey, ISTM inSTM )
	{
		super( inKey, inSTM );
		
		source = dataKey.getValue( MarketDataConstants.FIELD_SOURCE );
		ccy1 = dataKey.getValue( MarketDataConstants.FIELD_CCY1 );
		ccy2 = dataKey.getValue( MarketDataConstants.FIELD_CCY2 );
		period = dataKey.getValue( MarketDataConstants.FIELD_PERIOD );
	}
	@Override
	protected void start()
	{
		if( source == null || ccy1 == null || ccy2 == null || period == null )
		{
			stm.logError( "Missing required field in MarketDataProducer" );
			return;
		}
		
		try
		{
			startSubscription();
		}catch( Exception e )
		{
			stm.logError( e.getMessage(), e );
		}
	}
	
//	public void startListener( )throws Exception
//	{
//		Hashtable<String, String> properties = new Hashtable<String, String>();
//		properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.exolab.jms.jndi.InitialContextFactory");
//		properties.put(Context.PROVIDER_URL, "tcp://localhost:3035/");
//
//
//		Context context = new InitialContext(properties);
//
//		ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");
//
//		Connection connection = factory.createConnection();
//
//		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//
//		Destination destination = (Destination) context.lookup(QPMessage.SENDER_PREFIX+source);
//		MessageConsumer receiver = session.createConsumer(destination);
//		receiver.setMessageListener(new MessageListener() {
//			public void onMessage(Message message) {
//				try
//				{
//					String textMess = ((TextMessage)message).getText();
//					final QPMessage mess = new QPMessage( textMess );
//					
//					if( mess.type.equals( QPMessage.QUOTE ) )
//					{
//						if( ccy1.equals(  mess.ccy1 ) && ccy2.equals( mess.ccy2 ) && period.equals( mess.period ))
//						{
//							stm.execute( new Runnable() {
//								public void run()
//								{
//									parseQuote( mess );
//								}
//							}, IExecutor.HIGH );
//						}
//					}
//
//				}catch (Exception e)
//				{
//					e.printStackTrace();
//				}
//			}
//		});
//
//		// start the connection to enable message delivery
//		connection.start();
//	}
	
	public void startSubscription( ) throws Exception
	{
		QPMessage subMessage = new QPMessage( QPMessage.SUBSCRIBE, ccy1, ccy2, period);
		MarketDataSource.addSubscriber( this, subMessage, source );
	}
	
	protected void stop()
	{
		super.stop();
		MarketDataSource.removeSubscriber( this, source );
	}
	
	public void parseQuote( final QPMessage inQuote )
	{
		stm.commit( new DataTransaction(dataKey, this )
		{
			@Override
			public void execute()
			{
				putValue( MarketDataConstants.FIELD_BID, new DataTypeBigDecimal( inQuote.bid ));
				putValue( MarketDataConstants.FIELD_ASK, new DataTypeBigDecimal( inQuote.ask ));
				
				Long timeStamp = System.nanoTime();
				
				putValue( MarketDataConstants.FIELD_TIMESTAMP, new DataTypeLong( timeStamp ));
				
				if( getStatus() != Status.OK )
					setStatus( Status.OK );
			}
		});
	}
	@Override
	public void marketDataUpdated(QPMessage inMessage)
	{
		parseQuote( inMessage );
	}
	
}