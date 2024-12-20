package org.cftoolsuite.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
public class ProfilesClient {

    private static final Logger log = LoggerFactory.getLogger(ProfilesClient.class);

    private final RestClient client;

    public ProfilesClient(@Value("${document.service.url}") String sanfordUrl) {
        client = RestClient.builder()
            .baseUrl(sanfordUrl)
            .build();
    }

    public Set<String> getProfiles() {
        Set<String> result = new HashSet<>();
        ResponseEntity<Map<String, Object>> response =
            client
                .get()
                .uri("/actuator/info")
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});
        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("active-profiles")) {
                String profiles = (String) body.get("active-profiles");
                result = Stream.of(profiles.trim().split(","))
                        .collect(Collectors.toSet());
            } else {
                log.warn("Could not determine active profiles.");
            }
        }
        return result;
    }

    public boolean contains(String profile) {
        return getProfiles().contains(profile);
    }
}
