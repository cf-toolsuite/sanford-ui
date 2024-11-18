package org.cftoolsuite.ui.view;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.cftoolsuite.client.SanfordClient;
import org.cftoolsuite.domain.crawl.CrawlRequest;
import org.cftoolsuite.domain.crawl.CrawlResponse;
import org.cftoolsuite.ui.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.PostConstruct;

@PageTitle("sanford-ui » Crawl")
@Route(value = "crawl", layout = MainLayout.class)
public class CrawlView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(CrawlView.class);

    private TextField rootDomain;
    private TextArea seeds;
    private Button crawlButton;
    private Button clearButton;
    private HorizontalLayout buttons;

    public CrawlView(SanfordClient sanfordClient) {
        super(sanfordClient);
    }

    @PostConstruct
    public void init() {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(getLogoImage());
        setupUI();
    }

    @Override
    protected void setupUI() {
        this.rootDomain = new TextField("Root domain");
        this.rootDomain.setRequired(true);
        this.rootDomain.setHelperText("The root domain of the website you want to crawl which may also include sub-paths.");

        this.seeds = new TextArea("Seeds");
        this.seeds.setRequired(true);
        this.seeds.setHelperText("A comma-separated list of seeds from which to execute crawling from. Each seed should be an additional sub-path from the root domain. Links found within each file found will be crawled so long as they match filter. The crawling algorithm is also constrained to a maximum depth of 5.");

        this.crawlButton = new Button("Crawl");
        this.clearButton = new Button("Clear");
        this.buttons = new HorizontalLayout();

        buttons.add(crawlButton, clearButton);

        buttons.setAlignItems(Alignment.CENTER);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);
        crawlButton.addClickListener(event -> crawlRequest());
        clearButton.addClickListener(event -> clearAllFields());

        add(
            new H2("Crawl a website"),
            rootDomain,
            seeds,
            buttons
        );

        autoSizeFields();
    }

    protected void crawlRequest() {
        try {
            CrawlRequest request = new CrawlRequest(
                rootDomain.getValue(),
                convertToArray(seeds.getValue()),
                null,
                null,
                null
            );

            ResponseEntity<CrawlResponse> response = sanfordClient.startCrawl(request);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                showNotification("Completed crawling website", NotificationVariant.LUMO_SUCCESS);
            } else {
                showNotification("Error crawling website", NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            log.error("Error crawling website", e);
            showNotification("Error crawling website: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    protected String[] convertToArray(String commaSeparatedString) {
        return Arrays.stream(commaSeparatedString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet())
                .toArray(new String[0]);
    }

    @Override
    protected void clearAllFields() {
        rootDomain.clear();
        seeds.clear();
    }

    private void autoSizeFields() {
        rootDomain.setWidth("480px");
        seeds.setWidth("480px");
    }
}