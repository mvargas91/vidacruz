package com.mvargas.clinical_assistant.models.entities;

import com.mvargas.clinical_assistant.enums.PatientStatus;
import com.mvargas.clinical_assistant.enums.SlaLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Table(name = "patient")
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    private Long id;

    @Column(value = "full_name")
    private String fullName;

    @Column(value = "age")
    private Integer age;

    @Column(value = "heart_rate")
    private Integer heartRate;

    @Column(value = "systolic_pressure")
    private Integer systolicPressure;

    @Column(value = "diastolic_pressure")
    private Integer diastolicPressure;

    @Column(value = "sp_o_two")
    private Integer spOTwo;

    @Column(value = "temperature")
    private BigDecimal temperature;

    @Column(value = "ache")
    private Integer ache;

    @Column(value = "gcs")
    private Integer gcs;

    @Column(value = "description")
    private String description;

    @Column(value = "sla_level")
    private SlaLevel slaLevel;

    @Column(value = "patient_status")
    private PatientStatus patientStatus;

    @Column(value = "registration_date")
    private OffsetDateTime registrationDate;

    @Column("escalated")
    private boolean escalated = false;

    @Column("escalated_date")
    private OffsetDateTime escalatedDate;
}
