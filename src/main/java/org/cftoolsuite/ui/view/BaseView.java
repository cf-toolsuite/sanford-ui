package org.cftoolsuite.ui.view;

import org.cftoolsuite.client.SanfordClient;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.cftoolsuite.domain.AppProperties;

import java.util.Map;

public abstract class BaseView extends VerticalLayout {

    protected SanfordClient sanfordClient;
    Map<String, String> supportedContentTypes;

    public BaseView(SanfordClient sanfordClient, AppProperties appProperties) {
        this.sanfordClient = sanfordClient;
        this.supportedContentTypes = appProperties.supportedContentTypes();
        setupUI();
    }

    protected abstract void setupUI();

    protected void clearAllFields() {}

    protected void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 5000, Position.TOP_STRETCH);
        notification.setPosition(Notification.Position.TOP_CENTER);
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