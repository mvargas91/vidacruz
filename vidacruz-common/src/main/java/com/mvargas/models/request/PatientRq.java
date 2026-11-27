package com.mvargas.models.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PatientRq {

    @NotBlank(message = "FulName is required")
    @Size(min = 3, max = 100, message = "FulName must be between 3 and 100 characters")
    private String fullName;

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age size must be at least 0")
    @Max(value = 120, message = "Age size cannot exceed 120")
    private Integer age;

    @NotNull(message = "Heart Rate is required")
    @Min(value = 20, message = "Heart Rate size must be at least 20 bpm")
    @Max(value = 250, message = "Heart Rate size cannot exceed 250 bpm")
    private Integer heartRate;

    @NotNull(message = "Systolic Pressure is required")
    @Min(value = 60, message = "Systolic Pressure size must be at least 60 mmHg")
    @Max(value = 260, message = "Systolic Pressure size cannot exceed 260 mmHg")
    private Integer systolicPressure;

    @NotNull(message = "Diastolic Pressure is required")
    @Min(value = 30, message = "Diastolic Pressure size must be at least 30 mmHg")
    @Max(value = 160, message = "Diastolic Pressure size cannot exceed 160 mmHg")
    private Integer diastolicPressure;

    @NotNull(message = "SpO2 is required")
    @Min(value = 70, message = "SpO2 size must be at least 70 %")
    @Max(value = 100, message = "SpO2 Pressure size cannot exceed 100 %")
    private Integer spOTwo;

    @NotNull(message = "Temperature is required")
    @DecimalMin(value = "34.0", message = "The minimum permitted temperature is 34.0 °C")
    @DecimalMax(value = "42.0", message = "The maximum permitted temperature is 42.0 °C")
    @Digits(integer = 2, fraction = 2, message = "Invalid format (example: 36.5 o 36.55)")
    private BigDecimal temperature;

    @NotNull(message = "Ache is required")
    @Min(value = 0, message = "Ache size must be at least 0")
    @Max(value = 10, message = "Ache Pressure size cannot exceed 10")
    private Integer ache;

    @NotNull(message = "GCS is required")
    @Min(value = 3, message = "GCS size must be at least 3")
    @Max(value = 15, message = "GCS Pressure size cannot exceed 15")
    private Integer gcs;

    private String description;
}
