package org.cftoolsuite.ui.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.collections4.CollectionUtils;
import org.cftoolsuite.client.SanfordStreamingClient;
import org.cftoolsuite.domain.chat.FilterMetadata;
import org.cftoolsuite.domain.chat.Inquiry;
import org.cftoolsuite.ui.MainLayout;
import org.cftoolsuite.ui.component.Markdown;
import org.cftoolsuite.ui.component.MetadataFilter;

import java.util.List;

@PageTitle("sanford-ui » Chat")
@Route(value = "chat", layout = MainLayout.class)
public class ChatView extends BaseStreamingView  {

    private TextField question;
    private MetadataFilter metadataFilter;
    private Button submitButton;
    private Button clearButton;
    private HorizontalLayout buttons;
    private Markdown chatHistory;

    public ChatView(SanfordStreamingClient sanfordStreamingClient) {
        super(sanfordStreamingClient);
    }

    @Override
    protected void setupUI() {
        var ui = UI.getCurrent();
        setSizeFull();

        // Create chat history container
        VerticalLayout chatHistoryContainer = new VerticalLayout();
        chatHistoryContainer.setSizeFull();
        chatHistoryContainer.setPadding(false);
        chatHistoryContainer.setSpacing(false);

        // Add Markdown with styling
        chatHistory = new Markdown();
        chatHistory.getElement().getStyle()
            .set("height", "100%")
            .set("width", "100%")
            .set("overflow", "auto");

        chatHistoryContainer.add(chatHistory);
        chatHistoryContainer.setFlexGrow(1, chatHistory);

        // Create input components
        question = new TextField();
        question.setPlaceholder("Type a question...");
        question.setWidth("100%");

        metadataFilter = new MetadataFilter();
        metadataFilter.setWidth("100%");
        metadataFilter.setLabel("Metadata Filters");

        // Create buttons
        this.buttons = new HorizontalLayout();
        submitButton = new Button("Submit");
        submitButton.addClickListener(e -> {
            // Prepare the question and metadata message
            String inquiry = formatQuestion(
                question.getValue(),
                metadataFilter.getValue()
            );

            // Clear previous content and append question message
            chatHistory.clear();
            chatHistory.appendMarkdown(inquiry);

            // Stream the response
            sanfordStreamingClient
                .streamResponseToQuestion(new Inquiry(question.getValue(), metadataFilter.getValue()))
                .subscribe(ui.accessLater(response -> {
                    chatHistory.appendMarkdown("\n\n" + response);
                }, null));
        });
        submitButton.addClickShortcut(Key.ENTER);

        this.clearButton = new Button("Clear");
        clearButton.addClickListener(e -> clearAllFields());
        buttons.add(submitButton, clearButton);

        // Create input layout with bottom-anchoring
        VerticalLayout inputLayout = new VerticalLayout(question, metadataFilter, buttons);
        inputLayout.setSpacing(false);
        inputLayout.setPadding(false);
        inputLayout.setWidth("100%");

        // Create a wrapper layout to control height distribution
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);

        // Add header
        H2 header = new H2("Chat");
        mainLayout.add(header);

        // Add chat history with expand ratio
        mainLayout.add(chatHistoryContainer);
        mainLayout.setFlexGrow(2, chatHistoryContainer); // This gives chatHistory 2/3 of the space

        // Add input layout at the bottom
        mainLayout.add(inputLayout);
        mainLayout.setFlexGrow(0, inputLayout); // This prevents input layout from expanding

        // Replace the direct add with adding the main layout
        add(mainLayout);
    }

    private String formatQuestion(String question, List<FilterMetadata> metadata) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("**").append(question).append("**").append("\n\n");
        if (CollectionUtils.isNotEmpty(metadata)) {
            messageBuilder.append("Filtered by: { ");
            metadata.forEach(fm ->
                messageBuilder.append(fm.key())
                    .append(": ").append(fm.value()).append(" | ")
            );
            messageBuilder.append(" }");
        }
        String formattedQuestion = messageBuilder.toString();
        if (formattedQuestion.endsWith("|  }")) formattedQuestion = formattedQuestion.replaceAll("\\|\\s*}", "}");
        return formattedQuestion;
    }
    
    @Override
    protected void clearAllFields() {
        question.clear();
        chatHistory.clear();
        metadataFilter.clear();
    }
}
