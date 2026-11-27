package com.mvargas.clinical_assistant.models.response;

import java.time.Instant;
import java.util.Map;

public record EmergencyStatusRs(long totalActivos, Map<String, Long> porNivel, Instant consultadoEn) {
}
