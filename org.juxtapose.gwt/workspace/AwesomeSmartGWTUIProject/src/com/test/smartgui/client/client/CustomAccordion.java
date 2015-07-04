package com.test.smartgui.client.client;

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;

public class CustomAccordion extends SectionStack {

	public CustomAccordion() {

		this.setWidth100();
		this.setVisibilityMode(VisibilityMode.MUTEX);
		this.setShowExpandControls(false);
		this.setAnimateSections(true);

		SectionStackSection section1 = new SectionStackSection("TabSection1");
		section1.setExpanded(true);
		HTMLFlow htmlFlow1 = new HTMLFlow(); 
		htmlFlow1.setOverflow(Overflow.AUTO); 
		htmlFlow1.setPadding(10); 
		htmlFlow1.setContents("TabSection1");    
		section1.addItem(htmlFlow1);

		SectionStackSection section2 = new SectionStackSection("TabSection2"); 
		section2.setExpanded(false); 
		HTMLFlow htmlFlow2 = new HTMLFlow(); 
		htmlFlow2.setOverflow(Overflow.AUTO); 
		htmlFlow2.setPadding(10); 
		htmlFlow2.setContents("TabSection2"); 
		section2.addItem(htmlFlow2);

		SectionStackSection section3 = new SectionStackSection("TabSection3"); 
		section3.setExpanded(false);
		HTMLFlow htmlFlow3 = new HTMLFlow(); 
		htmlFlow3.setOverflow(Overflow.AUTO); 
		htmlFlow3.setPadding(10); 
		htmlFlow3.setContents("TabSection3");   
		section3.addItem(htmlFlow3);
		this.addSection(section1); 
		this.addSection(section2); 
		this.addSection(section3);

	}
}