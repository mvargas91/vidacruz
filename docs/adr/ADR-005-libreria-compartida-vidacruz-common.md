# ADR-005: Creación de Librería Compartida (`vidacruz-common`) para Modelos y Contratos de Eventos

* **Estado:** Aceptado
* **Fecha:** 2026-11-27
* **Contexto / Componentes afectados:** `clinical_assistant`, `service_triage`, `vidacruz-common`.

## 1. Contexto y Problema
La arquitectura del sistema está compuesta por múltiples microservicios reactivos construidos con Spring WebFlux (`clinical_assistant` y `service_triage`) que se comunican de manera asíncrona a través de Apache Kafka y persisten información utilizando Spring Data R2DBC.

Ambos servicios requieren manipular las mismas entidades de dominio (mapeadas con anotaciones relacionales como `@Table` y `@Column`), reglas de validación (`jakarta.validation`) y contratos de mensajes/eventos (`PatientEvent`, `TipoEvento`). Mantener copias duplicadas de estas clases en cada repositorio genera:
* Riesgo de desincronización en los esquemas de serialización/deserialización (JSON) de Kafka.
* Duplicación de código y mayor esfuerzo de mantenimiento ante cambios en el modelo de datos.
* Inconsistencias en las reglas de validación de entrada entre servicios.

## 2. Decisión
Se decide extraer las entidades, enumeraciones, validaciones y DTOs de eventos compartidos hacia un módulo independiente empaquetado como librería Java estándar bajo el artefacto **`com.mvargas:vidacruz-common`**, siguiendo estos lineamientos técnicos:

1. **Empaquetado como `java-library` sin servidor embebido:** Se configura con Gradle usando los plugins `java-library` y `maven-publish` sobre **Java 25**, evitando dependencias de arranque de Spring Boot (`bootJar`) para mantenerla ligera y reutilizable.
2. **Exposición controlada de dependencias (`api`):** Se exponen únicamente las anotaciones necesarias para el mapeo relacional reactivo (`spring-data-relational`, `spring-data-commons`), validaciones (`jakarta.validation-api`) y serialización (`jackson-annotations`), utilizando versiones parcheadas contra vulnerabilidades conocidas (CVEs) y Lombok (`1.18.48`) compatible con Java 25.
3. **Distribución y Versionamiento:** Se publica mediante `publishToMavenLocal` en el repositorio local (`~/.m2`) para desarrollo y pruebas, permitiendo que ambos microservicios consuman el artefacto declarando `mavenLocal()` y la dependencia `com.mvargas:vidacruz-common:1.0.0`.
4. **Espacio de nombres neutral:** Todas las clases residen bajo el paquete base `com.mvargas.vidacruz_common`, desacoplando el dominio compartido de los paquetes específicos de cada microservicio.

## 3. Opciones Consideradas

* **Duplicación de clases (Copy-Paste entre repositorios):** Descartada porque viola el principio DRY y aumenta la probabilidad de errores en tiempo de ejecución si un microservicio modifica un campo del evento de Kafka sin actualizar al consumidor.
* **Monorepo multi-módulo de Gradle:** Descartada para mantener la independencia de ciclo de vida y despliegue de cada repositorio en la prueba técnica, permitiendo además migrar `vidacruz-common` fácilmente a un registro privado (como GitHub Packages, Nexus o AWS CodeArtifact) en un entorno productivo.

## 4. Consecuencias

### Positivas
* **Consistencia de contratos (Schema Governance):** Garantiza que el productor y el consumidor de Kafka utilicen exactamente la misma estructura y tipos de datos.
* **Reutilización y mantenibilidad:** Cualquier ajuste en una entidad o regla de validación (`@NotNull`, `@Column`) se realiza en un único lugar y se propaga incrementando la versión de la librería.
* **Seguridad y compatibilidad:** Centraliza el control de versiones seguras de las dependencias de modelos y asegura compatibilidad con el compilador de Java 25.

### Negativas / Riesgos
* **Acoplamiento a nivel de modelo:** Un cambio disruptivo (*breaking change*) en `vidacruz-common` obliga a recompilar y actualizar la versión en ambos microservicios. Se mitiga aplicando versionamiento semántico (`SemVer`) y manteniendo retrocompatibilidad en los eventos.
* **Dependencia de repositorio local en desarrollo:** Requiere ejecutar `./gradlew publishToMavenLocal` antes de compilar los microservicios en un entorno nuevo hasta que se configure un repositorio de artefactos remoto.

---
*This is for informational purposes only. For medical advice or diagnosis, consult a professional. AI responses may include mistakes.*
