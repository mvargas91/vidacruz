package com.mvargas.service_triage.services;

import com.mvargas.service_triage.models.response.PatientRs;
import com.mvargas.service_triage.mappers.PatientMapper;
import com.mvargas.service_triage.repositories.PatientRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class TriageService {

    private final PatientRepository repository;
    private final PatientMapper mapper;

    public Mono<PatientRs> addPatientInformation(){
        log.info("add information of the patient");
        return Mono.just(PatientRs.builder().build());
    }
}
