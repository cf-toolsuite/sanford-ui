package org.cftoolsuite.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import org.cftoolsuite.client.ProfilesClient;
import org.cftoolsuite.ui.view.*;


public class MainLayout extends AppLayout {

    private static final long serialVersionUID = 1L;

    public MainLayout(ProfilesClient modeClient) {
    	Tab homeTab = createTab(VaadinIcon.HOME.create(), "Home", HomeView.class);

    	Tabs actionTabs = createTabs();

    	Tab uploadTab = createTab(VaadinIcon.UPLOAD.create(), "Upload documents", UploadView.class);
		Tab crawlTab = createTab(VaadinIcon.SITEMAP.create(), "Crawl websites for documents", CrawlView.class);
		Tab fetchTab = createTab(VaadinIcon.CROSSHAIRS.create(), "Fetch documents", FetchView.class);
		Tab converseTab = createTab(VaadinIcon.MEGAPHONE.create(), "Converse with AI bot about documents", ConverseView.class);
		Tab chatTab = createTab(VaadinIcon.CHAT.create(), "Chat with AI bot about documents", ChatView.class);
		Tab listTab = createTab(VaadinIcon.LIST.create(), "List document metadata", ListView.class);
		Tab searchTab = createTab(VaadinIcon.SEARCH.create(), "Search for document metadata", SearchView.class);
    	Tab summaryTab = createTab(VaadinIcon.BULLETS.create(), "Summarize a document", SummarizeView.class);
		Tab downloadTab = createTab(VaadinIcon.DOWNLOAD.create(), "Download a document", DownloadView.class);
		Tab deleteTab = createTab(VaadinIcon.TRASH.create(), "Delete a document", DeleteView.class);
		actionTabs.add(uploadTab, crawlTab, fetchTab);
		if (modeClient.contains("openai")) {
			actionTabs.add(converseTab);
		}
		actionTabs.add(chatTab, listTab, searchTab, summaryTab, downloadTab, deleteTab);
    	addToNavbar(true, homeTab, new DrawerToggle());
    	addToDrawer(getLogoImage(), actionTabs);
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

	private Image getLogoImage() {
		StreamResource imageResource = new StreamResource("sanford.png",
				() -> getClass().getResourceAsStream("/static/sanford.png"));
		Image logo = new Image(imageResource, "Logo");
		logo.setWidth("240px");
		return logo;
	}

}
