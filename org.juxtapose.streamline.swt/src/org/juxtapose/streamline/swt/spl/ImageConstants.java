package org.juxtapose.streamline.swt.spl;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class ImageConstants
{
	public static final String ICON_PATH = "/icons/";
	
	public static final String WARNING = "warning";
	public static final String OK = "accept";
	public static final String EDITED = "livejournal";
	public static final String DELETE = "delete";
	
	private static ImageRegistry imageRegistry = new ImageRegistry();
	
	static {
		
		ImageDescriptor id1 = ImageDescriptor.createFromFile(ImageConstants.class, ICON_PATH + WARNING+".png");
		imageRegistry.put(WARNING, id1);
		
		ImageDescriptor id2 = ImageDescriptor.createFromFile(ImageConstants.class, ICON_PATH + OK+".png");
		imageRegistry.put(OK, id2);
		
		ImageDescriptor id3 = ImageDescriptor.createFromFile(ImageConstants.class, ICON_PATH + EDITED+".png");
		imageRegistry.put(EDITED, id3);
		
		ImageDescriptor id4 = ImageDescriptor.createFromFile(ImageConstants.class, ICON_PATH + DELETE+".png");
		imageRegistry.put(DELETE, id4);
	}
	
	public static Image getImage( String inKey )
	{
		return imageRegistry.get(inKey);
	}
}
