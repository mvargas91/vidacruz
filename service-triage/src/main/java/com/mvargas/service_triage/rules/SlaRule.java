package com.mvargas.service_triage.rules;

import com.mvargas.enums.SlaLevel;
import com.mvargas.models.request.PatientRq;

import java.util.Optional;

public interface SlaRule {
    Optional<SlaLevel> evaluate(PatientRq request);
}
