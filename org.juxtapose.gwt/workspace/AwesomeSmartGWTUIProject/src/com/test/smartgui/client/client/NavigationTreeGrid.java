package com.test.smartgui.client.client;

import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;
import com.smartgwt.client.widgets.tree.events.NodeClickEvent;
import com.smartgwt.client.widgets.tree.events.NodeClickHandler;

public class NavigationTreeGrid extends TreeGrid {

	public NavigationTreeGrid() {

		setNodeIcon("arrow_down.png"); 
		setFolderIcon("arrow_up.png"); 
		setShowOpenIcons(false);
		setShowDropIcons(false);
		setShowSelectedStyle(true); 
		setShowPartialSelection(true); 
		setCascadeSelection(false);
		setCanSort(false);
		setShowConnectors(true);
		setShowHeader(false);
		setLoadDataOnDemand(false);
		setSelectionType(SelectionStyle.SINGLE);

		Tree data = new Tree();
		data.setModelType(TreeModelType.CHILDREN);

		data.setRoot(
				new TreeNode("root",
						new TreeNode("File",
								new TreeNode("FileChild")),
								new TreeNode("Edit",
										new TreeNode("EditChild",
												new TreeNode("EditGrandChild"))),
												new TreeNode("Window"))
				);

		setData(data);

		addNodeClickHandler(new NodeClickHandler() {           
			@Override
			public void onNodeClick(NodeClickEvent event) {
				String name = event.getNode().getName();
				SC.say("Node Clicked: " + name);
			}
		});

	}

}
