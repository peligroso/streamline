package com.test.smartgui.client.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class AwesomeSmartGWTUIProject implements EntryPoint {
	
	private static final int HEADER_HEIGHT = 85;
	     
	private VLayout mainLayout;
	private HLayout northLayout;
	private HLayout southLayout;
	private VLayout eastLayout;
	private HLayout westLayout;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() 
	{
		Window.enableScrolling(false);
		Window.setMargin("0px");
		         
		// main layout occupies the whole area
		mainLayout = new VLayout();
		mainLayout.setWidth100();
		mainLayout.setHeight100();
			 
		northLayout = new HLayout();
		northLayout.setHeight(HEADER_HEIGHT);
			 
		VLayout vLayout = new VLayout();
		vLayout.addMember(new HeaderArea());
		vLayout.addMember(new ApplicationMenu());
		northLayout.addMember(vLayout);
			 
		westLayout = new NavigationArea();
		westLayout.setWidth("15%");
			         
		eastLayout = new MainArea();
		eastLayout.setWidth("85%");
			         
		southLayout = new HLayout();
		southLayout.setMembers(westLayout, eastLayout);
		 
		mainLayout.addMember(northLayout);
		mainLayout.addMember(southLayout);
			 
		 // add the main layout container to GWT's root panel
		RootLayoutPanel.get().add(mainLayout);
	}
}
