package com.test.smartgui.client.client;

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.smartgwt.client.widgets.toolbar.ToolStripMenuButton;

public class MainArea extends VLayout {

	final TabSet topTabSet = new TabSet();
	
	public MainArea() 
	{
		super();

		this.setOverflow(Overflow.HIDDEN);

		topTabSet.setTabBarPosition(Side.TOP); 
		topTabSet.setTabBarAlign(Side.LEFT);

		ToolStrip toolStrip = new ToolStrip();
		toolStrip.setWidth100();

		ToolStripButton iconButton = new ToolStripButton();
		iconButton.setTitle("MyButton");
		toolStrip.addButton(iconButton);       

		MenuItem[] itemArray = new MenuItem[4];

		itemArray[0] = new MenuItem("MenuItem1");
		Menu menu1 = new Menu();
		menu1.setData(new MenuItem("SubMenuItem11"), new MenuItem("SubMenuItem12"));
		itemArray[0].setSubmenu(menu1);

		itemArray[1] = new MenuItem("MenuItem2");
		Menu menu2 = new Menu();
		menu2.setData(new MenuItem("SubMenuItem21"), new MenuItem("SubMenuItem22"));
		itemArray[1].setSubmenu(menu2);

		Menu parentMenu = new Menu(); 
		parentMenu.setCanSelectParentItems(true); 
		parentMenu.setData(itemArray);

		ToolStripMenuButton menuButton =
				new ToolStripMenuButton("Menu", parentMenu);
		toolStrip.addMenuButton(menuButton);

		VLayout hlayout = new VLayout();
		hlayout.addMember(toolStrip);
		hlayout.addMember(new HTMLFlow("Tab3"));

		addTabToTopTabset("Tab1", new HTMLFlow("Tab1"), true);
		addTabToTopTabset("Tab2", hlayout, true);       
		addTabToTopTabset("Tab3", new CustomAccordion(), true);

		this.addMember(topTabSet);

	}
	
	private void addTabToTopTabset(String title, Canvas pane, boolean closable) {
		Tab tab = createTab(title, pane, closable);
		topTabSet.addTab(tab);
		topTabSet.selectTab(tab);
	}

	private Tab createTab(String title, Canvas pane, boolean closable) {
		Tab tab = new Tab(title);
		tab.setCanClose(closable);
		tab.setPane(pane);
		return tab;
	}

}