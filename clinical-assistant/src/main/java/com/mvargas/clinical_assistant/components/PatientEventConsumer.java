package com.mvargas.clinical_assistant.components;

import com.mvargas.enums.NivelTriaje;
import com.mvargas.enums.TipoEvento;
import com.mvargas.models.request.PatientEvent;
import com.mvargas.models.response.TriageEvent;
import com.mvargas.clinical_assistant.services.AssistantService;
import io.micrometer.core.instrument.MeterRegistry;
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
    private final MeterRegistry meterRegistry;

    private static final String METRIC_NAME = "count.patient.status";
    private static final String NAME_TAG = "status";
    private static final String STATUS_FAILED = "failed";
    private static final String STATUS_OK = "success";

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
        .doOnSuccess(unused -> meterRegistry.counter(METRIC_NAME, NAME_TAG, STATUS_OK).increment())
        .onErrorResume(throwable -> {
            meterRegistry.counter(METRIC_NAME, NAME_TAG, STATUS_FAILED).increment();
            log.error("Error processing patient event {} message: {}", patientEvent, throwable.getMessage());
            return Mono.empty();
        });
    }
}
