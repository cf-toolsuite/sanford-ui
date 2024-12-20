package org.cftoolsuite.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import elemental.json.JsonObject;
import org.apache.commons.io.FileUtils;
import org.cftoolsuite.client.SanfordClient;
import org.cftoolsuite.domain.AppProperties;
import org.cftoolsuite.domain.CustomMultipartFile;
import org.cftoolsuite.ui.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Base64;

@PageTitle("sanford-ui » Converse")
@Route(value = "converse", layout = MainLayout.class)
@JavaScript("./flow/audio-recorder.js")
public class ConverseView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(ConverseView.class);

    private boolean isRecording = false;
    private Button recordButton;
    private Div responseDiv;
    private Div loadingDiv;

    public ConverseView(SanfordClient sanfordClient, AppProperties appProperties) {
        super(sanfordClient, appProperties);
    }

    @Override
    protected void setupUI() {
        addClassName("box-border");
        setAlignItems(Alignment.CENTER);
        setSpacing(true);
        setPadding(true);

        setupRobotEmoji();
        setupTitle();
        setupMainContainer();
    }

    private void setupRobotEmoji() {
        Span robotEmoji = new Span("🤖");
        robotEmoji.getStyle().set("font-size", "100px");
        add(robotEmoji);
    }

    private void setupTitle() {
        H2 title = new H2("Converse");
        add(title);
    }

    private void setupMainContainer() {
        Div container = new Div();
        container.addClassNames("max-w-screen-sm", "w-full", "space-y-4");
        container.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "var(--lumo-space-l)");

        setupRecordButton();
        setupLoadingIndicator();
        setupResponseDiv();

        Div buttonContainer = new Div(recordButton);
        buttonContainer.getStyle()
                .set("display", "flex")
                .set("justify-content", "center")
                .set("gap", "var(--lumo-space-m)");

        container.add(buttonContainer, loadingDiv, responseDiv);
        add(container);
    }

    private void setupRecordButton() {
        recordButton = new Button();
        updateRecordButtonState();
        recordButton.addClickListener(e -> toggleRecording());
    }

    private void setupLoadingIndicator() {
        loadingDiv = new Div();
        loadingDiv.setText("Hold on, I'm thinking...");
        loadingDiv.getStyle()
                .set("display", "none")
                .set("justify-content", "center");
    }

    private void setupResponseDiv() {
        responseDiv = new Div();
        responseDiv.getStyle().set("display", "none");
    }

    private void updateRecordButtonState() {
        recordButton.setIcon(isRecording ? VaadinIcon.STOP.create() : VaadinIcon.MICROPHONE.create());
        recordButton.setText(isRecording ? "Stop Recording" : "Start Recording");
        recordButton.setThemeName(isRecording ? "error" : "primary");
    }

    private void toggleRecording() {
        isRecording = !isRecording;
        updateRecordButtonState();

        if (isRecording) {
            getUI().ifPresent(ui ->
                    ui.getPage().executeJs("return window.audioRecorder.startRecording()"));
        } else {
            getUI().ifPresent(ui -> {
                PendingJavaScriptResult result = ui.getPage()
                        .executeJs("return window.audioRecorder.stopRecording()");
                result.then(JsonObject.class, this::handleRecordingResult);
            });
        }
    }

    private void handleRecordingResult(JsonObject result) {
        try {
            String base64Audio = result.getString("audioData");
            byte[] audioData = Base64.getDecoder().decode(base64Audio);

            // Create temporary file
            File tempFile = File.createTempFile("audio-", ".webm");
            FileUtils.writeByteArrayToFile(tempFile, audioData);

            // Convert to MultipartFile
            MultipartFile multipartFile = new CustomMultipartFile(
                    "file",
                    "recording.webm",
                    "audio/webm",
                    FileUtils.readFileToByteArray(tempFile)
            );

            // Show loading indicator
            UI.getCurrent().access(() -> loadingDiv.getStyle().set("display", "flex"));

            // Make API call using Feign client
            var response = sanfordClient.converse(multipartFile);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Update UI with response
                UI.getCurrent().access(() -> {
                    loadingDiv.getStyle().set("display", "none");
                    responseDiv.getStyle().set("display", "block");

                    if (response.getBody() != null) {
                        responseDiv.setText(response.getBody().text());

                        // Play audio response using our JavaScript function
                        UI.getCurrent().getPage().executeJs(
                                "window.audioRecorder.playAudioResponse($0)", response.getBody().audioBase64()
                        );
                    }
                });
            } else {
                UI.getCurrent().access(() -> {
                    loadingDiv.getStyle().set("display", "none");
                });
                showNotification("Error conversing with chatbot", NotificationVariant.LUMO_ERROR);
            }

            // Cleanup temp file
            tempFile.delete();

        } catch (Exception e) {
            log.error("Error conversing with chatbot", e);
            UI.getCurrent().access(() -> {
                loadingDiv.getStyle().set("display", "none");
            });
            showNotification("Error conversing with chatbot: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }
}
