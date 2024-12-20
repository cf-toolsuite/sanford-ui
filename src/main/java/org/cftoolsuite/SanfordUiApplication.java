package org.cftoolsuite;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import reactivefeign.spring.config.EnableReactiveFeignClients;

@Push
@SpringBootApplication
@EnableFeignClients
@EnableReactiveFeignClients
@ConfigurationPropertiesScan
@Theme(themeClass = Lumo.class, variant = Lumo.DARK)
@PWA(name = "A simple user interface to interact with a sanford instance", shortName = "sanford-ui")
public class SanfordUiApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(SanfordUiApplication.class, args);
	}

}
