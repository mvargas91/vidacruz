package com.mvargas.clinical_assistant.repositories;

import com.mvargas.clinical_assistant.models.entities.Patient;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface PatientRepository extends R2dbcRepository<Patient, Long> {
}
