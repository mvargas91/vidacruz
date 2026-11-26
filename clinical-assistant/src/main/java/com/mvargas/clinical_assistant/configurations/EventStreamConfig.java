package com.mvargas.clinical_assistant.configurations;

import com.mvargas.clinical_assistant.models.request.PatientEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

@Configuration
public class EventStreamConfig {

    @Bean
    public Sinks.Many<Message<PatientEvent>> eventSink() {
        return Sinks.many().multicast().onBackpressureBuffer(1000, false);
    }

    @Bean
    public Supplier<Flux<Message<PatientEvent>>> triajeEventos(
            Sinks.Many<Message<PatientEvent>> eventSink) {
        return eventSink::asFlux;
    }
}
