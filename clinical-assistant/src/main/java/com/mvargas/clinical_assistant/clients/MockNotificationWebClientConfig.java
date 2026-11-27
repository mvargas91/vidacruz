package com.mvargas.clinical_assistant.clients;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class MockNotificationWebClientConfig {

    private final AtomicInteger requestCounter = new AtomicInteger(0);

    @Bean
    public WebClient notificationWebClient() {
        ExchangeFunction mockExchangeFunction = _ -> {
            int currentAttempt = requestCounter.incrementAndGet();

            if (currentAttempt % 3 == 0) {
                return Mono.just(
                        ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .body("{\"error\": \"Servicio externo de notificaciones no disponible\"}")
                                .build()
                );
            }

            return Mono.just(
                    ClientResponse.create(HttpStatus.OK)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body("{\"status\": \"SENT\", \"attempt\": " + currentAttempt + "}")
                            .build()
            );
        };

        return WebClient.builder()
                .baseUrl("https://mock-notificaciones.com")
                .exchangeFunction(mockExchangeFunction)
                .build();
    }
}