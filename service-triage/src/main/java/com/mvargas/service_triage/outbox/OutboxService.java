package com.mvargas.service_triage.outbox;

import com.mvargas.enums.OutboxStatus;
import com.mvargas.models.entities.OutboxEvent;
import com.mvargas.service_triage.repositories.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;

    public Mono<OutboxEvent> enqueue(final String aggregateType,
                                     final String aggregateId,
                                     final String eventType,
                                     final UUID eventId,
                                     final Object payload) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(payload))
                .map(json -> OutboxEvent.builder()
                        .eventId(eventId)
                        .aggregateType(aggregateType)
                        .aggregateId(aggregateId)
                        .eventType(eventType)
                        .payload(json)
                        .status(OutboxStatus.PENDING)
                        .attempts(0)
                        .createdAt(LocalDateTime.now())
                        .build())
                .flatMap(repository::save);
    }
}
