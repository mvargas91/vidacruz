package com.mvargas.clinical_assistant.enums;

import lombok.Getter;

@Getter
public enum EventType {
    PACIENTE_REGISTRADO("paciente_registrado"),
    PACIENTE_EN_CONSULTA("paciente_en_consulta"),
    PACIENTE_ESCALADO("paciente_escalado"),
    PACIENTE_ALTA("paciente_alta");

    private final String valor;

    EventType(String valor) { this.valor = valor; }

    public static EventType byState(final PatientStatus status) {
        return switch (status) {
            case REGISTRADO  -> PACIENTE_REGISTRADO;
            case EN_CONSULTA -> PACIENTE_EN_CONSULTA;
            case ALTA        -> PACIENTE_ALTA;
            case ESCALADO    -> PACIENTE_ESCALADO;
        };
    }
}
