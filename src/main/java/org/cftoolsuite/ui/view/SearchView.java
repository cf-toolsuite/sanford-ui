package org.cftoolsuite.ui.view;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.cftoolsuite.client.SanfordClient;
import org.cftoolsuite.domain.FileMetadata;
import org.cftoolsuite.service.SummaryService;
import org.cftoolsuite.ui.MainLayout;
import org.cftoolsuite.ui.component.Markdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.vaadin.olli.FileDownloadWrapper;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import jakarta.annotation.PostConstruct;
import reactor.core.Disposable;

@PageTitle("sanford-ui » Search")
@Route(value = "search", layout = MainLayout.class)
public class SearchView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(UploadView.class);

    private TextField query;
    private Button submitButton;
    private Button clearButton;
    private HorizontalLayout buttons;
    private Grid<FileMetadata> grid;
    private SummaryService summaryService;

    public SearchView(SanfordClient sanfordClient, SummaryService summaryService) {
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
        this.query = new TextField("Query");
        this.query.setRequired(true);
        this.query.setHelperText("Ask a question. A similarity search will be conducted to find documents whose contents may contain information of interest.  Only document metadata will be returned.");
        this.submitButton = new Button("Submit");
        this.clearButton = new Button("Clear");
        this.buttons = new HorizontalLayout();

        buttons.add(submitButton, clearButton);

        buttons.setAlignItems(Alignment.CENTER);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);
        submitButton.addClickListener(event -> searchRequest());
        clearButton.addClickListener(event -> clearAllFields());

        grid = new Grid<>(FileMetadata.class, false);

        grid.setPageSize(10);
        grid.setHeight("480px");

        grid.addColumn(FileMetadata::objectId).setHeader("Object ID").setSortable(true);
        grid.addColumn(FileMetadata::fileName).setHeader("File Name").setSortable(true);
        grid.addColumn(FileMetadata::fileExtension).setHeader("File Extension").setSortable(true);
        grid.addColumn(FileMetadata::contentType).setHeader("Content Type").setSortable(true);

        grid.addComponentColumn(fileMetadata -> {
            Button downloadButton = new Button(new Icon(VaadinIcon.DOWNLOAD));
            String fileName = fileMetadata.fileName();
            FileDownloadWrapper wrapper = new FileDownloadWrapper(
                new StreamResource(fileName, () -> getFileContent(fileName))
            );
            wrapper.wrapComponent(downloadButton);
            wrapper.setFileName(fileName);
            return wrapper;
        }).setHeader("Download").setWidth("100px").setFlexGrow(0);

        grid.addComponentColumn(fileMetadata -> {
            Button summaryButton = new Button(new Icon(VaadinIcon.BULLETS));
            summaryButton.addClickListener(e -> showSummary(fileMetadata.fileName()));
            return summaryButton;
        }).setHeader("Summary").setWidth("120px").setFlexGrow(0);


        grid.addComponentColumn(fileMetadata -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addClickListener(e -> deleteFile(fileMetadata.fileName()));
            return deleteButton;
        }).setHeader("Delete").setWidth("100px").setFlexGrow(0);

        add(
            new H2("Search for document metadata"),
            query,
            buttons,
            grid
        );

        autoSizeFields();
    }

    protected void searchRequest() {
        try {
            ResponseEntity<List<FileMetadata>> response = sanfordClient.search(query.getValue());
            if (response.getStatusCode().is2xxSuccessful()) {
                List<FileMetadata> fileMetadataList = response.getBody();
                if (fileMetadataList != null && !fileMetadataList.isEmpty()) {
                    populateGrid(fileMetadataList);
                    showNotification("File metadata retrieved successfully", NotificationVariant.LUMO_SUCCESS);
                } else {
                    populateGrid(List.of());
                    showNotification("No file metadata found matching your query", NotificationVariant.LUMO_CONTRAST);
                }
            } else {
                String errorMessage = "Error submitting search request. Status code: " + response.getStatusCode();
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

    private InputStream getFileContent(String fileName) {
        try {
            ResponseEntity<Resource> response = sanfordClient.downloadFile(fileName);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getInputStream();
            } else {
                String errorMessage = "Error downloading file. Status code: " + response.getStatusCode();
                showNotification(errorMessage, NotificationVariant.LUMO_ERROR);
                return new ByteArrayInputStream(new byte[0]);
            }
        } catch (Exception e) {
            String errorMessage = "An unexpected error occurred: " + e.getMessage();
            showNotification(errorMessage, NotificationVariant.LUMO_ERROR);
            log.error("An unexpected error occurred", e);
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    protected void showSummary(String fileName) {
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

            Disposable subscription = summaryService.getSummary(fileName)
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

    protected void deleteFile(String fileName) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirm deletion");
        dialog.setText("Are you sure you want to delete " + fileName + "?");

        dialog.setCancelable(true);
        dialog.addCancelListener(event -> dialog.close());

        dialog.setConfirmText("Delete");
        dialog.addConfirmListener(event -> {
            try {
                ResponseEntity<Void> response = sanfordClient.deleteFile(fileName);
                if (response.getStatusCode().is2xxSuccessful()) {
                    showNotification("File deleted successfully", NotificationVariant.LUMO_SUCCESS);
                    clearAllFields();
                } else {
                    showNotification("Error deleting file", NotificationVariant.LUMO_ERROR);
                }
            } catch (Exception e) {
                log.error("Error deleting file", e);
                showNotification("Error deleting file: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }

    private void populateGrid(List<FileMetadata> items) {
        ListDataProvider<FileMetadata> dataProvider = new ListDataProvider<>(items);
        grid.setItems(dataProvider);
    }

    @Override
    protected void clearAllFields() {
        query.clear();
        populateGrid(List.of());
    }

    private void autoSizeFields() {
        query.setWidth("240px");
        grid.setWidthFull();
    }
}