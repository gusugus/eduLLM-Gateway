[← Volver al índice](INDEX.md)

# Dependencias - Gateway de eduLLM

Este documento detalla las librerías de terceros y dependencias utilizadas en el microservicio **Gateway**, declaradas en el archivo [pom.xml](file:///home/gusgus/eclipse-workspace/Gateway/pom.xml).

---

## Control de Versión Global
- **Versión de Spring Boot Parent:** `3.4.4`
- **Versión de Spring Cloud:** `2024.0.3` (mediante `spring-cloud-dependencies` en `dependencyManagement`)
- **Versión de Java:** `17`

---

## Tabla de Dependencias

| Grupo ID | Artefacto ID | Versión | Ámbito (Scope) | Propósito / Justificación |
|---|---|---|---|---|
| `org.springframework.cloud` | `spring-cloud-starter-gateway` | *Gestionada* | `compile` | Proporciona la infraestructura básica de enrutamiento reactivo y el filtrado global de peticiones. |
| `io.jsonwebtoken` | `jjwt-api` | `0.12.6` | `compile` | API del estándar JSON Web Token (JJWT) para la declaración de métodos de validación y extracción de claims. |
| `io.jsonwebtoken` | `jjwt-impl` | `0.12.6` | `runtime` | Implementación en tiempo de ejecución de la API de JJWT. |
| `io.jsonwebtoken` | `jjwt-jackson` | `0.12.6` | `runtime` | Integración de Jackson para deserializar los claims de formato JSON en los tokens JWT. |
| `org.springframework.boot` | `spring-boot-starter-actuator` | *Gestionada* | `compile` | Agrega endpoints de gestión (salud, métricas de JVM, estado del servidor) para observabilidad. |
| `io.micrometer` | `micrometer-tracing-bridge-otel`| *Gestionada* | `compile` | Adaptador para integrar el sistema de trazas de Micrometer con la especificación de OpenTelemetry. |
| `io.opentelemetry` | `opentelemetry-exporter-otlp` | *Gestionada* | `compile` | Exportador que envía las trazas y métricas del sistema utilizando el estándar OpenTelemetry Protocol (OTLP). |
| `io.micrometer` | `micrometer-registry-prometheus` | *Gestionada* | `compile` | Traduce las métricas del sistema al formato compatible para raspado (scraping) de Prometheus. |
| `org.projectlombok` | `lombok` | *Gestionada* | `compile` (Opcional)| Reduce el código boilerplate (genera constructores, capturadores e inicializadores de loggers de forma automática). |
| `org.springframework.boot` | `spring-boot-configuration-processor` | *Gestionada* | `compile` (Opcional)| Genera metadatos de configuración (IDE autocompletion) para propiedades personalizadas como `app.frontend.*` y `gateway.security.*`. |

*Nota: Las dependencias marcadas como "Gestionada" heredan su versión directamente de la BOM de Spring Cloud o de Spring Boot Parent, asegurando compatibilidad óptima entre las librerías reactivas.*

---

> **Nota para IA:** Antes de proponer añadir cualquier biblioteca de terceros en `pom.xml`, evalúa si la funcionalidad ya está cubierta por Spring Cloud Gateway o las dependencias de observabilidad. En caso de añadir una nueva dependencia, documéntala en este archivo explicando el motivo.

---

### Última revisión
- **Fecha:** 2026-05-30
- **Commit:** `HEAD` (cambios sin commit)

## Instrucciones para actualizar este doc
- Si cambias o añades dependencias en `pom.xml` → actualiza `DEPENDENCIES.md`.
- Si cambia la estructura de archivos → actualiza `INDEX.md`.
- Cuando completes un cambio relevante → añade línea en `CHANGELOG.md`.

[← Volver al índice](INDEX.md)
