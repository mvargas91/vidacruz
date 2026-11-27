package com.mvargas.clinical_assistant.enums;

import java.util.Arrays;

/** Eventos del topic triaje.eventos. */
public enum TipoEvento {
    REGISTRADO("paciente_registrado"),
    EN_CONSULTA("paciente_en_consulta"),
    ESCALADO("paciente_escalado"),
    ALTA("paciente_alta");

    private final String nombre;

    TipoEvento(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return nombre;
    }

    public static TipoEvento fromString(final String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value.trim()))
                .findFirst().orElse(REGISTRADO);
    }
}
