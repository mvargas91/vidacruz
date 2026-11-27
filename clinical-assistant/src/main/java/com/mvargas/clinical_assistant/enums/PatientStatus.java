package com.mvargas.clinical_assistant.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

@Getter
public enum PatientStatus {
    ESCALADO,
    REGISTRADO,
    EN_CONSULTA,
    ALTA;

    public static PatientStatus fromString(final String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value.trim()))
                .findFirst().orElse(REGISTRADO);
    }

    private Set<PatientStatus> permitted() {
        return switch (this) {
            case ESCALADO, ALTA -> EnumSet.noneOf(PatientStatus.class);
            case REGISTRADO  -> EnumSet.of(EN_CONSULTA, ALTA);
            case EN_CONSULTA -> EnumSet.of(ALTA);
        };
    }

    public boolean canTransitionTo(final PatientStatus newStatus) {
        return permitted().contains(newStatus);
    }
}