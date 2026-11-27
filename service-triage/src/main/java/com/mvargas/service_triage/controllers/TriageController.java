package com.mvargas.service_triage.controllers;

import com.mvargas.models.request.PatientRq;
import com.mvargas.models.response.PatientRegisterRs;
import com.mvargas.models.response.PatientRs;
import com.mvargas.service_triage.services.TriageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("triage")
@RequiredArgsConstructor
public class TriageController {

    private final TriageService service;

    @PostMapping(path = "pacientes")
    public Mono<ResponseEntity<PatientRegisterRs>> addPatient(@Valid @RequestBody final PatientRq request) {
        return service.addPatientInformation(request)
                .map(ResponseEntity::ok);
    }
}
