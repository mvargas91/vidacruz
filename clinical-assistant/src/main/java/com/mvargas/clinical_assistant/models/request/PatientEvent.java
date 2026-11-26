package com.mvargas.clinical_assistant.models.request;

import com.mvargas.clinical_assistant.enums.EventType;
import com.mvargas.clinical_assistant.enums.PatientStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PatientEvent (
        String eventId,
        EventType eventType,
        Long patientId,
        PatientStatus previousState,
        PatientStatus newStatus,
        OffsetDateTime timestamp
) {
public static PatientEvent of(Long patientId, PatientStatus previousState,
                               PatientStatus newStatus, EventType eventType) {
    return new PatientEvent(UUID.randomUUID().toString(), eventType, patientId,
            previousState, newStatus, OffsetDateTime.now());
}
}