package com.mvargas.clinical_assistant.enums;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum PatientStatus {
    REGISTRADO,
    EN_CONSULTA,
    ALTA;

    private Set<PatientStatus> permitted() {
        return switch (this) {
            case REGISTRADO  -> EnumSet.of(EN_CONSULTA, ALTA);
            case EN_CONSULTA -> EnumSet.of(ALTA);
            case ALTA        -> EnumSet.noneOf(PatientStatus.class);
        };
    }

    public boolean canTransitionTo(final PatientStatus newStatus) {
        return permitted().contains(newStatus);
    }
}