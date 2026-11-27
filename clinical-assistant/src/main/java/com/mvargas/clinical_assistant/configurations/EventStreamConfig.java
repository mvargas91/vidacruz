package com.mvargas.clinical_assistant.configurations;

import com.mvargas.models.request.PatientEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

@Configuration
@Slf4j
public class EventStreamConfig {

    @Bean
    public Sinks.Many<Message<PatientEvent>> eventSink() {
        log.info("Creating patientEvent sink");
        return Sinks.many().multicast().onBackpressureBuffer(1000, false);
    }

    @Bean
    public Supplier<Flux<Message<PatientEvent>>> eventTriageSupplier(Sinks.Many<Message<PatientEvent>> eventSink) {
        log.info("Creating patientEvent Supplier");
        return () -> eventSink.asFlux()
                .doOnSubscribe(s -> log.info("Subscribing to patientEvent sink"))
                .doOnCancel(() -> log.info("Canceling patientEvent sink"));
    }
}
