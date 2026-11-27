package com.mvargas.clinical_assistant.components;

import com.mvargas.clinical_assistant.models.request.PatientEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
@Slf4j
public class PatientEventPublisher {

    private final Sinks.Many<Message<PatientEvent>> eventSink;

    public Mono<Void> publish(final PatientEvent patientEvent) {

        log.info("Publish patientEvent");

        return Mono.fromRunnable(() -> {

            Message<PatientEvent> message = MessageBuilder.withPayload(patientEvent)
                    .setHeader(KafkaHeaders.KEY, String.valueOf(patientEvent.patientId()))
                    .build();

            Sinks.EmitResult result = this.eventSink.tryEmitNext(message);

            if (result.isSuccess()) {
                log.info("PatientEvent request sent successfully");
            }
        });
    }
}
