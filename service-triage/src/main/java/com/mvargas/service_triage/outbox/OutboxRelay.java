package com.mvargas.service_triage.outbox;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import com.mvargas.enums.OutboxStatus;
import com.mvargas.models.entities.OutboxEvent;
import com.mvargas.service_triage.repositories.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private static final String BINDING = "triajeEventos-out-0";
    private static final int BATCH_SIZE = 50;
    private static final int MAX_ATTEMPTS = 5;

    private final OutboxRepository repository;
    private final StreamBridge streamBridge;
    private final TransactionalOperator transactionalOperator;

    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:1000}")
    public Mono<Void>  publishPending() {
        return repository.lockPending(BATCH_SIZE)
                .concatMap(this::publish)
                .then()
                .as(transactionalOperator::transactional)
                .onErrorResume(ex -> {
                    log.error("Error en el relay de outbox", ex);
                    return Mono.empty();
                });
    }

    private Mono<Void> publish(OutboxEvent event) {
        return Mono.fromCallable(() -> send(event))
                .subscribeOn(Schedulers.boundedElastic())   // send() bloquea
                .flatMap(sent -> {
                    if (!sent) {
                        return Mono.error(new IllegalStateException("StreamBridge devolvió false"));
                    }
                    event.setStatus(OutboxStatus.PUBLISHED);
                    event.setPublishedAt(LocalDateTime.now());
                    return repository.save(event).then();
                })
                .onErrorResume(ex -> {
                    log.warn("No se pudo publicar el evento {}", event.getEventId(), ex);
                    event.setAttempts(event.getAttempts() + 1);
                    event.setLastError(ex.getMessage());
                    if (event.getAttempts() >= MAX_ATTEMPTS) {
                        event.setStatus(OutboxStatus.FAILED);
                    }
                    return repository.save(event).then();
                });
    }

    private boolean send(OutboxEvent event) {
        Message<String> message = MessageBuilder
                .withPayload(event.getPayload())
                .setHeader(KafkaHeaders.KEY, event.getAggregateId())
                .build();
        return streamBridge.send(BINDING, message);
    }

    @Scheduled(cron = "0 0 3 * * *")
    public Mono<Void> cleanup() {
        return repository.deletePublishedBefore(LocalDateTime.now().minusDays(7)).then();
    }
}
