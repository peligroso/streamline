package org.juxtapose.fxtradingclient;

public class EnumInput implements InputContainer
{
	final String[] input;
	
	public EnumInput( String[] inPut )
	{
		input = inPut;
	}
	@Override
	public String[] getInputObjects()
	{
		return input;
	}

}
