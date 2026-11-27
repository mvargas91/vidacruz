package com.mvargas.service_triage.models.request;

import com.mvargas.service_triage.enums.PatientStatus;

import java.time.Instant;
import java.util.UUID;

public record PatientEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long patientId,
        String level,
        Integer slaMinutes,
        PatientStatus status) {
}
