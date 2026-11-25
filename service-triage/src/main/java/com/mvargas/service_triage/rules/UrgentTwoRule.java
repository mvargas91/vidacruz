package com.mvargas.service_triage.rules;

import com.mvargas.service_triage.enums.SlaLevel;
import com.mvargas.service_triage.models.request.PatientRq;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@Order(3)
public class UrgentTwoRule implements SlaRule {

    private static final BigDecimal FEVER_LOW = new BigDecimal("38.0");
    private static final BigDecimal FEVER_HIGH = new BigDecimal("38.5");

    @Override
    public Optional<SlaLevel> evaluate(final PatientRq request) {
        boolean match = request.getHeartRate() >= 90 && request.getHeartRate() <= 120
                || request.getTemperature().compareTo(FEVER_LOW) >= 0
                && request.getTemperature().compareTo(FEVER_HIGH) < 0
                || request.getAche() >= 7;
        return match ? Optional.of(SlaLevel.URGENTE_2) : Optional.empty();
    }
}
