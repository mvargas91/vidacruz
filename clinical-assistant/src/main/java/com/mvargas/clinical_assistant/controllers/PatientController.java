package com.mvargas.clinical_assistant.controllers;

import com.mvargas.clinical_assistant.enums.PatientStatus;
import com.mvargas.clinical_assistant.models.entities.Patient;
import com.mvargas.clinical_assistant.models.request.PatientStatusRq;
import com.mvargas.clinical_assistant.models.response.PatientRs;
import com.mvargas.clinical_assistant.services.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @PatchMapping("/pacientes/{id}/estado")
    public Mono<ResponseEntity<Patient>> updateStatus(@PathVariable Long id,
                                                      @Valid @RequestBody PatientStatusRq request) {
        return service.updatePatientStatus(id, PatientStatus.valueOf(request.status()))
                .map(ResponseEntity::ok);
    }
}
