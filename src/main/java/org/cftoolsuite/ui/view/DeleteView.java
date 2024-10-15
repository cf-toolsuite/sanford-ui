package org.cftoolsuite.ui.view;

import org.cftoolsuite.client.SanfordClient;
import org.cftoolsuite.ui.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.PostConstruct;

@PageTitle("sanford-ui » Delete")
@Route(value = "delete", layout = MainLayout.class)
public class DeleteView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(DeleteView.class);

    private TextField fileName;
    private Button submitButton;
    private Button clearButton;
    private HorizontalLayout buttons;

    public DeleteView(SanfordClient sanfordClient) {
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
        this.fileName.setHelperText("Specify the name of the file you would like to delete.");
        this.submitButton = new Button("Submit");
        this.clearButton = new Button("Clear");
        this.buttons = new HorizontalLayout();

        buttons.add(submitButton, clearButton);

        buttons.setAlignItems(Alignment.CENTER);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);
        submitButton.addClickListener(event -> deleteRequest());
        clearButton.addClickListener(event -> clearAllFields());

        add(
            new H2("Delete a document"),
            fileName,
            buttons
        );

        autoSizeFields();
    }

    protected void deleteRequest() {
        try {
            ResponseEntity<Void> response = sanfordClient.deleteFile(fileName.getValue());
            if (response.getStatusCode().is2xxSuccessful()) {
                showNotification("Document deleted successfully", NotificationVariant.LUMO_SUCCESS);
            } else {
                String errorMessage = "Error submitting delete request. Status code: " + response.getStatusCode();
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