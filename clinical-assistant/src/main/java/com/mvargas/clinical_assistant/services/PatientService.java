package com.mvargas.clinical_assistant.services;

import com.mvargas.clinical_assistant.components.PatientEventPublisher;
import com.mvargas.enums.EventType;
import com.mvargas.enums.PatientStatus;
import com.mvargas.clinical_assistant.exceptions.PatientAlreadyExistsException;
import com.mvargas.clinical_assistant.exceptions.PatientNotFoundException;
import com.mvargas.clinical_assistant.exceptions.PatientServiceException;
import com.mvargas.clinical_assistant.exceptions.PatientStatusConflictException;
import com.mvargas.clinical_assistant.mappers.PatientMapper;
import com.mvargas.models.entities.Patient;
import com.mvargas.models.entities.PatientAudit;
import com.mvargas.models.request.PatientEvent;
import com.mvargas.models.response.PatientRs;
import com.mvargas.clinical_assistant.repositories.PatientAuditRepository;
import com.mvargas.clinical_assistant.repositories.PatientRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository repository;
    private final PatientAuditRepository auditRepository;
    private final PatientMapper mapper;
    private final TransactionalOperator transactionalOperator;
    private final PatientEventPublisher patientEventPublisher;

    public Mono<PatientRs> getPatientById(final Long patientId){
        log.info("get information of the patient");
        return repository.findById(patientId)
                .switchIfEmpty(Mono.error(new PatientNotFoundException(patientId)))
                .map(mapper::toResponse)
                .onErrorMap(this::translate);
    }

    public Mono<Patient> updatePatientStatus(final Long patientId,
                                             final PatientStatus newStatus) {
        return transactionalOperator.transactional(persistChange(patientId, newStatus))
                .flatMap(statusChange -> publishEvent(statusChange.patientEvent)
                        .thenReturn(statusChange.patient())
                );
    }

    private Mono<StatusChange> persistChange(final Long patientId,
                                             final PatientStatus newStatus) {
        return repository.findById(patientId)
                .switchIfEmpty(Mono.error(new PatientNotFoundException(patientId)))
                .flatMap(patient -> {

                    final EventType type = EventType.byState(newStatus);
                    final var currentStatus = patient.getPatientStatus();

                    if (!currentStatus.canTransitionTo(newStatus)) {
                        return Mono.error(new PatientStatusConflictException(currentStatus, newStatus));
                    }

                    patient.setPatientStatus(newStatus);

                    return repository.save(patient)
                            .flatMap(patientSave -> auditRepository.save(PatientAudit.builder()
                                    .patientId(patientSave.getId())
                                    .newStatus(newStatus.name())
                                    .previousState(currentStatus.name())
                                    .eventType(type)
                                    .build()
                            ).thenReturn(new StatusChange(
                                    patientSave,
                                    PatientEvent.of(type.getValor(),
                                            patientSave.getId(),
                                            patientSave.getSlaLevel().name(),
                                            patientSave.getSlaLevel().getMinutes(),
                                            newStatus))
                            ));
                });
    }

    private Mono<Void> publishEvent(final PatientEvent patientEvent) {
        return patientEventPublisher
                .publish(patientEvent)
                .onErrorResume(e -> {
                    log.error("No se pudo publicar {} del paciente {}", patientEvent.eventType(), patientEvent.patientId(), e);
                    return Mono.empty();
                });
    }

    private record StatusChange(Patient patient, PatientEvent patientEvent) {}

    private Throwable translate(final Throwable e) {
        if (e instanceof PatientNotFoundException
                || e instanceof PatientAlreadyExistsException
                || e instanceof PatientServiceException) {
            return e;
        }
        log.error("Error inesperado en ProductService", e);
        return new PatientServiceException("Error procesando la solicitud de productos", e);
    }
}
