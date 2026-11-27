package com.mvargas.service_triage.models.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public record HourlyOperationReportRs(
        OffsetDateTime hora,
        long totalPacientes,
        Map<String, Long> porNivel,        // CRITICO, URGENTE, URGENTE_2, NO_URGENTE, NO_URGENTE_2
        BigDecimal esperaPromedioMin,      // null si nadie entró a consulta en esa hora
        BigDecimal esperaP95Min,
        long acumulado,                    // window: SUM() OVER
        long rankCarga) {}                 // window: RANK() OVER (1 = hora más cargada)
