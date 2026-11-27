package com.mvargas.clinical_assistant.repositories;

import com.mvargas.clinical_assistant.components.SlaPolicy;
import com.mvargas.clinical_assistant.enums.SlaLevel;
import com.mvargas.clinical_assistant.models.request.EscalatedPatient;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class EscalationRepository {

    private static final String SQL = """
            UPDATE patient
               SET escalated = true, escalated_date = now()
             WHERE patient_status = 'REGISTRADO'
               AND escalated = false
               AND registration_date + make_interval(secs => CASE sla_level
                       WHEN 'CRITICO'      THEN :critico
                       WHEN 'URGENTE'      THEN :urgente
                       WHEN 'URGENTE_2'    THEN :urgente2
                       WHEN 'NO_URGENTE'   THEN :noUrgente
                       ELSE :noUrgente2 END) <= now()
            RETURNING id, sla_level, patient_status
            """;

    private final DatabaseClient db;
    private final SlaPolicy slaPolicy;

    public EscalationRepository(final DatabaseClient db,
                                final SlaPolicy slaPolicy) {
        this.db = db;
        this.slaPolicy = slaPolicy;
    }

    public Flux<EscalatedPatient> markOverdue() {
        return db.sql(SQL)
                .bind("critico",    seconds(SlaLevel.CRITICO))
                .bind("urgente",    seconds(SlaLevel.URGENTE))
                .bind("urgente2",   seconds(SlaLevel.URGENTE_2))
                .bind("noUrgente",  seconds(SlaLevel.NO_URGENTE))
                .bind("noUrgente2", seconds(SlaLevel.NO_URGENTE_2))
                .map(row -> new EscalatedPatient(
                        row.get("id", Long.class),
                        row.get("sla_level", String.class),
                        row.get("patient_status", String.class))
                )
                .all();
    }

    private double seconds(SlaLevel level) {
        return slaPolicy.of(level).toSeconds();
    }
}
