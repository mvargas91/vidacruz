package com.mvargas.service_triage.services;

import com.mvargas.models.entities.Patient;
import com.mvargas.models.entities.PatientAudit;
import com.mvargas.models.request.PatientEvent;
import com.mvargas.models.request.PatientRq;
import com.mvargas.models.response.PatientRegisterRs;
import com.mvargas.service_triage.mappers.PatientMapper;
import com.mvargas.service_triage.outbox.OutboxService;
import com.mvargas.service_triage.repositories.PatientAuditRepository;
import com.mvargas.service_triage.repositories.PatientRepository;
import io.r2dbc.spi.R2dbcException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static com.mvargas.enums.EventType.PACIENTE_REGISTRADO;
import static com.mvargas.enums.PatientStatus.REGISTRADO;

@Service
@AllArgsConstructor
@Slf4j
public class TriageService {

    private static final String EVENT_TYPE = "paciente_registrado";

    private final PatientAuditRepository auditRepository;
    private final PatientRepository repository;
    private final PatientMapper mapper;
    private final SlaLevelService slaLevel;
    private final OutboxService outboxService;

    public Mono<PatientRegisterRs> addPatientInformation(final PatientRq request){
        log.info("add information of the patient");
        return Mono.fromSupplier(() -> {
                    Patient patient = mapper.toEntity(request);
                    patient.setSlaLevel(slaLevel.getLevel(request));
                    patient.setRegistrationDate(OffsetDateTime.now(ZoneOffset.UTC));
                    patient.setPatientStatus(REGISTRADO);
                    return patient;
                })
                .flatMap(repository::save)
                .flatMap(patientSaved -> saveAudit(patientSaved).thenReturn(patientSaved))
                .flatMap(saved -> {
                    final UUID eventId = UUID.randomUUID();
                    final PatientEvent event = new PatientEvent(
                            eventId, EVENT_TYPE, Instant.now(),
                            saved.getId(),
                            saved.getSlaLevel().getLevel(),
                            saved.getSlaLevel().getMinutes(),
                            saved.getPatientStatus());
                    return outboxService
                            .enqueue("PATIENT", String.valueOf(saved.getId()),
                                    EVENT_TYPE, eventId, event)
                            .thenReturn(saved);
                })
                .map(mapper::toResponse)
                .onErrorResume(this::handleDbError);
    }

    private Mono<PatientAudit> saveAudit(final Patient patient) {
        return auditRepository.save(PatientAudit.builder()
                .patientId(patient.getId())
                .previousState(REGISTRADO.name())                         // al registrar no hay estado anterior
                .newStatus(REGISTRADO.name())
                .eventType(PACIENTE_REGISTRADO)
                .build());
    }

    private <T> Mono<T> handleDbError(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            return Mono.error(ex);
        }
        if (ex instanceof DataIntegrityViolationException) {
            // CHECK, NOT NULL, UNIQUE, FK
            return Mono.error(new ResponseStatusException(
                    HttpStatus.CONFLICT, "Los datos violan una restricción de la base de datos", ex));
        }
        if (ex instanceof R2dbcException) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error al acceder a la base de datos", ex));
        }
        return Mono.error(ex);
    }
}
