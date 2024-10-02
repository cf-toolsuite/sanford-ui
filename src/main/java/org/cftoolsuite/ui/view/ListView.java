package org.cftoolsuite.ui.view;

import java.util.Arrays;
import java.util.List;

import org.cftoolsuite.client.SanfordClient;
import org.cftoolsuite.domain.FileMetadata;
import org.cftoolsuite.ui.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.PostConstruct;

@PageTitle("sanford-ui » List")
@Route(value = "list", layout = MainLayout.class)
public class ListView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(UploadView.class);

    private TextField fileName;
    private Button submitButton;
    private Button clearButton;
    private HorizontalLayout buttons;
    private Grid<FileMetadata> grid;

    public ListView(SanfordClient sanfordClient) {
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
        this.fileName.setHelperText("Enter the name of a file you would like metadata details for. If left blank, all file metadata in the bucket will be returned.");
        this.submitButton = new Button("Submit");
        this.clearButton = new Button("Clear");
        this.buttons = new HorizontalLayout();

        buttons.add(submitButton, clearButton);

        buttons.setAlignItems(Alignment.CENTER);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);
        submitButton.addClickListener(event -> listRequest());
        clearButton.addClickListener(event -> clearAllFields());

        grid = new Grid<>(FileMetadata.class, false);
        grid.addColumn(FileMetadata::objectId).setHeader("Object ID").setSortable(true);
        grid.addColumn(FileMetadata::fileName).setHeader("File Name").setSortable(true);
        grid.addColumn(FileMetadata::fileExtension).setHeader("File Extension").setSortable(true);
        grid.addColumn(FileMetadata::contentType).setHeader("Content Type").setSortable(true);

        add(
            new H2("List document metadata"),
            fileName,
            buttons,
            grid
        );

        autoSizeFields();
    }

    protected void listRequest() {
        try {
            ResponseEntity<List<FileMetadata>> response = sanfordClient.getFileMetadata(fileName.getValue());
            if (response.getStatusCode().is2xxSuccessful()) {
                List<FileMetadata> fileMetadataList = response.getBody();
                if (fileMetadataList != null && !fileMetadataList.isEmpty()) {
                    populateGrid(fileMetadataList);
                    showNotification("File metadata retrieved successfully", NotificationVariant.LUMO_SUCCESS);
                } else {
                    grid.getDataProvider().refreshAll();
                    showNotification("No file metadata found", NotificationVariant.LUMO_CONTRAST);
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

    private void populateGrid(List<FileMetadata> fileMetadataList) {
        grid.setItems(fileMetadataList);
        grid.sort(Arrays.asList(new GridSortOrder<>(grid.getColumnByKey("fileName"), SortDirection.ASCENDING)));
    }

    @Override
    protected void clearAllFields() {
        fileName.clear();
    }

    private void autoSizeFields() {
        fileName.setWidth("240px");
        grid.setWidthFull();
    }
}