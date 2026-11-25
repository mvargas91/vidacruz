package com.mvargas.service_triage.rules;

import com.mvargas.service_triage.enums.SlaLevel;
import com.mvargas.service_triage.models.request.PatientRq;

import java.util.Optional;

public interface SlaRule {
    Optional<SlaLevel> evaluate(PatientRq request);
}
