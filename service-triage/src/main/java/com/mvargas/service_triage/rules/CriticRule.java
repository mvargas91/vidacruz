package com.mvargas.service_triage.rules;

import com.mvargas.enums.SlaLevel;
import com.mvargas.models.request.PatientRq;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(1)
public class CriticRule implements SlaRule {
    @Override
    public Optional<SlaLevel> evaluate(final PatientRq request) {
        boolean match = request.getHeartRate() < 40
                || request.getHeartRate() > 120
                || request.getSpOTwo() < 90
                || request.getSystolicPressure() < 90
                || request.getGcs() <= 8;
        return match ? Optional.of(SlaLevel.CRITICO) : Optional.empty();
    }
}
