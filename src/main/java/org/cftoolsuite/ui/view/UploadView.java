package org.cftoolsuite.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.io.FilenameUtils;
import org.cftoolsuite.client.SanfordClient;
import org.cftoolsuite.domain.AppProperties;
import org.cftoolsuite.domain.CustomMultipartFile;
import org.cftoolsuite.domain.FileMetadata;
import org.cftoolsuite.ui.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

@PageTitle("sanford-ui » Upload")
@Route(value = "upload", layout = MainLayout.class)
public class UploadView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(UploadView.class);

    private Upload upload;
    private Button clearButton;
    private VerticalLayout buttons;

    public UploadView(SanfordClient sanfordClient, AppProperties appProperties) {
        super(sanfordClient, appProperties);
    }

    @Override
    protected void setupUI() {
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        int numberOfSupportedContentTypes = supportedContentTypes.size() * 2;
        List<String> acceptedFileExtensions = supportedContentTypes.keySet().stream().map(k -> String.format(".%s", k)).toList();
        List<String> acceptedContentTypes = supportedContentTypes.values().stream().toList();
        List<String> combinedList = Stream.of(acceptedFileExtensions, acceptedContentTypes).flatMap(List::stream).toList();
        String[] acceptedFileTypes = combinedList.toArray(new String[numberOfSupportedContentTypes]);
        upload = new Upload(buffer);
        upload.setDropAllowed(true);
        upload.setAcceptedFileTypes(acceptedFileTypes);
        this.clearButton = new Button("Clear");
        this.buttons = new VerticalLayout();

        buttons.add(upload, clearButton);

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

    protected void uploadRequest(InputStream stream, String fileName) {
        String fileExtension = FilenameUtils.getExtension(fileName);
        String contentType = supportedContentTypes.get(fileExtension);
        try {
            byte[] content = stream.readAllBytes();
            MultipartFile multipartFile = new CustomMultipartFile("file", fileName, contentType, content);

            ResponseEntity<FileMetadata> response = sanfordClient.uploadFile(multipartFile);
            if (response.getStatusCode().is2xxSuccessful()) {
                showNotification("Upload request successful \n" + response.getBody(), NotificationVariant.LUMO_SUCCESS);
            } else {
                String errorMessage = "Error submitting upload request. Status code: " + response.getStatusCode();
                if (response.getBody() != null) {
                    errorMessage += ". Message: " + response.getBody().toString();
                }
                showNotification(errorMessage, NotificationVariant.LUMO_ERROR);
            }
        } catch (IOException e) {
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