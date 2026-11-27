-- 1. agregamos la columna para guarda fecha de cambio de consulta
ALTER TABLE patient ADD COLUMN consultation_date TIMESTAMPTZ;

-- 2. Funcion del trigger
CREATE OR REPLACE FUNCTION set_consultation_date()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        -- Si ya tenia fecha, se conserva siempre (inmutable), aunque la app mande NULL
        IF OLD.consultation_date IS NOT NULL THEN
            NEW.consultation_date := OLD.consultation_date;
            RETURN NEW;
        END IF;
    END IF;

    -- Primera entrada a consulta
    IF NEW.patient_status = 'EN_CONSULTA' AND NEW.consultation_date IS NULL THEN
        NEW.consultation_date := now();
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3. Trigger
CREATE TRIGGER trg_patient_consultation_date
    BEFORE INSERT OR UPDATE OF patient_status ON patient
    FOR EACH ROW
    EXECUTE FUNCTION set_consultation_date();

-- 4. Vista con los agregados por hora (todo el historico, sin window functions)
CREATE OR REPLACE VIEW v_hourly_operation AS
WITH base AS (
    SELECT date_trunc('hour', registration_date) AS hora,
           sla_level,
           EXTRACT(EPOCH FROM (consultation_date - registration_date)) / 60.0 AS wait_min
    FROM patient
)
SELECT hora,
       COUNT(*)                                           AS total,
       COUNT(*) FILTER (WHERE sla_level = 'CRITICO')      AS critico,
       COUNT(*) FILTER (WHERE sla_level = 'URGENTE')      AS urgente,
       COUNT(*) FILTER (WHERE sla_level = 'URGENTE_2')    AS urgente_2,
       COUNT(*) FILTER (WHERE sla_level = 'NO_URGENTE')   AS no_urgente,
       COUNT(*) FILTER (WHERE sla_level = 'NO_URGENTE_2') AS no_urgente_2,
       ROUND(AVG(wait_min)::numeric, 2)                   AS avg_wait,
       ROUND((percentile_cont(0.95)
              WITHIN GROUP (ORDER BY wait_min))::numeric, 2) AS p95_wait
FROM base
GROUP BY hora;