package com.mvargas.service_triage.services;

import com.mvargas.service_triage.enums.SlaLevel;
import com.mvargas.service_triage.models.request.PatientRq;
import com.mvargas.service_triage.rules.SlaRule;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SlaLevelService {

    private final List<SlaRule> rules;

    public SlaLevelService(List<SlaRule> rules) {
        this.rules = rules;
    }

    public SlaLevel getLevel(final PatientRq request) {
        return rules.stream()
                .map(rule -> rule.evaluate(request))
                .flatMap(Optional::stream)
                .findFirst()
                .orElse(SlaLevel.NO_URGENTE);
    }
}
