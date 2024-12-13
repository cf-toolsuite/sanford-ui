package org.cftoolsuite.ui.view;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.cftoolsuite.client.SanfordClient;
import org.cftoolsuite.domain.AppProperties;
import org.cftoolsuite.domain.fetch.FetchRequest;
import org.cftoolsuite.domain.fetch.FetchResponse;
import org.cftoolsuite.ui.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("sanford-ui » Fetch")
@Route(value = "fetch", layout = MainLayout.class)
public class FetchView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(FetchView.class);

    private TextArea urls;
    private Button fetchButton;
    private Button clearButton;
    private HorizontalLayout buttons;

    public FetchView(SanfordClient sanfordClient, AppProperties appProperties) {
        super(sanfordClient, appProperties);
    }

    @Override
    protected void setupUI() {
        this.urls = new TextArea("URLs");
        this.urls.setRequired(true);
        this.urls.setHelperText("A comma-separated list of URLs. (The content from each URL will be ingested).");

        this.fetchButton = new Button("Fetch");
        this.clearButton = new Button("Clear");
        this.buttons = new HorizontalLayout();

        buttons.add(fetchButton, clearButton);

        buttons.setAlignItems(Alignment.CENTER);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);
        fetchButton.addClickListener(event -> fetchRequest());
        clearButton.addClickListener(event -> clearAllFields());

        add(
            new H2("Fetch content from one or more URLs"),
            urls,
            buttons
        );

        autoSizeFields();
    }

    protected void fetchRequest() {
        try {
            FetchRequest request = FetchRequest.fromCommaSeparatedList(urls.getValue());

            ResponseEntity<FetchResponse> response = sanfordClient.fetchUrls(request);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                showNotification("Completed fetching URL(s)", NotificationVariant.LUMO_SUCCESS);
            } else {
                showNotification("Error fetching URL(s)", NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            log.error("Error fetching URL(s)", e);
            showNotification("Error fetching URL(s): " + e.getMessage(), NotificationVariant.LUMO_ERROR);
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
        urls.clear();
    }

    private void autoSizeFields() {
        urls.setWidth("480px");
    }
}