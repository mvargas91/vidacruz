package com.mvargas.clinical_assistant.components;

import com.mvargas.clinical_assistant.enums.NivelTriaje;
import com.mvargas.clinical_assistant.enums.TipoEvento;
import com.mvargas.clinical_assistant.models.request.PatientEvent;
import com.mvargas.clinical_assistant.models.response.TriageEvent;
import com.mvargas.clinical_assistant.services.AssistantService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Configuration
@AllArgsConstructor
@Slf4j
public class PatientEventConsumer {

    private final AssistantService assistantService;

    @Bean
    public Consumer<Flux<PatientEvent>> eventTriageConsumer() {
        log.info("Initializing event triage consumer");

        return flux -> flux
                .doOnNext(patientEvent -> log.info("patient event received: {}", patientEvent))
                .flatMap(this::processMessage)
                .doOnError(err -> log.error("Error processing patient event", err))
                .retry(3)
                .subscribe();
    }

    private Mono<Void> processMessage(PatientEvent patientEvent) {
        log.info("Processing patient event {}", patientEvent);

        return Mono.fromCallable(() -> new TriageEvent(
                TipoEvento.fromString(patientEvent.eventType()),
                patientEvent.patientId(),
                NivelTriaje.fromString(patientEvent.level())
        ))
        .flatMap(this.assistantService::apply)
        .onErrorResume(throwable -> {
            log.error("Error processing patient event {} message: {}", patientEvent, throwable.getMessage());
            return Mono.empty();
        });
    }
}
