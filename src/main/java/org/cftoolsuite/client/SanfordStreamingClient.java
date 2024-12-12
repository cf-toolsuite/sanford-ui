package org.cftoolsuite.client;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

@Component
public class SanfordStreamingClient {

    private final WebClient webClient;

    public SanfordStreamingClient(@Value("${document.service.url}") String baseUrl) {
        this.webClient = WebClient.create(baseUrl);
    }

    public Flux<String> streamResponseToQuestion(String message, Map<String, Object> filterMetadata) {
        Flux<String> response;
        if (MapUtils.isNotEmpty(filterMetadata)) {
            response =
                webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/stream/chat")
                            .queryParam("q", message)
                            .queryParam("f", filterMetadata)
                            .build())
                    .retrieve()
                    .bodyToFlux(String.class);
        } else {
            response =
                webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/stream/chat")
                            .queryParam("q", message)
                            .build())
                    .retrieve()
                    .bodyToFlux(String.class);
        }
        return response;
    }
}
