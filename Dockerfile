FROM amazoncorretto:17-alpine

# Crear usuario no-root
RUN addgroup -g 1001 -S nodegroup && \
    adduser -S nodeuser -G nodegroup -u 1001

WORKDIR /app

# Cambiar propietario
COPY --chown=nodeuser:nodegroup . .

USER nodeuser

# Copiar el JAR de la aplicación
COPY ./target/Gateway-0.0.1-SNAPSHOT.jar app.jar

# Descargar el agente OpenTelemetry (o copiarlo)
#ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

COPY ./opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

# Variables de entorno por defecto (pueden sobrescribirse en el compose)
ENV OTEL_RESOURCE_ATTRIBUTES="service.name=ms-gateway,service.namespace=eduLLM,service.version=0.0.1-SNAPSHOT,app.artifact=autenticacionWeb"
ENV OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4318"
ENV OTEL_METRICS_EXPORTER="none"
ENV OTEL_TRACES_EXPORTER="otlp"
ENV OTEL_LOGS_EXPORTER="otlp"

EXPOSE 8081

ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "app.jar"]