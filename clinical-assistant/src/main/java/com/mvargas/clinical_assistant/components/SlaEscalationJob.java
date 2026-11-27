package com.mvargas.clinical_assistant.components;

import com.mvargas.clinical_assistant.services.SlaEscalationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class SlaEscalationJob {

    private final SlaEscalationService service;

    @Scheduled(fixedDelayString = "${app.triage.evaluation-interval}")
    public Mono<Void> evaluate() {
        log.info("SLA evaluation started");
        return service.escalateOverdue()
                .onErrorResume(e -> {
                    log.error("SLA evaluation failed", e);
                    return Mono.empty();
                });
    }
}
