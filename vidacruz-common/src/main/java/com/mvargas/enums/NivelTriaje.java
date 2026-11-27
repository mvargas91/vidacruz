package com.mvargas.enums;

import java.util.Arrays;

public enum NivelTriaje {
    CRITICO(1), URGENTE(2), URGENTE_2(3), NO_URGENTE(4), NO_URGENTE_2(5);

    private final int codigo;

    NivelTriaje(int codigo) {
        this.codigo = codigo;
    }

    public static NivelTriaje fromString(final String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value.trim()))
                .findFirst().orElse(NO_URGENTE_2);
    }
}
