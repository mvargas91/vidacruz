# --- ETAPA 1: Compilación (Build) ---
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /workspace

# 1. Copiamos e instalamos primero la librería compartida en el ~/.m2 del contenedor
COPY vidacruz-common /workspace/vidacruz-common
WORKDIR /workspace/vidacruz-common
RUN chmod +x gradlew && ./gradlew publishToMavenLocal --no-daemon

# 2. Copiamos y compilamos el microservicio (cambia 'clinical_assistant' por 'service_triage' en el otro Dockerfile)
COPY service-triage /workspace/service-triage
WORKDIR /workspace/service-triage
RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

# --- ETAPA 2: Ejecución (Runtime ligero) ---
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copiamos únicamente el JAR compilado (que ya lleva adentro vidacruz-common)
COPY --from=builder /workspace/service-triage/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
