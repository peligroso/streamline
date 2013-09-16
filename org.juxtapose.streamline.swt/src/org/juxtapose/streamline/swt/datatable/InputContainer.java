package org.juxtapose.streamline.swt.datatable;


public interface InputContainer
{
	String[] getInputObjects();
	
	public void addInputListener( IInputListener inInputListener );
	public void removeContainerListener( IInputListener inputListener );
	
}
