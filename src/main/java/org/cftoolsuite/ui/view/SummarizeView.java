package org.cftoolsuite.ui.view;

import org.cftoolsuite.client.SanfordClient;
import org.cftoolsuite.service.SummaryService;
import org.cftoolsuite.ui.MainLayout;
import org.cftoolsuite.ui.component.Markdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.PostConstruct;
import reactor.core.Disposable;

@PageTitle("sanford-ui » Summarize")
@Route(value = "summarize", layout = MainLayout.class)
public class SummarizeView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(UploadView.class);

    private TextField fileName;
    private Button submitButton;
    private Button clearButton;
    private HorizontalLayout buttons;
    private SummaryService summaryService;

    public SummarizeView(SanfordClient sanfordClient, SummaryService summaryService) {
        super(sanfordClient);
        this.summaryService = summaryService;
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
        Div contentWrapper = new Div();
        contentWrapper.setWidthFull();
        contentWrapper.getStyle()
            .set("max-height", "600px")
            .set("overflow-y", "auto");

        Markdown markdown = new Markdown();
        contentWrapper.add(markdown);

        Dialog summaryDialog = new Dialog();

        Button closeButton = new Button("Close", e -> summaryDialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(closeButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonLayout.setWidthFull();

        VerticalLayout layout = new VerticalLayout(contentWrapper, buttonLayout);

        summaryDialog.setWidth("800px");
        summaryDialog.add(layout);

        summaryDialog.addAttachListener(attachEvent -> {
            UI ui = attachEvent.getUI();

            Disposable subscription = summaryService.getSummary(fileName.getValue())
                .subscribe(
                    chunk -> ui.access(() -> {
                        markdown.setSource(chunk);
                    }),
                    error -> ui.access(() -> {
                        log.error("Error fetching summary", error);
                        showNotification("Error fetching summary: " + error.getMessage(), NotificationVariant.LUMO_ERROR);
                    }),
                    () -> ui.access(() -> {
                        showNotification("Summary completed", NotificationVariant.LUMO_SUCCESS);
                    })
                );

            summaryDialog.addDetachListener(detachEvent -> subscription.dispose());
        });

        summaryDialog.open();
    }

    @Override
    protected void clearAllFields() {
        fileName.clear();
    }

    private void autoSizeFields() {
        fileName.setWidth("240px");
    }
}