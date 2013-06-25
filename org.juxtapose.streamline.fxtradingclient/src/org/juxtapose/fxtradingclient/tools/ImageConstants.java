package org.juxtapose.fxtradingclient.tools;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class ImageConstants
{
	public static final String ICON_PATH = "/icons/";
	public static final String TEST = "TEST";
	
	private static ImageRegistry imageRegistry = new ImageRegistry();
	
	static {
		
		ImageDescriptor id1 = ImageDescriptor.createFromFile(ImageConstants.class, ICON_PATH + "alt_window_16.gif");
		imageRegistry.put(TEST, id1);
	}
	
	public static Image getImage( String inKey )
	{
		return imageRegistry.get(inKey);
	}
}
