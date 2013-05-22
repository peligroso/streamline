package org.juxtapose.streamline.tools;


/**
 * @author Pontus
 *
 */
public final class Preconditions
{
	private Preconditions(){}
	
	public static <T> T notNull(T inReference)
	{
		if(inReference == null)
			throwException(new NullPointerException());
		
		return inReference;
	}
	
	public static <T> T notNull(T inReference, Object inMessage)
	{
		if(inReference == null)
			throwException(new NullPointerException(String.valueOf(inMessage)));
		
		return inReference;
	}

	public static <T> T notNull(T inReference, String inMessageTemplate, Object... inMessages)
	{
		if(inReference == null)
			throwException(new NullPointerException(message(inMessageTemplate,inMessages)));
		
		return inReference;
	}
	

	/**
	 * Ensures that an object that is about to be set is null
	 * 
	 * @param inReference an <T> object reference
	 * @return a non-null reference
	 */
	public static <T> T isNull(T inReference)
	{
		if(inReference != null)
			throwException(new IllegalArgumentException());
		
		return inReference;
	}
	
	public static <T> T isNull(T inReference, Object inMessage)
	{
		if(inReference != null)
			throwException(new IllegalArgumentException(String.valueOf(inMessage)));
		
		return inReference;
	}

	public static <T> T isNull(T inReference, String inMessageTemplate, Object... inMessages)
	{
		if(inReference != null)
			throwException(new IllegalArgumentException(message(inMessageTemplate,inMessages)));
		
		return inReference;
	}

	
	
	/**
	 * A short hand way of checking that a {@link String} isn't null or empty
	 *  
	 * @param inString the {@link String} to check
	 * @param inMessage the message to display
	 * @return the argument {@link String}
	 */
	public static String notNullOrEmpty(String inString)
	{
		String string = notNull(inString);
		checkThat(!string.isEmpty());
		return string;
	}
	
	public static String notNullOrEmpty(String inString, Object inMessage)
	{
		String string = notNull(inString,inMessage);
		checkThat(!string.isEmpty(), inMessage);
		return string;
	}
	
	public static String notNullOrEmpty(String inString, String inMessageTemplate,Object... inMessages)
	{
		String string = notNull(inString,inMessageTemplate,inMessages);
		checkThat(!string.isEmpty(), inMessageTemplate,inMessages);
		return string;
	}
	
	public static void checkThat(boolean inExpression)
	{
		if(!inExpression)
			throwException(new IllegalArgumentException());
	}
	
	public static void checkThat(boolean inExpression, Object inMessage)
	{
		if(!inExpression)
			throwException(new IllegalArgumentException(String.valueOf(inMessage)));
	}
	
	public static void checkThat(boolean inExpression, String inMessageTemplate, Object... inMessages)
	{
		if(!inExpression)
			throwException(new IllegalArgumentException(message(inMessageTemplate, inMessages)));
	}
	
	public static void checkRange(int inFromIndex, int inToIndex, int inLength)
	{
		if (inFromIndex > inToIndex)
			throwException(new IllegalArgumentException("fromIndex(" + inFromIndex + ") > toIndex(" + inToIndex + ")"));
		
		if (inFromIndex < 0)
			throwException(new ArrayIndexOutOfBoundsException(inFromIndex));
		
		if (inToIndex > inLength)
			throwException(new ArrayIndexOutOfBoundsException(inToIndex));
	}
	
	public static boolean equalsOrBothNull( Object inObj1, Object inObj2 )
	{
		if( inObj1 == null || inObj2 == null)
			return inObj1 == null && inObj2 == null;
		
		return inObj1.equals( inObj2 );
	}
	
	/**
	 * Good place to set breakpoint
	 * @param e The exception to throw
	 */
	private static void throwException(RuntimeException e)
	{
		throw e;
	}
	
	/**
	 * Method to parse strings for variable placeholders an replace them with their values
	 * @param inTemplate template for the result message
	 * @param inMessages list of messages to use when replacing placeholders in templates
	 * @return
	 */
	private static String message(String inTemplate,Object... inMessages)
	{
		return message(new StringBuffer(),0,inTemplate,inMessages);
	}
	
	/**
	 * Tail-recursive counterpart of {@link Preconditions#message(String, Object...)}
	 * @param inBuffer StringBuffer holding the result
	 * @param inIndex the current index of the inMessages
	 * @param inTemplate the template to parse
	 * @param inMessages the var-args array of messages
	 * @return the rendered message
	 */
	private static String message(StringBuffer inBuffer,int inIndex,String inTemplate,Object... inMessages)
	{
		int index = inTemplate.indexOf("%s");
		if(index == -1 || inIndex >= inMessages.length)
			return inBuffer.append(inTemplate).toString();
		
		inBuffer.append(inTemplate.substring(0, index)).append(inMessages[inIndex]);
		return message(inBuffer,inIndex+1,inTemplate.substring(index+2),inMessages);
	}
	
	
}
