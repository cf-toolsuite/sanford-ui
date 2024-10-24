package org.cftoolsuite.ui;

import org.cftoolsuite.ui.view.ChatView;
import org.cftoolsuite.ui.view.CrawlView;
import org.cftoolsuite.ui.view.DeleteView;
import org.cftoolsuite.ui.view.DownloadView;
import org.cftoolsuite.ui.view.HomeView;
import org.cftoolsuite.ui.view.ListView;
import org.cftoolsuite.ui.view.SearchView;
import org.cftoolsuite.ui.view.SummarizeView;
import org.cftoolsuite.ui.view.UploadView;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;


public class MainLayout extends AppLayout {

    private static final long serialVersionUID = 1L;

    public MainLayout() {
    	Tab homeTab = createTab(VaadinIcon.HOME.create(), "Home", HomeView.class);

    	Accordion accordion = new Accordion();
		accordion.setSizeFull();

    	Tabs actionTabs = createTabs();

    	Tab uploadTab = createTab(VaadinIcon.UPLOAD.create(), "Upload documents", UploadView.class);
		Tab crawlTab = createTab(VaadinIcon.SITEMAP.create(), "Crawl websites for documents", CrawlView.class);
		Tab chatTab = createTab(VaadinIcon.CHAT.create(), "Chat with AI bot about documents", ChatView.class);
		Tab listTab = createTab(VaadinIcon.LIST.create(), "List document metadata", ListView.class);
		Tab searchTab = createTab(VaadinIcon.SEARCH.create(), "Search for document metadata", SearchView.class);
    	Tab summaryTab = createTab(VaadinIcon.BULLETS.create(), "Summarize a document", SummarizeView.class);
		Tab downloadTab = createTab(VaadinIcon.DOWNLOAD.create(), "Download a document", DownloadView.class);
		Tab deleteTab = createTab(VaadinIcon.TRASH.create(), "Delete a document", DeleteView.class);
		actionTabs.add(uploadTab, crawlTab, chatTab, listTab, searchTab, summaryTab, downloadTab, deleteTab);
    	accordion.add("Actions", actionTabs).addThemeVariants(DetailsVariant.REVERSE);

    	addToNavbar(true, homeTab, new DrawerToggle());
    	addToDrawer(accordion);
    }

    private Tabs createTabs() {
    	Tabs menu = new Tabs();
    	menu.setWidthFull();
    	menu.setOrientation(Tabs.Orientation.VERTICAL);
    	menu.setFlexGrowForEnclosedTabs(1);
    	return menu;
    }

    private Tab createTab(Icon icon, String label, Class<? extends Component> layout) {
		RouterLink link = new RouterLink();
    	link.setRoute(layout);
		Div container = new Div();
		container.getStyle().set("display", "flex");
		container.getStyle().set("justify-content", "flex-start");
		container.getStyle().set("align-items", "center");
		container.getStyle().set("width", "100%");
		icon.getStyle().set("margin-right", "8px");
		container.add(icon, new Span(label));
		link.add(container);
		Tab tab = new Tab();
		tab.add(link);
		return tab;
    }

}
