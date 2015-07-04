package com.test.smartgui.client.client;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HLayout;

public class ApplicationMenu extends HLayout {

	private static final int APPLICATION_MENU_HEIGHT = 27;

	private Label label;

	public ApplicationMenu() {

		super();
		this.setHeight(APPLICATION_MENU_HEIGHT);

		label = new Label();
		label.setContents("Application Menu");
		label.setAlign(Alignment.CENTER);
		label.setOverflow(Overflow.HIDDEN);

		this.addMember(label);

	}

}
