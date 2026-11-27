package com.mvargas.clinical_assistant.components;

import com.mvargas.clinical_assistant.enums.NivelTriaje;
import com.mvargas.clinical_assistant.enums.TipoEvento;
import com.mvargas.clinical_assistant.models.request.PatientEvent;
import com.mvargas.clinical_assistant.models.response.TriageEvent;
import com.mvargas.clinical_assistant.services.AlertEventService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Configuration
@AllArgsConstructor
@Slf4j
public class AlertEventConsumer {

    private final AlertEventService service;

    @Bean
    public Consumer<Flux<PatientEvent>> eventAlertConsumer() {
        log.info("Initializing event alert consumer");

        return flux -> flux
                .doOnNext(patientEvent -> log.info("alert event received: {}", patientEvent))
                .filter(isScaled())
                .flatMap(this::processMessage)
                .doOnError(err -> log.error("Error processing alert event", err))
                .retry(3)
                .subscribe();
    }

    private Predicate<PatientEvent> isScaled() {
        return event -> event != null && TipoEvento.ESCALADO.nombre().equalsIgnoreCase(event.eventType());
    }

    private Mono<Void> processMessage(PatientEvent patientEvent) {
        log.info("Processing alert event {}", patientEvent);

        return Mono.fromCallable(() -> new TriageEvent(
                TipoEvento.fromString(patientEvent.eventType()),
                patientEvent.patientId(),
                NivelTriaje.fromString(patientEvent.level())
        ))
        .flatMap(this.service::sendAlert)
        .onErrorResume(throwable -> {
            log.error("Error processing alert event {} message: {}", patientEvent, throwable.getMessage());
            return Mono.empty();
        });
    }
}
