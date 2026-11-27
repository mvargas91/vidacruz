package com.mvargas.clinical_assistant.services;

import com.mvargas.clinical_assistant.enums.NivelTriaje;
import com.mvargas.clinical_assistant.models.response.EmergencyStatusRs;
import com.mvargas.clinical_assistant.models.response.TriageEvent;
import com.mvargas.clinical_assistant.repositories.EmergencyViewRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class AssistantService {

    private final EmergencyViewRepository repository;

    public Mono<Void> apply(final TriageEvent triageEvent) {
        return switch (triageEvent.status()) {
            case REGISTRADO, EN_CONSULTA -> repository.registrar(String.valueOf(triageEvent.patientId()), triageEvent.slaLevel()).then();
            case ALTA -> repository.darAlta(String.valueOf(triageEvent.patientId())).then();
            case ESCALADO -> Mono.empty();
        };
    }

    public Mono<EmergencyStatusRs> getStatus() {
        log.info("iniciamos la consulta de los triajes");
        return repository.countByLevel().map(dataCount -> {
            Map<String, Long> byLevel = new LinkedHashMap<>();
            long total = 0;
            for (NivelTriaje n : NivelTriaje.values()) {
                long value = dataCount.getOrDefault(n, 0L);
                byLevel.put(n.name(), value);
                total += value;
            }
            return new EmergencyStatusRs(total, byLevel, Instant.now());
        });
    }
}
