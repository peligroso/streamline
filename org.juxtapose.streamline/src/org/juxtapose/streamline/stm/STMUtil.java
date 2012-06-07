package org.juxtapose.streamline.stm;

import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.util.ISTMEntry;

/**
 * @author Pontus Jörgne
 * 28 jun 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class STMUtil {

	public static String PRODUCER_SERVICES = "PRODUCER_SERVICES";
	
	/**Used for stack validation**/
	static String COMMIT_METHOD = "commit";
	
	/**
	 * @param inData
	 * @param inTransaction
	 * @return
	 */
	public static boolean validateProducerToData( ISTMEntry inData, STMTransaction inTransaction )
	{
		ISTMEntryProducer dataProd = inData.getProducer();
		if( dataProd != null )
		{
			ISTMEntryProducer transactionProducer = inTransaction.producedBy();
			if( transactionProducer != null && transactionProducer == dataProd )
				return true;
			else
				return false;
		}
		return true;
	}
	
	/**
	 * @return
	 */
	public static boolean validateCommitStack()
	{
		//Must not contain a commit method
		return validateCommitMethodStack( false );
	}
	
	/**
	 * @return
	 */
	public static boolean validateTransactionStack()
	{
		//Needs to be called from commit method of STM
		return validateCommitMethodStack( true );
	}
	
	/**
	 * @param inInclusive
	 * @return
	 */
	public static boolean validateCommitMethodStack( boolean inInclusive )
	{
		StackTraceElement stEl[] = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : stEl )
		{
			if( isSTMClass( element.getClassName() ) && element.getMethodName().equals( COMMIT_METHOD ) )
			{
				return inInclusive;
			}
		}
		
		return !inInclusive;
	}
	
	public static boolean validateStackMethodCall( String inClassName, String inMethodName, boolean inInclusive )
	{
		StackTraceElement stEl[] = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : stEl )
		{
			if( element.getClassName().equals( inClassName ) && element.getMethodName().equals( inMethodName ) )
			{
				return inInclusive;
			}
		}
		
		return !inInclusive;
	}
	
	public static boolean validateStackMethodCall( String inClassName, String inMethodName, boolean inInclusive, int inIndex )
	{
		StackTraceElement stEl[] = Thread.currentThread().getStackTrace();
		StackTraceElement element = stEl[inIndex];
		
		if( element.getClassName().equals( inClassName ) && element.getMethodName().equals( inMethodName ) )
		{
			return inInclusive;
		}
		return !inInclusive;
	}
	
	/**
	 * @param inClassName
	 * @return
	 */
	public static boolean isSTMClass( String inClassName )
	{
		return inClassName.contains("STM");
		
//		Why does getClass().getName() return "java.lang.Class" ??
//		return STM.class.getClass().getName().equals( inClassName ) ||
//		BlockingSTM.class.getClass().getName().equals( inClassName ) ||
//		NonBlockingSTM.class.getClass().getName().equals( inClassName );
	}
	
}
