package com.mvargas.clinical_assistant.controllers;

import com.mvargas.clinical_assistant.models.response.PatientRs;
import com.mvargas.clinical_assistant.services.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("triage")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService service;

    @GetMapping(path = "/pacientes/{id}")
    public Mono<ResponseEntity<PatientRs>> addPatient(@PathVariable Long id) {
        return service.getPatientById(id)
                .map(ResponseEntity::ok);
    }
}
