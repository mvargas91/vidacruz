ALTER TABLE patient
    ADD COLUMN registration_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN sla_level         VARCHAR(13),
    ADD COLUMN patient_status    VARCHAR(13);

COMMENT ON COLUMN patient.registration_date IS 'Fecha y hora de registro (UTC)';
COMMENT ON COLUMN patient.sla_level         IS 'Nivel de SLA';
COMMENT ON COLUMN patient.patient_status    IS 'Estado del paciente';

ALTER TABLE patient
    ADD CONSTRAINT chk_patient_status
    CHECK (patient_status IN ('REGISTRADO', 'EN_CONSULTA', 'ALTA'));

ALTER TABLE patient
    ADD CONSTRAINT chk_sla_level
    CHECK (sla_level IN ('CRITICO', 'URGENTE', 'URGENTE_2', 'NO_URGENTE', 'NO_URGENTE_2'));

ALTER TABLE patient
    ADD COLUMN escalated      BOOLEAN     NOT NULL DEFAULT false,
    ADD COLUMN escalated_date TIMESTAMPTZ;

CREATE INDEX idx_patient_pending_sla
    ON patient (registration_date)
    WHERE patient_status = 'REGISTRADO' AND escalated = false;