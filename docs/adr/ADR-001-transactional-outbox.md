# ADR-001: Transactional Outbox para publicar `paciente_registrado` (RF-01)

- **Estado:** Aceptada
- **Fecha:** 2026-11-27
- **Requerimiento:** RF-01 (registro y clasificación de pacientes)
- **Servicio:** Triage

## Contexto

Al registrar un paciente (`POST /triage/pacientes`) el servicio de Triage debe:

1. Guardar el paciente en PostgreSQL (R2DBC), ya clasificado con su nivel y SLA.
2. Publicar el evento `paciente_registrado` en el topic Kafka `triaje.eventos`.

PostgreSQL y Kafka son dos sistemas distintos y no se puede hacer una transacción distribuida entre ellos. Si se guarda el paciente y luego se publica directamente en Kafka, pueden ocurrir dos fallos:

- **Paciente guardado, evento perdido:** Kafka no responde o el servicio se cae entre ambas operaciones. El Asistente Clínico (que reconstruye el estado de urgencias desde `triaje.eventos`) nunca se entera del paciente.
- **Evento publicado, paciente no guardado:** si se publica primero y falla el commit, se emite un evento de un paciente que no existe.

Además, el enunciado exige que el servicio de Triage **nunca dependa de la disponibilidad de otros componentes**: el registro de un paciente en urgencias no puede fallar porque Kafka esté caído.

## Decisión

Se adopta el patrón **Transactional Outbox**.

1. Se crea la tabla `outbox_event`. El paciente y su evento se guardan **en la misma transacción** de base de datos (`@Transactional` sobre `create`). O se guardan ambos o ninguno.
2. Un proceso de fondo (`OutboxRelay`, `@Scheduled`) lee los eventos `PENDING`, los publica en Kafka y los marca como `PUBLISHED`.
3. La API responde al cliente en cuanto se confirma la transacción, sin esperar a Kafka.

### Tabla `outbox_event`

| Columna | Descripción |
|---|---|
| `id` | Identificador interno (identity). Define el orden de lectura. |
| `event_id` | UUID único del evento. Los consumidores lo usan para deduplicar. |
| `aggregate_type` / `aggregate_id` | Entidad de origen (`PATIENT`, id del paciente). El `aggregate_id` es la key del mensaje Kafka. |
| `event_type` | `paciente_registrado` (y a futuro `paciente_en_consulta`, `paciente_escalado`, `paciente_alta`). |
| `payload` | JSON del evento. |
| `status` | `PENDING`, `PUBLISHED` o `FAILED` (con `CHECK`). |
| `attempts` / `last_error` | Control de reintentos y diagnóstico. |
| `created_at` / `published_at` | Trazabilidad. |

Un índice parcial `WHERE status = 'PENDING'` mantiene rápida la consulta del relay aunque la tabla crezca.

### Relay

- **Lectura:** `SELECT ... WHERE status = 'PENDING' ORDER BY id LIMIT 50 FOR UPDATE SKIP LOCKED`. Con varias instancias del servicio, cada una toma lotes distintos sin duplicar envíos.
- **Orden:** los eventos se procesan con `concatMap` (secuencial). La key del mensaje es el id del paciente, así que los eventos de un mismo paciente quedan en la misma partición y en orden.
- **Envío:** `StreamBridge.send` con `producer.sync=true`, `acks=all` e idempotencia habilitada. Como la llamada es bloqueante, se ejecuta en `Schedulers.boundedElastic()` para no bloquear hilos reactivos.
- **Reintentos:** si falla, se incrementa `attempts` y se guarda `last_error`. Tras 5 intentos el evento pasa a `FAILED` para revisión manual.
- **Planificación:** `@Scheduled(fixedDelay)` devolviendo `Mono<Void>` (Spring 6.1+), sin `.block()`. El intervalo es configurable (`outbox.poll-interval-ms`).
- **Limpieza:** tarea diaria que elimina eventos `PUBLISHED` con más de 7 días.

### Contenido del evento

El payload incluye solo lo que necesitan los consumidores: `eventId`, `eventType`, `occurredAt`, `patientId`, nivel, minutos de SLA y estado. **No** se envían nombre ni signos vitales, por ser datos de salud y no necesarios para reconstruir la vista de urgencias (minimización de datos).

## Alternativas consideradas

| Alternativa | Por qué se descartó |
|---|---|
| **Publicar directo a Kafka tras guardar** | No es atómico. Pierde eventos si Kafka falla y acopla el registro del paciente a la disponibilidad de Kafka. |
| **Publicar primero y guardar después** | Puede emitir eventos de pacientes inexistentes. |
| **Transacciones Kafka + base de datos (2PC / `ChainedTransactionManager`)** | Kafka no participa en transacciones XA. Solo daría una consistencia aparente. |
| **CDC con Debezium sobre la tabla outbox** | Es la opción más robusta y de menor latencia, pero añade Kafka Connect y más infraestructura. Para el alcance de la prueba, el polling es suficiente. El diseño permite migrar a Debezium sin cambiar la tabla ni el servicio. |
| **Event sourcing completo** | Complejidad desproporcionada para este problema. |

## Consecuencias

**Positivas**

- Consistencia entre el estado del paciente y el evento: nunca se registra uno sin el otro.
- Triage sigue funcionando con Kafka caído. Los eventos se acumulan como `PENDING` y se publican al recuperarse.
- Funciona con varias instancias sin coordinación externa (`SKIP LOCKED`).
- El mismo mecanismo se reutiliza para los eventos de estado y escalamiento (RF-02, RF-03), manteniendo la auditoría coherente.

**Negativas / a tener en cuenta**

- **Entrega "al menos una vez":** si el relay publica y falla antes de marcar `PUBLISHED`, el evento se reenviará. Los consumidores deben ser **idempotentes** usando `eventId`. El Asistente debe descartar eventos ya procesados al actualizar su vista en Redis.
- **Latencia adicional:** el evento llega a Kafka con un retraso de hasta el intervalo del relay (por defecto 1 s). Es aceptable para actualizar una vista de estado.
- **Orden solo por paciente:** no hay orden global entre pacientes distintos.
- **Carga extra en PostgreSQL:** una escritura adicional por evento y consultas periódicas del relay. Se mitiga con el índice parcial y la limpieza diaria.
- **Eventos `FAILED`:** requieren monitoreo y atención manual. Se recomienda una métrica (Micrometer) con el conteo de eventos `PENDING` y `FAILED`.
- **Errores dentro de la transacción:** el manejo de errores de `create` debe relanzar las excepciones. Si las convirtiera en una respuesta normal, la transacción haría commit con el paciente guardado y sin evento.

## Referencias

- Patrón Transactional Outbox (microservices.io)
- Spring Cloud Stream: `StreamBridge`
- ADR relacionados (por documentar): formato de mensajes (JSON), modo degradado del asistente
