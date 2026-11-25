package com.mvargas.service_triage.repositories;

import com.mvargas.service_triage.models.entities.Patient;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface PatientRepository extends R2dbcRepository<Patient, Long> {
}
