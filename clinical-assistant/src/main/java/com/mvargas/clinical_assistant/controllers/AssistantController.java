package com.mvargas.clinical_assistant.controllers;

import com.mvargas.models.response.EmergencyStatusRs;
import com.mvargas.clinical_assistant.services.AssistantService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/asistente")
@AllArgsConstructor
public class AssistantController {

    private final AssistantService service;

    @GetMapping("/estado-urgencias")
    public Mono<EmergencyStatusRs> getEmergencyStatusRs() {
        return service.getStatus();
    }
}
