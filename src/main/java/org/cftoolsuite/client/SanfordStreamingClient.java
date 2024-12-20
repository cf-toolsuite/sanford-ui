package org.cftoolsuite.client;

import org.cftoolsuite.domain.chat.Inquiry;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Flux;

@ReactiveFeignClient(name="sanford-streaming-client", url="${document.service.url}")
public interface SanfordStreamingClient {

    @PostMapping("/api/stream/chat")
    Flux<String> streamResponseToQuestion(@RequestBody Inquiry inquiry);
}

