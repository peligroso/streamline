package com.smartgwt.sample.showcase.client.menu;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.menu.*;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import com.smartgwt.sample.showcase.client.PanelFactory;
import com.smartgwt.sample.showcase.client.ShowcasePanel;

public class MenuAppearanceSample extends ShowcasePanel {
    private static final String DESCRIPTION = "Click \"File\" to see a typical File menu with icons, submenus, checks, separators, disabled items, " +
            "and keyboard shortcut hints. Note the beveled edge and drop shadow.";

    public static class Factory implements PanelFactory {
        private String id;

        public Canvas create() {
            MenuAppearanceSample panel = new MenuAppearanceSample();
            id = panel.getID();
            return panel;
        }

        public String getID() {
            return id;
        }

        public String getDescription() {
            return DESCRIPTION;
        }
    }

    public Canvas getViewPanel() {
        Menu menu = new Menu();
        menu.setShowShadow(true);
        menu.setShadowDepth(10);

        MenuItem newItem = new MenuItem("New", "icons/16/document_plain_new.png", "Ctrl+N");
        MenuItem openItem = new MenuItem("Open", "icons/16/folder_out.png", "Ctrl+O");
        MenuItem saveItem = new MenuItem("Save", "icons/16/disk_blue.png", "Ctrl+S");
        MenuItem saveAsItem = new MenuItem("Save As", "icons/16/save_as.png");

        MenuItem recentDocItem = new MenuItem("Recent Documents", "icons/16/folder_document.png");

        Menu recentDocSubMenu = new Menu();
        MenuItem dataSM = new MenuItem("data.xml");
        dataSM.setChecked(true);
        MenuItem componentSM = new MenuItem("Component Guide.doc");
        MenuItem ajaxSM = new MenuItem("AJAX.doc");
        recentDocSubMenu.setItems(dataSM, componentSM, ajaxSM);

        recentDocItem.setSubmenu(recentDocSubMenu);

        MenuItem exportItem = new MenuItem("Export as...", "icons/16/export1.png");
        Menu exportSM = new Menu();
        exportSM.setItems(
                new MenuItem("XML"),
                new MenuItem("CSV"),
                new MenuItem("Plain text"));
        exportItem.setSubmenu(exportSM);

        MenuItem printItem = new MenuItem("Print", "icons/16/printer3.png", "Ctrl+P");
        printItem.setEnabled(false);

        MenuItemSeparator separator = new MenuItemSeparator();

        final MenuItem activateMenu = new MenuItem("Activate");
        activateMenu.setDynamicTitleFunction(new MenuItemStringFunction() {
            public String execute(final Canvas aTarget, final Menu aMenu, final MenuItem aItem) {
                if (Math.random() > 0.5) {
                    return "De-Activate Blacklist";
                } else {
                    return "Activate Blacklist";
                }
            }
        });
        menu.setItems(activateMenu, newItem, openItem, separator, saveItem, saveAsItem,
                separator, recentDocItem, separator, exportItem, separator, printItem);

        IMenuButton menuButton = new IMenuButton("File", menu);
        menuButton.setWidth(100);
        return menuButton;
    }

    public String getIntro() {
        return DESCRIPTION;
    }
}