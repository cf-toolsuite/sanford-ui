package org.cftoolsuite.ui.view;

import java.util.List;

import org.cftoolsuite.client.SanfordClient;
import org.cftoolsuite.domain.FileMetadata;
import org.cftoolsuite.ui.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.PostConstruct;

@PageTitle("sanford-ui » Search")
@Route(value = "search", layout = MainLayout.class)
public class SearchView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(UploadView.class);

    private TextField query;
    private Button submitButton;
    private Button clearButton;
    private HorizontalLayout buttons;
    private Grid<FileMetadata> grid;

    public SearchView(SanfordClient sanfordClient) {
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
        grid.addColumn(FileMetadata::objectId).setHeader("Object ID").setSortable(true);
        grid.addColumn(FileMetadata::fileName).setHeader("File Name").setSortable(true);
        grid.addColumn(FileMetadata::fileExtension).setHeader("File Extension").setSortable(true);
        grid.addColumn(FileMetadata::contentType).setHeader("Content Type").setSortable(true);

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
                    grid.getDataProvider().refreshAll();
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

    private void populateGrid(List<FileMetadata> fileMetadataList) {
        grid.setItems(fileMetadataList);
    }

    @Override
    protected void clearAllFields() {
        query.clear();
        grid.getDataProvider().refreshAll();
    }

    private void autoSizeFields() {
        query.setWidth("240px");
        grid.setWidthFull();
    }
}