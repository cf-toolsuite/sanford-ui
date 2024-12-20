package org.cftoolsuite.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.cftoolsuite.client.SanfordClient;
import org.cftoolsuite.domain.AppProperties;
import org.cftoolsuite.ui.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@PageTitle("sanford-ui » Download")
@Route(value = "download", layout = MainLayout.class)
public class DownloadView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(DownloadView.class);

    private TextField fileName;
    private Button downloadButton;
    private Button clearButton;
    private HorizontalLayout buttons;

    public DownloadView(SanfordClient sanfordClient, AppProperties appProperties) {
        super(sanfordClient, appProperties);
    }

    @Override
    protected void setupUI() {
        this.fileName = new TextField("File name");
        this.fileName.setRequired(true);
        this.fileName.setHelperText("Specify the name of the file you would like to download.");
        this.downloadButton = new Button("Download");
        this.clearButton = new Button("Clear");
        this.buttons = new HorizontalLayout();

        FileDownloadWrapper wrapper = new FileDownloadWrapper(
            new StreamResource(fileName.getValue(), () -> getFileContent(fileName.getValue()))
        );
        wrapper.wrapComponent(downloadButton);
        wrapper.setFileName(fileName.getValue());

        buttons.add(wrapper, clearButton);

        buttons.setAlignItems(Alignment.CENTER);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);
        clearButton.addClickListener(event -> clearAllFields());

        add(
            new H2("Download a document"),
            fileName,
            buttons
        );

        autoSizeFields();
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

    @Override
    protected void clearAllFields() {
        fileName.clear();
    }

    private void autoSizeFields() {
        fileName.setWidth("240px");
    }
}