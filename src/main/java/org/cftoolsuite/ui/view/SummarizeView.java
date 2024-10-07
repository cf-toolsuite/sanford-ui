package org.cftoolsuite.ui.view;

import org.cftoolsuite.client.SanfordClient;
import org.cftoolsuite.ui.MainLayout;
import org.cftoolsuite.ui.component.Markdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

        add(
            new H2("Summarize a document"),
            fileName,
            buttons
        );

        autoSizeFields();
    }

    protected void summarizeRequest() {
        try {
            ResponseEntity<String> response = sanfordClient.summarize(fileName.getValue());
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Dialog summaryDialog = new Dialog();
                summaryDialog.setWidth("800px");

                H3 title = new H3("Summary for " + fileName);

                Div contentWrapper = new Div();
                contentWrapper.setWidthFull();
                contentWrapper.getStyle()
                    .set("max-height", "600px")
                    .set("overflow-y", "auto");

                Markdown markdown = new Markdown(response.getBody());
                contentWrapper.add(markdown);

                Button closeButton = new Button("Close", e -> summaryDialog.close());
                closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                HorizontalLayout buttonLayout = new HorizontalLayout(closeButton);
                buttonLayout.setJustifyContentMode(JustifyContentMode.END);
                buttonLayout.setWidthFull();

                VerticalLayout layout = new VerticalLayout(title, contentWrapper, buttonLayout);
                summaryDialog.add(layout);
                summaryDialog.open();
            } else {
                showNotification("Error fetching summary", NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            log.error("Error fetching summary", e);
            showNotification("Error fetching summary: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
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