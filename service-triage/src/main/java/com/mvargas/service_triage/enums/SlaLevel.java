package com.mvargas.service_triage.enums;

import lombok.Getter;

@Getter
public enum SlaLevel {

    CRITICO("CRITICO", 5),
    URGENTE("URGENTE", 10),
    URGENTE_2("URGENTE_2", 30),
    NO_URGENTE("NO_URGENTE", 60),
    NO_URGENTE_2("NO_URGENTE_2", 120);

    private final String level;
    private final int minutes;

    SlaLevel(String level, int minutes) {
        this.level = level;
        this.minutes = minutes;
    }
}