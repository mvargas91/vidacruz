package com.mvargas.clinical_assistant.services;

import com.mvargas.clinical_assistant.models.response.TriageEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class AlertEventService {

    private final WebClient notificationWebClient;

    private static final String RESOURCE = "/v1/alerts";
    private static final String ERROR_MSG_5XX = "Error occurred while calling reservation request";
    private static final Mono<Throwable> MONO_5XX = Mono.error(new IllegalArgumentException(ERROR_MSG_5XX));

    public Mono<Void> sendAlert(TriageEvent event) {
        return notificationWebClient.post()
                .uri(RESOURCE)
                .bodyValue(event)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, _ -> MONO_5XX)
                .bodyToMono(String.class)
                .doOnSuccess(res -> log.info("✅ Alert sent: {}", res))
                .doOnError(err -> log.error("❌ Error sending alert: {}", err.getMessage()))
                .then()
                .onErrorResume(err -> {
                    log.error("❌ Error for save in data base: {}", err.getMessage());
                    return Mono.empty();
                });
    }
}
