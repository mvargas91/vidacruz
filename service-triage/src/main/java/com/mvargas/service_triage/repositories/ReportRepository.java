package com.mvargas.service_triage.repositories;

import com.mvargas.models.response.HourlyOperationReportRs;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ReportRepository {

    private final DatabaseClient databaseClient;

    private static final String SQL = """
        SELECT v.*,
               (SUM(total) OVER (ORDER BY hora))::bigint AS acumulado,
               RANK() OVER (ORDER BY total DESC)         AS rank_carga
        FROM v_hourly_operation v
        WHERE hora >= date_trunc('hour', CAST(:fromDate AS timestamptz))
          AND hora <  :toDate
        ORDER BY hora
        """;

    public Flux<HourlyOperationReportRs> findHourlyReport(final OffsetDateTime fromDate,
                                                          final OffsetDateTime toDate) {
        return databaseClient.sql(SQL)
                .bind("fromDate", fromDate)
                .bind("toDate", toDate)
                .map((row, meta) -> {
                    Map<String, Long> niveles = new LinkedHashMap<>();
                    niveles.put("CRITICO", row.get("critico", Long.class));
                    niveles.put("URGENTE", row.get("urgente", Long.class));
                    niveles.put("URGENTE_2", row.get("urgente_2", Long.class));
                    niveles.put("NO_URGENTE", row.get("no_urgente", Long.class));
                    niveles.put("NO_URGENTE_2", row.get("no_urgente_2", Long.class));
                    return new HourlyOperationReportRs(
                            row.get("hora", OffsetDateTime.class),
                            row.get("total", Long.class),
                            niveles,
                            row.get("avg_wait", BigDecimal.class),
                            row.get("p95_wait", BigDecimal.class),
                            row.get("acumulado", Long.class),
                            row.get("rank_carga", Long.class));
                })
                .all();
    }
}
