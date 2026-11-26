package com.mvargas.service_triage.repositories;

import com.mvargas.service_triage.models.entities.OutboxEvent;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface OutboxRepository extends ReactiveCrudRepository<OutboxEvent, Long> {

    static String SELECT_OUTBOX_EVENT_QUERY = """
            SELECT * FROM outbox_event
            WHERE status = 'PENDING'
            ORDER BY id
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """;

    static String DELETE_OUTBOX_EVENT_QUERY = """
            DELETE FROM outbox_event
            WHERE status = 'PUBLISHED' AND published_at < :before
            """;

    // SKIP LOCKED evita que dos instancias del servicio tomen los mismos eventos
    @Query(SELECT_OUTBOX_EVENT_QUERY)
    Flux<OutboxEvent> lockPending(final int limit);

    @Modifying
    @Query(DELETE_OUTBOX_EVENT_QUERY)
    Mono<Long> deletePublishedBefore(final LocalDateTime before);
}
