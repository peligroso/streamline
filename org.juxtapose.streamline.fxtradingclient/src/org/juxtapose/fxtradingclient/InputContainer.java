package org.juxtapose.fxtradingclient;

import org.juxtapose.streamline.util.IInputListener;

public interface InputContainer
{
	String[] getInputObjects();
	
	public void addInputListener( IInputListener inInputListener );
	public void removeContainerListener( IInputListener inputListener );
	
}
