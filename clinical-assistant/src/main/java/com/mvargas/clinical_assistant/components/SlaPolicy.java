package com.mvargas.clinical_assistant.components;

import com.mvargas.clinical_assistant.configurations.TriageProperties;
import com.mvargas.enums.SlaLevel;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
public class SlaPolicy {

    private final Map<SlaLevel, Duration> overrides;

    public SlaPolicy(TriageProperties props) {
        this.overrides = props.sla() == null ? Map.of() : props.sla();
    }

    public Duration of(SlaLevel level) {
        return overrides.getOrDefault(level, Duration.ofMinutes(level.getMinutes()));
    }
}