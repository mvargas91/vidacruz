# ADR-003: Monitoreo de SLA y escalamiento automático con scheduler y UPDATE condicional (RF-03)

- **Estado:** Aceptada
- **Fecha:** 2026-11-27
- **Requerimiento:** RF-03 (monitoreo de SLA y escalamiento), RN-02, RN-03, RN-04
- **Servicio:** Triage

## Contexto

Cada paciente tiene un SLA según su nivel de triaje (CRITICO 5 min, URGENTE 10, URGENTE_2 30, NO_URGENTE 60, NO_URGENTE_2 120): el tiempo máximo entre el registro y la entrada a consulta. Si un paciente en estado `REGISTRADO` supera su SLA, el sistema debe:

1. Marcarlo como escalado (flag persistente: `escalated` y `escalated_date` en `patient`).
2. Publicar el evento `paciente_escalado` en `triaje.eventos`.
3. Registrar la auditoría (RN-04).

Restricciones del enunciado:

- **RN-03:** un paciente se escala una sola vez, sin eventos ni auditoría duplicados. Un paciente en `EN_CONSULTA` o `ALTA` nunca debe escalarse.
- El intervalo de evaluación y los SLA deben ser configurables, para probar el escalamiento sin esperar decenas de minutos.
- El servicio debe ser reactivo, sin bloquear el event loop.
- Debe funcionar con varias instancias del servicio corriendo a la vez.

## Decisión

Un **proceso programado** (`SlaMonitor`, `@Scheduled` con `fixedDelay`, devolviendo `Mono<Void>`) evalúa periódicamente a los pacientes activos y escala a los vencidos mediante un **`UPDATE` condicional atómico**. En la misma transacción se registra la auditoría y se encola el evento en la tabla outbox (ver ADR-001).

### Flujo de cada ciclo

Todo ocurre en una sola transacción:

1. **Escalar con un UPDATE condicional** que devuelve los pacientes realmente escalados:

```sql
WITH vencidos AS (
    SELECT id
    FROM patient
    WHERE patient_status = 'REGISTRADO'
      AND escalated = FALSE
      AND registration_date + (<minutos del nivel> * INTERVAL '1 minute') <= :now
    ORDER BY registration_date
    LIMIT :batch
    FOR UPDATE SKIP LOCKED
)
UPDATE patient p
SET escalated = TRUE,
    escalated_date = :now
FROM vencidos v
WHERE p.id = v.id
RETURNING p.id, p.sla_level;
```

2. **Auditoría:** por cada fila devuelta se inserta un registro (timestamp, id del paciente, estado anterior y nuevo, tipo de evento `ESCALAMIENTO`).
3. **Evento:** por cada fila devuelta se inserta un `paciente_escalado` en `outbox_event`. El `OutboxRelay` lo publica en Kafka.

Los minutos por nivel se pasan como parámetros enlazados (por ejemplo con un `CASE sla_level WHEN ... THEN ...`), tomados de las propiedades de la aplicación. El cálculo del vencimiento se hace en la base de datos y no recorriendo pacientes en memoria.

### Configuración

```yaml
triage:
  sla:
    evaluation-interval-ms: 30000   # cada cuánto se evalúa
    batch-size: 100                 # máximo de pacientes escalados por ciclo
    minutes:
      CRITICO: 5
      URGENTE: 10
      URGENTE_2: 30
      NO_URGENTE: 60
      NO_URGENTE_2: 120
```

Para pruebas locales se pueden bajar los minutos a valores de segundos o de 1 minuto, y el intervalo a 1-2 segundos. Los valores por defecto son los de la RN-02.

### Cómo se cumple la RN-03

- **Una sola vez:** la condición `escalated = FALSE` va dentro del `UPDATE`. Si el paciente ya está escalado, no se actualiza, no devuelve fila y por tanto no se genera ni auditoría ni evento. La idempotencia está garantizada por la base de datos, no por lógica en memoria.
- **Nunca en consulta ni alta:** la condición `patient_status = 'REGISTRADO'` excluye esos estados.
- **Carrera con la transición a `EN_CONSULTA`:** si un médico pasa al paciente a consulta justo mientras corre el monitor, ambas operaciones compiten por el bloqueo de la fila. La que llega segunda reevalúa la condición y no actúa si ya no aplica. No hay escalamiento de un paciente ya en consulta.
- **Varias instancias:** `FOR UPDATE SKIP LOCKED` hace que cada instancia tome pacientes distintos. No se necesita un lock distribuido ni un líder.

## Alternativas consideradas

| Alternativa | Por qué se descartó |
|---|---|
| **Un temporizador por paciente** (`Mono.delay`, `ScheduledExecutor` en memoria) | Se pierde al reiniciar el servicio y no escala con varias instancias. Requiere reconstruir los timers al arrancar. |
| **Mensajes diferidos en Kafka / topic de reintento** | Kafka no tiene entrega diferida nativa. Añade complejidad y topics extra para algo que una consulta indexada resuelve. |
| **Quartz o ShedLock** | Añaden dependencias y tablas. Con `SKIP LOCKED` ya no hace falta coordinar quién ejecuta. |
| **Cargar los pacientes activos y evaluarlos en memoria** | Costoso con volumen alto y no es atómico (dos instancias podrían escalar al mismo paciente). Contradice el criterio de calcular en la base de datos. |
| **Consumidor Kafka que evalúa el SLA** | Un consumidor reacciona a eventos, y el vencimiento de un SLA es la ausencia de un evento. Necesitaría igualmente un reloj. |
| **Trigger o job en PostgreSQL (`pg_cron`)** | Aleja la regla de negocio del código, es más difícil de probar y no encaja con el despliegue en Azure sin extensiones adicionales. |

## Consecuencias

**Positivas**

- Idempotencia y exclusión de estados garantizadas por una única sentencia atómica.
- Sobrevive a reinicios: el estado vive en la base de datos y el siguiente ciclo retoma lo pendiente.
- Escala horizontalmente sin coordinación.
- Auditoría y evento son consistentes con el flag, por la misma transacción y el patrón outbox.
- Intervalo y SLA configurables, lo que facilita las pruebas de integración.

**Negativas / a tener en cuenta**

- **Precisión limitada por el intervalo:** un paciente puede escalarse hasta `evaluation-interval-ms` después de vencer su SLA. Con 30 s de intervalo, es aceptable frente a un SLA mínimo de 5 minutos. Si se requiere más precisión, se reduce el intervalo.
- **Consulta periódica:** se recomienda un índice parcial para mantenerla barata:

```sql
CREATE INDEX idx_patient_active_not_escalated
    ON patient (registration_date)
    WHERE patient_status = 'REGISTRADO' AND escalated = FALSE;
```

- **Backlog:** si hay más vencidos que `batch-size`, se escalan por lotes en ciclos consecutivos.
- **Reloj:** se usa la hora del servicio como parámetro (`:now`) y no `now()` de la base de datos, lo que facilita las pruebas con un reloj fijo. En producción, las instancias deben tener la hora sincronizada.
- **Reglas duplicadas:** los minutos están en el enum `SlaLevel` y en la configuración. La configuración tiene prioridad, y el enum aporta los valores por defecto.

## Métricas y observabilidad

- Contador `triage.escalations` (Micrometer), con la etiqueta de nivel.
- Duración de cada ciclo y número de pacientes escalados por ciclo.
- Log estructurado por escalamiento, con el id del paciente y el nivel.

## Relación con otros requerimientos

- **RF-06 (alertas al equipo médico):** consume el evento `paciente_escalado` publicado por este flujo. La decisión del canal se documenta en un ADR aparte.
- **RF-04 Parte B:** el Asistente actualiza su vista de urgencias con los eventos de `triaje.eventos`, de forma idempotente según `eventId`.

## Referencias

- ADR-001: Transactional Outbox para publicar eventos
- PostgreSQL: `SELECT ... FOR UPDATE SKIP LOCKED`
- Spring Framework 6.1: `@Scheduled` con tipos reactivos
