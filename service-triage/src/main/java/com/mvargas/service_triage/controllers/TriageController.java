package com.mvargas.service_triage.controllers;

import com.mvargas.service_triage.models.response.PatientRs;
import com.mvargas.service_triage.services.TriageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("triage")
@RequiredArgsConstructor
public class TriageController {

    private final TriageService service;

    @PostMapping(path = "pacientes")
    public Mono<ResponseEntity<PatientRs>> addPatient() {
        return service.addPatientInformation()
                .map(ResponseEntity::ok);
    }
}
