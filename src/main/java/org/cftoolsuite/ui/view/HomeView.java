package org.cftoolsuite.ui.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.cftoolsuite.ui.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "", layout = MainLayout.class)
@PageTitle("sanford-ui » Home")
public class HomeView extends Div {

    private static Logger log = LoggerFactory.getLogger(HomeView.class);

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        log.trace("Navigated to ROOT... now redirecting!");
        UI.getCurrent().navigate(UploadView.class);
    }
}