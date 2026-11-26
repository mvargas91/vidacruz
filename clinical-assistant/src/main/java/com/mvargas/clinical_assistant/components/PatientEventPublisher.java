package com.mvargas.clinical_assistant.components;

import com.mvargas.clinical_assistant.models.request.PatientEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@Component
@Slf4j
public class PatientEventPublisher {


    private final Sinks.Many<Message<PatientEvent>> sink;

    public PatientEventPublisher(Sinks.Many<Message<PatientEvent>> sink) {
        this.sink = sink;
    }

    public Mono<Void> publish(final PatientEvent event) {
        return Mono.fromRunnable(() -> {
            Message<PatientEvent> message = MessageBuilder.withPayload(event)
                    .setHeader("kafka_messageKey", String.valueOf(event.patientId()))
                    .build();

            sink.emitNext(message, Sinks.EmitFailureHandler.busyLooping(Duration.ofMillis(100)));
            log.info("Evento {} encolado, paciente={}", event.eventType(), event.patientId());
        });
    }
}
