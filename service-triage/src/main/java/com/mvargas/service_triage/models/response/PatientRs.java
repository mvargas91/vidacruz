package com.mvargas.service_triage.models.response;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder(toBuilder = true)
public record PatientRs (Long id,
                         String level,
                         String slaMinutes,
                         String state,
                         OffsetDateTime registrationDate) {}
