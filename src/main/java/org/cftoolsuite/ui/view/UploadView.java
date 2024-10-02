package org.cftoolsuite.ui.view;

import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.cftoolsuite.client.SanfordClient;
import org.cftoolsuite.domain.AppProperties;
import org.cftoolsuite.domain.CustomMultipartFile;
import org.cftoolsuite.domain.FileMetadata;
import org.cftoolsuite.ui.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.PostConstruct;

@PageTitle("sanford-ui » Upload")
@Route(value = "upload", layout = MainLayout.class)
public class UploadView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(UploadView.class);

    private Upload upload;
    private Button clearButton;
    private VerticalLayout buttons;
    Map<String, String> supportedContentTypes;

    public UploadView(SanfordClient sanfordClient, AppProperties appProperties) {
        super(sanfordClient);
        this.supportedContentTypes = appProperties.supportedContentTypes();
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
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        int numberOfSupportedContentTypes = supportedContentTypes.size();
        String[] acceptedFileTypes = supportedContentTypes.values().toArray(new String[numberOfSupportedContentTypes]);
        upload = new Upload(buffer);
        upload.setDropAllowed(true);
        upload.setAcceptedFileTypes(acceptedFileTypes);
        this.clearButton = new Button("Clear");
        this.buttons = new VerticalLayout();

        buttons.add(upload, clearButton);

        buttons.setAlignItems(Alignment.CENTER);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);
        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            InputStream inputStream = buffer.getInputStream(fileName);
            uploadRequest(inputStream, fileName);
        });
        clearButton.addClickListener(event -> clearAllFields());

        add(
            new H2("Upload documents"),
            buttons
        );

        autoSizeFields();
    }

    @Override
    protected void uploadRequest(InputStream stream, String fileName) {
        String fileExtension = FilenameUtils.getExtension(fileName);
        String contentType = supportedContentTypes.get(fileExtension);
        try {
            ResponseEntity<FileMetadata> response = sanfordClient.uploadFile(new CustomMultipartFile("fileName", fileName, contentType, stream));
            if (response.getStatusCode().is2xxSuccessful()) {
                showNotification("Upload request successful \n" + response.getBody(), NotificationVariant.LUMO_SUCCESS);
            } else {
                String errorMessage = "Error submitting ingest request. Status code: " + response.getStatusCode();
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
        upload.clearFileList();
    }

    private void autoSizeFields() {
        upload.setWidth("240px");
    }
}