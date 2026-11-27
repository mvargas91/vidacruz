# ADR-002: Reporte de operación con vista SQL y window functions en la consulta (RF-05)

- **Estado:** Aceptado
- **Fecha:** 2026-11-27
- **Contexto:** RF-05

## Contexto

El RF-05 pide un reporte agrupado por hora con total de pacientes, conteo por nivel de
triaje, tiempo promedio de espera (registro → primera consulta) y p95. Debe usar SQL
avanzado y no se permite iterar en memoria para calcular agregados. El rango
(`desde`/`hasta`) llega como parámetro del endpoint.

## Decisión

1. **Vista `v_hourly_operation`**: agrega por hora todo el histórico de `patient` con
   `COUNT(*) FILTER`, `AVG` y `percentile_cont(0.95) WITHIN GROUP`.
2. **Window functions en la consulta del repository**: `SUM(total) OVER (ORDER BY hora)`
   y `RANK() OVER (ORDER BY total DESC)` se calculan sobre `v_hourly_operation`, ya
   filtrada por el rango pedido.
3. **`consultation_date` mantenida por un trigger `BEFORE INSERT OR UPDATE`** en PostgreSQL:
   se fija una sola vez, en la primera entrada a `EN_CONSULTA`, y es inmutable después.

## Justificación

- **Una vista no recibe parámetros.** El filtro por rango debe aplicarse fuera de ella.
- **Las window functions van fuera de la vista** para que el acumulado y el ranking se
  calculen solo sobre el rango consultado. Dentro de la vista se calcularían sobre todo
  el histórico y darían resultados incorrectos.
- **La lógica de agregación vive en la BD**: es reutilizable (BI, otros servicios) y el
  código Java queda en un `DatabaseClient` que devuelve un `Flux`, sin cálculo en memoria.
- **El trigger garantiza consistencia** de la fecha de consulta sin depender de la capa de
  aplicación, ni de que cada `UPDATE` recuerde enviarla. Se conserva `OLD.consultation_date`
  para que un `save()` con la columna en `null` no la borre.

## Alternativas consideradas

| Alternativa | Por qué se descartó |
|---|---|
| SQL completo en el repository, sin vista | No es reutilizable y duplica lógica si otro consumidor necesita los agregados |
| Función `fn_operation_report(desde, hasta)` | Filtra exacto por `registration_date` y rinde mejor, pero oculta el SQL en la BD y complica el versionado. Queda como evolución si hay problemas de rendimiento |
| Vista materializada | Introduce datos desactualizados y requiere un job de refresco; el reporte es operativo |
| Calcular agregados en Java | Prohibido por el RF-05 y no escala |

## Consecuencias

- **Positivas:** SQL avanzado visible y justificado, código Java mínimo, y fecha de consulta
  coherente sin importar quién escriba en la tabla.
- **Negativas / limitaciones:**
  - El filtro se aplica sobre el bucket `hora`, no sobre `registration_date`. Un
    `desde=10:30` incluye el bucket completo de las 10:00.
  - `date_trunc('hour', ...)` no aprovecha un índice normal sobre `registration_date`. Con
    volúmenes altos conviene un índice de expresión en UTC o migrar a la función.
  - `date_trunc` sobre `timestamptz` depende de la zona horaria de la sesión: fijar
    `timezone=UTC` en la conexión.
  - Los pacientes con alta directa (`REGISTRADO → ALTA`) tienen `consultation_date` nula y
    no entran en el promedio ni en el p95 (`AVG` y `percentile_cont` ignoran nulos).