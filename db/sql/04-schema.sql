CREATE TABLE IF NOT EXISTS patient_audit (
    id             BIGSERIAL PRIMARY KEY,
    patient_id     BIGINT NOT NULL REFERENCES patient(id),
    previous_state VARCHAR(20),
    new_status    VARCHAR(20)  NOT NULL,
    event_type    VARCHAR(40)  NOT NULL,
    event_date    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_patient_audit_id ON patient_audit (patient_id, event_date);

-- Opcional: la auditoria no se puede modificar ni borrar ni desde SQL
CREATE OR REPLACE FUNCTION immutable_audit() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'La auditoria clinica es inmutable';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_immutable_audit
    BEFORE UPDATE OR DELETE ON patient_audit
    FOR EACH ROW EXECUTE FUNCTION immutable_audit();