package com.mvargas.clinical_assistant.services;

import com.mvargas.clinical_assistant.components.PatientEventPublisher;
import com.mvargas.clinical_assistant.enums.EventType;
import com.mvargas.clinical_assistant.enums.PatientStatus;
import com.mvargas.clinical_assistant.models.entities.PatientAudit;
import com.mvargas.clinical_assistant.models.request.EscalatedPatient;
import com.mvargas.clinical_assistant.models.request.PatientEvent;
import com.mvargas.clinical_assistant.repositories.EscalationRepository;
import com.mvargas.clinical_assistant.repositories.PatientAuditRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class SlaEscalationService {

    private final EscalationRepository escalationRepository;
    private final PatientAuditRepository patientAuditRepository;
    private final PatientEventPublisher patientEventPublisher;
    private final TransactionalOperator transactionalOperator;
    private final MeterRegistry meterRegistry;

    public Mono<Void> escalateOverdue() {
        return transactionalOperator.transactional(markAndAudit())
                .flatMapMany(Flux::fromIterable)
                .concatMap(this::publishEscalation)
                .then();
    }

    private Mono<List<EscalatedPatient>> markAndAudit() {
        return escalationRepository.markOverdue()
                .concatMap(p -> patientAuditRepository
                        .save(PatientAudit.builder()
                                .patientId(p.id())
                                .newStatus(PatientStatus.REGISTRADO.name())
                                .previousState(PatientStatus.REGISTRADO.name())
                                .eventType(EventType.PACIENTE_ESCALADO)
                                .build()
                        )
                        .thenReturn(p))
                .collectList();
    }

    private Mono<Void> publishEscalation(final EscalatedPatient p) {
        meterRegistry.counter("triage.escalamientos", "nivel", p.slaLevel()).increment();
        return patientEventPublisher
                .publish(PatientEvent.of(p.id(), PatientStatus.REGISTRADO,
                        PatientStatus.REGISTRADO, EventType.PACIENTE_ESCALADO))
                .onErrorResume(e -> {
                    log.error("Could not publish patient_escalated id: {}", p.id(), e);
                    return Mono.empty();
                });
    }
}
