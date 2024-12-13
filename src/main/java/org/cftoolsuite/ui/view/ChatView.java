package org.cftoolsuite.ui.view;


import org.cftoolsuite.client.SanfordStreamingClient;
import org.cftoolsuite.ui.MainLayout;
import org.cftoolsuite.ui.component.Markdown;
import org.cftoolsuite.ui.component.MetadataFilter;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("sanford-ui » Chat")
@Route(value = "chat", layout = MainLayout.class)
public class ChatView extends BaseStreamingView  {

    private TextField messageInput;
    private MetadataFilter metadataFilter;
    private Button sendButton;
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
        setPadding(true);
        setSpacing(true);

        chatHistory = new Markdown();

        messageInput = new TextField();
        messageInput.setPlaceholder("Type a message...");
        messageInput.setWidth("100%");

        metadataFilter = new MetadataFilter();
        metadataFilter.setWidth("100%");
        metadataFilter.setLabel("Metadata Filters");

        this.buttons = new HorizontalLayout();
        sendButton = new Button("Send");
        sendButton.addClickListener(e -> {
            chatHistory.clear();
            sanfordStreamingClient
                .streamResponseToQuestion(messageInput.getValue(), metadataFilter.getValue())
                .subscribe(ui.accessLater(chatHistory::appendMarkdown, null));
        });
        sendButton.addClickShortcut(Key.ENTER);

        this.clearButton = new Button("Clear");
        clearButton.addClickListener(e -> clearAllFields());
        buttons.add(sendButton, clearButton);

        add(new H2("Chat"));

        VerticalLayout inputLayout = new VerticalLayout(messageInput, metadataFilter, buttons);
        inputLayout.setSpacing(false);
        inputLayout.setPadding(false);

        add(chatHistory, inputLayout);
    }

    @Override
    protected void clearAllFields() {
        messageInput.clear();
        chatHistory.clear();
        metadataFilter.setPresentationValue(null);
    }

}
