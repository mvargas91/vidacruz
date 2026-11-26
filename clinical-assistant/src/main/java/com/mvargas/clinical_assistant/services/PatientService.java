package com.mvargas.clinical_assistant.services;

import com.mvargas.clinical_assistant.exceptions.PatientAlreadyExistsException;
import com.mvargas.clinical_assistant.exceptions.PatientNotFoundException;
import com.mvargas.clinical_assistant.exceptions.PatientServiceException;
import com.mvargas.clinical_assistant.exceptions.PatientStatusConflictException;
import com.mvargas.clinical_assistant.mappers.PatientMapper;
import com.mvargas.clinical_assistant.models.entities.Patient;
import com.mvargas.clinical_assistant.models.entities.PatientAudit;
import com.mvargas.clinical_assistant.models.request.PatientEvent;
import com.mvargas.clinical_assistant.models.response.PatientRs;
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
    private final PatientMapper mapper;

    public Mono<PatientRs> getPatientById(final Long id){
        log.info("get information of the patient");
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new PatientNotFoundException(id)))
                .map(mapper::toResponse)
                .onErrorMap(this::translate);
    }

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
