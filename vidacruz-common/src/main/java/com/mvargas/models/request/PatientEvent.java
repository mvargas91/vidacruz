package com.mvargas.models.request;

import com.mvargas.enums.PatientStatus;

import java.time.Instant;
import java.util.UUID;

public record PatientEvent (
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long patientId,
        String level,
        Integer slaMinutes,
        PatientStatus status
) {
public static PatientEvent of(String eventType,
                              Long patientId,
                              String level,
                              Integer slaMinutes,
                              PatientStatus status) {
    return new PatientEvent(
            UUID.randomUUID(),
            eventType,
            Instant.now(),
            patientId,
            level, slaMinutes, status);
}
}