package org.cftoolsuite.ui.view;

import org.apache.commons.lang3.StringUtils;
import org.cftoolsuite.client.SanfordClient;
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

@PageTitle("sanford-ui » Summarize")
@Route(value = "summarize", layout = MainLayout.class)
public class SummarizeView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(UploadView.class);

    private TextField fileName;
    private Button submitButton;
    private Button clearButton;
    private HorizontalLayout buttons;
    private TextArea summary;

    public SummarizeView(SanfordClient sanfordClient) {
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
        this.fileName = new TextField("File Name");
        this.fileName.setRequired(true);
        this.fileName.setHelperText("Think of me like your Cliff Notes document research assistant.  Enter the name of a file you would like to get a brief summary on.");
        this.submitButton = new Button("Submit");
        this.clearButton = new Button("Clear");
        this.buttons = new HorizontalLayout();

        buttons.add(submitButton, clearButton);

        buttons.setAlignItems(Alignment.CENTER);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);
        submitButton.addClickListener(event -> summarizeRequest());
        clearButton.addClickListener(event -> clearAllFields());

        summary = new TextArea();
        summary.setLabel("Summary");
        summary.setReadOnly(true);

        add(
            new H2("Summarize a document"),
            fileName,
            buttons,
            summary
        );

        autoSizeFields();
    }

    protected void summarizeRequest() {
        try {
            ResponseEntity<String> response = sanfordClient.summarize(fileName.getValue());
            if (response.getStatusCode().is2xxSuccessful()) {
                String result = response.getBody();
                if (StringUtils.isNotBlank(result)) {
                    populateTextArea(result);
                    showNotification("Document summary retrieved successfully", NotificationVariant.LUMO_SUCCESS);
                } else {
                    summary.clear();
                    showNotification("No document summary available", NotificationVariant.LUMO_CONTRAST);
                }
            } else {
                String errorMessage = "Error submitting list request. Status code: " + response.getStatusCode();
                if (response.getBody() != null) {
                    errorMessage += ". Message: " + response.getBody().toString();
                }
                showNotification(errorMessage, NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            String errorMessage = "An unexpected error occurred: " + e.getMessage();
            showNotification(errorMessage, NotificationVariant.LUMO_ERROR);
            log.error("An unexpected error occurred", e);
        }
    }

    private void populateTextArea(String text) {
        summary.setValue(text);
    }

    @Override
    protected void clearAllFields() {
        fileName.clear();
        summary.clear();
    }

    private void autoSizeFields() {
        fileName.setWidth("240px");
        summary.setWidth("480px");
    }
}