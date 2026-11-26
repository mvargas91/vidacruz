package com.mvargas.clinical_assistant.configurations;

import com.mvargas.clinical_assistant.enums.SlaLevel;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

@ConfigurationProperties(prefix = "app.triage")
public record TriageProperties(Duration evaluationInterval, Map<SlaLevel, Duration> sla) {}
