package org.cftoolsuite.ui.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.cftoolsuite.client.SanfordStreamingClient;

public abstract class BaseStreamingView extends VerticalLayout {

    protected SanfordStreamingClient sanfordStreamingClient;

    public BaseStreamingView(SanfordStreamingClient sanfordStreamingClient) {
        this.sanfordStreamingClient = sanfordStreamingClient;
        setupUI();
    }

    protected abstract void setupUI();

    protected abstract void clearAllFields();

    protected void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 5000, Position.TOP_STRETCH);
        notification.setPosition(Position.TOP_CENTER);
        notification.addThemeVariants(variant);

        Div content = new Div();
        content.setText(message);
        content.getStyle().set("cursor", "pointer");
        content.addClickListener(event -> notification.close());

        notification.add(content);

        UI.getCurrent().addShortcutListener(
                notification::close,
            Key.ESCAPE
        );

        notification.open();

        notification.addDetachListener(event ->
            UI.getCurrent().getPage().executeJs(
                "window.Vaadin.Flow.notificationEscListener.remove()"
            )
        );
    }
}