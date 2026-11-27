package com.mvargas.service_triage.services;

import com.mvargas.service_triage.enums.PatientStatus;
import com.mvargas.service_triage.mappers.PatientMapper;
import com.mvargas.service_triage.models.entities.Patient;
import com.mvargas.service_triage.models.request.PatientEvent;
import com.mvargas.service_triage.models.request.PatientRq;
import com.mvargas.service_triage.models.response.PatientRs;
import com.mvargas.service_triage.outbox.OutboxService;
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

@Service
@AllArgsConstructor
@Slf4j
public class TriageService {

    private static final String EVENT_TYPE = "paciente_registrado";

    private final PatientRepository repository;
    private final PatientMapper mapper;
    private final SlaLevelService slaLevel;
    private final OutboxService outboxService;

    public Mono<PatientRs> addPatientInformation(final PatientRq request){
        log.info("add information of the patient");
        return Mono.fromSupplier(() -> {
                    Patient patient = mapper.toEntity(request);
                    patient.setSlaLevel(slaLevel.getLevel(request));
                    patient.setRegistrationDate(OffsetDateTime.now(ZoneOffset.UTC));
                    patient.setPatientStatus(PatientStatus.REGISTRADO);
                    return patient;
                })
                .flatMap(repository::save)
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
