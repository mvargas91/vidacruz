package com.mvargas.clinical_assistant.models.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PatientStatusRq(@NotBlank(message = "El estado es obligatorio")
                             @Pattern(regexp = "REGISTRADO|EN_CONSULTA|ALTA",
                                     message = "El estado debe ser REGISTRADO, EN_CONSULTA o ALTA")
                              String status
) {}
