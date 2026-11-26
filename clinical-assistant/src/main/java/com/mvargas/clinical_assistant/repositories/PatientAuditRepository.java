package com.mvargas.clinical_assistant.repositories;

import com.mvargas.clinical_assistant.models.entities.PatientAudit;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface PatientAuditRepository extends R2dbcRepository<PatientAudit, Long> {
}
