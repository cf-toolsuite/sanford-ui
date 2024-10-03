package org.cftoolsuite.ui.view;

import org.cftoolsuite.client.SanfordClient;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

public abstract class BaseView extends VerticalLayout {

    protected final SanfordClient sanfordClient;

    public BaseView(SanfordClient sanfordClient) {
        this.sanfordClient = sanfordClient;
    }

    protected abstract void setupUI();

    protected abstract void clearAllFields();

    protected void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);

        Div content = new Div();
        content.setText(message);
        content.getStyle().set("cursor", "pointer");
        content.addClickListener(event -> notification.close());

        notification.add(content);

        UI.getCurrent().addShortcutListener(
            () -> notification.close(),
            Key.ESCAPE
        );

        notification.open();

        notification.addDetachListener(event ->
            UI.getCurrent().getPage().executeJs(
                "window.Vaadin.Flow.notificationEscListener.remove()"
            )
        );
    }

    protected Image getLogoImage() {
        StreamResource imageResource = new StreamResource("sanford.png",
            () -> getClass().getResourceAsStream("/static/sanford.png"));
        Image logo = new Image(imageResource, "Logo");
        logo.setWidth("240px");
        return logo;
    }
}