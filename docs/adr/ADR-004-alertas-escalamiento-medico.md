# ADR-004: Arquitectura para el Sistema de Alertas de Escalamiento al Equipo Médico (RF-06)

* **Estado:** Aceptado
* **Fecha:** 2026-11-27
* **Contexto / Requerimiento:** RF-06 - Alertas de escalamiento al equipo médico.

## 1. Contexto y Problema
El sistema debe notificar al equipo médico cada vez que un paciente entra en estado de escalamiento. El requerimiento funcional deja abiertos el canal de comunicación, el formato del mensaje y la tecnología de entrega.

Actualmente, el flujo de escalamiento actualiza el estado del paciente en la base de datos y publica un evento en un tópico compartido de Apache Kafka (`triaje.eventos`), donde conviven distintos tipos de eventos diferenciados por el atributo `eventType`. Además, ya existe un consumidor activo en dicho tópico encargado de procesos de lectura y agregación.

Se requiere una solución desacoplada, resiliente y fácil de probar que no interfiera con los consumidores existentes ni bloquee el flujo principal de escalamiento.

## 2. Decisión
Se implementa una **arquitectura reactiva dirigida por eventos (Event-Driven)** mediante un nuevo consumidor de Kafka independiente, aplicando los siguientes patrones:

1. **Aislamiento mediante Consumer Group dedicado:** Se crea un consumidor reactivo suscrito al tópico `triaje.eventos` con un `group.id` exclusivo (`group-alert-scaled`). Esto garantiza que la lectura de eventos ocurra en paralelo sin competir ni alterar los *offsets* del consumidor existente.
2. **Filtrado en memoria (Message Filter Pattern):** Dado que el tópico es multipropósito, el flujo reactivo aplica un predicado (`Predicate<PatientEvent>`) seguro contra valores nulos para procesar únicamente los eventos cuyo `eventType` corresponda a `paciente_escalado`, descartando el resto sin detener el *commit* de los mensajes.
3. **Integración con Servicio Externo (Mock con WebClient):** Para el envío de la notificación (correo/SMS), se desacopla la lógica mediante un cliente HTTP reactivo (`WebClient`). Para efectos de esta prueba técnica, se inyecta un `ExchangeFunction` simulado (*mock*) que emula respuestas exitosas (`200 OK`) y escenarios de fallo controlado (`500 Internal Server Error` en cada tercer intento) para validar la resiliencia del sistema.
4. **Tolerancia a fallos y Logs Estructurados:** Se incorpora manejo de errores reactivo (`onErrorResume`) para evitar que un fallo en el proveedor externo cancele la suscripción del consumidor de Kafka. Cada etapa (recepción, envío exitoso o fallo) emite logs estructurados para facilitar la observabilidad y auditoría.

## 3. Opciones Consideradas y Evolución Futura (Trade-offs)

* **Persistencia en Base de Datos (Opcional / Preparado para escalar):** Aunque el alcance actual prioriza la notificación externa simulada y la trazabilidad por logs, la arquitectura permite conectar fácilmente un repositorio reactivo (R2DBC) dentro del mismo flujo para almacenar un historial de alertas (`tabla alertas` con estados `ENVIADA` / `FALLIDA`) y exponer un endpoint de consulta (`GET /api/alertas`) para un futuro panel médico.
* **Creación de un tópico exclusivo vs. Tópico compartido:** Se descartó crear un nuevo tópico en esta fase para reutilizar la infraestructura existente del productor sin introducir cambios en el contrato actual. En un entorno productivo de alto volumen, se recomienda migrar los eventos críticos a un tópico dedicado o filtrar mediante *Kafka Headers* para evitar la deserialización innecesaria de eventos no relevantes.

## 4. Consecuencias

### Positivas
* **Desacoplamiento total:** Una caída o latencia en el servicio de notificaciones no afecta el guardado del escalamiento del paciente.
* **Cero dependencias externas en pruebas:** El uso de un `WebClient` con `ExchangeFunction` simulado permite probar flujos felices y manejo de errores sin necesidad de credenciales reales ni servicios de terceros.
* **Extensibilidad:** Reemplazar el *mock* por un proveedor real (ej. Twilio, AWS SES, SendGrid) solo requiere apuntar la configuración del `WebClient` a la URL productiva sin modificar la lógica de negocio.

### Negativas / Riesgos
* **Consumo de red y CPU en el consumidor:** Al leer de un tópico compartido, el servicio recibe y deserializa mensajes que luego descarta. Este impacto es despreciable para el volumen actual, pero deberá monitorearse si el tráfico crece significativamente.
