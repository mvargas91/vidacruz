package com.mvargas.service_triage.repositories;

import com.mvargas.models.entities.PatientAudit;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface PatientAuditRepository extends R2dbcRepository<PatientAudit, Long> {
}
