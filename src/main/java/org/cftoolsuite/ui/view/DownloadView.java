package org.cftoolsuite.ui.view;

import org.cftoolsuite.client.SanfordClient;
import org.cftoolsuite.ui.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.PostConstruct;

@PageTitle("sanford-ui » Download")
@Route(value = "download", layout = MainLayout.class)
public class DownloadView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(UploadView.class);

    private TextField fileName;
    private Button submitButton;
    private Button clearButton;
    private HorizontalLayout buttons;

    public DownloadView(SanfordClient sanfordClient) {
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
        this.fileName = new TextField("File name");
        this.fileName.setRequired(true);
        this.fileName.setHelperText("Specify the name of the file you would like to download.");
        this.submitButton = new Button("Submit");
        this.clearButton = new Button("Clear");
        this.buttons = new HorizontalLayout();

        buttons.add(submitButton, clearButton);

        buttons.setAlignItems(Alignment.CENTER);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);
        submitButton.addClickListener(event -> downloadRequest());
        clearButton.addClickListener(event -> clearAllFields());

        add(
            new H2("Download a document"),
            fileName,
            buttons
        );

        autoSizeFields();
    }

    protected void downloadRequest() {
        try {
            ResponseEntity<Resource> response = sanfordClient.downloadFile(fileName.getValue());
            if (response.getStatusCode().is2xxSuccessful()) {
                showNotification("Document downloaded successfully", NotificationVariant.LUMO_SUCCESS);
            } else {
                String errorMessage = "Error submitting download request. Status code: " + response.getStatusCode();
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

    @Override
    protected void clearAllFields() {
        fileName.clear();
    }

    private void autoSizeFields() {
        fileName.setWidth("240px");
    }
}