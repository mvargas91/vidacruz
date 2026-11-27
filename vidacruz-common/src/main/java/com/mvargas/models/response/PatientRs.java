package com.mvargas.models.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder(toBuilder = true)
public record PatientRs (Long id,
                         String fullName,
                         Integer age,
                         Integer heartRate,
                         Integer systolicPressure,
                         Integer diastolicPressure,
                         Integer spOTwo,
                         BigDecimal temperature,
                         Integer ache,
                         Integer gcs,
                         String description,
                         String level,
                         String slaMinutes,
                         String status,
                         OffsetDateTime registrationDate) {}
