package com.mvargas.models.response;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder(toBuilder = true)
public record PatientRegisterRs (Long id,
                         String level,
                         String slaMinutes,
                         String state,
                         OffsetDateTime registrationDate) {}
