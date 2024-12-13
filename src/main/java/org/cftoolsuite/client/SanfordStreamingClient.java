package org.cftoolsuite.client;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;

import java.util.Map;

@Component
public class SanfordStreamingClient {

    private static Logger log = LoggerFactory.getLogger(SanfordStreamingClient.class);

    private final WebClient webClient;

    public SanfordStreamingClient(@Value("${document.service.url}") String baseUrl) {
        this.webClient = WebClient.create(baseUrl);
    }

    public Flux<String> streamResponseToQuestion(String message, Map<String, Object> filterMetadata) {
        return webClient
                .get()
                .uri(uriBuilder -> {
                    // Start with the base path and message
                    UriBuilder builder = uriBuilder.path("/api/stream/chat")
                            .queryParam("q", message);

                    // Add filter metadata as f[key]=value parameters
                    if (MapUtils.isNotEmpty(filterMetadata)) {
                        filterMetadata.forEach((key, value) -> {
                            builder.queryParam("f[" + key + "]", value);
                        });
                    }

                    // Build the URI
                    return builder.build();
                })
                .retrieve()
                .bodyToFlux(String.class);
    }
}
