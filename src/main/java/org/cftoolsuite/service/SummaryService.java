package org.cftoolsuite.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

@Service
public class SummaryService {

    private final WebClient webClient;

    public SummaryService(@Value("${document.service.url}") String baseUrl) {
        this.webClient = WebClient.create(baseUrl);
    }

    public Flux<String> getSummary(String fileName) {
        return
            webClient
                .get()
                .uri("/api/files/stream/summary/{fileName}", fileName)
                .retrieve()
                .bodyToFlux(String.class);
    }
}