package com.mvargas.service_triage.rules;

import com.mvargas.service_triage.enums.SlaLevel;
import com.mvargas.service_triage.models.request.PatientRq;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@Order(2)
public class UrgentRule implements TriageRule {

    private static final BigDecimal FEVER_HIGH = new BigDecimal("38.5");

    @Override
    public Optional<SlaLevel> evaluate(final PatientRq request) {
        boolean match = request.getSpOTwo() >= 90 && request.getSpOTwo() <= 93
                || request.getSystolicPressure() >= 90 && request.getSystolicPressure() <= 100
                || request.getTemperature().compareTo(FEVER_HIGH) >= 0
                || request.getGcs() >= 9 && request.getGcs() <= 13;
        return match ? Optional.of(SlaLevel.URGENTE) : Optional.empty();
    }
}
