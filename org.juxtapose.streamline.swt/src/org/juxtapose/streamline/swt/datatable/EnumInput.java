package org.juxtapose.streamline.swt.datatable;


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
	@Override
	public void addInputListener( IInputListener inInputListener )
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeContainerListener( IInputListener inputListener )
	{
		// TODO Auto-generated method stub
		
	}

}
