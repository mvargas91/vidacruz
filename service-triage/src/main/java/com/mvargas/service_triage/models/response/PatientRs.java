package com.mvargas.service_triage.models.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record PatientRs (Long id,
                         String level,
                         String slaMinutes,
                         Boolean state,
                         LocalDateTime registrationDate) {}
