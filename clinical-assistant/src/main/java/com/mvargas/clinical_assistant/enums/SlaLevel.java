package com.mvargas.clinical_assistant.enums;

import lombok.Getter;

import java.util.Arrays;

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

    public static int getMinutesByLevel(final String level) {
        return Arrays.stream(values())
                .filter(sla -> sla.level.equalsIgnoreCase(level.trim()))
                .mapToInt(SlaLevel::getMinutes)
                .findFirst()
                .orElse(120);
    }
}