[← Volver al índice](INDEX.md)

# Integraciones - Gateway de eduLLM

El Gateway actúa como el orquestador del tráfico de red de **eduLLM**, conectando el mundo exterior con los microservicios internos y los sistemas de telemetría y monitoreo corporativo.

---

## 1. Integraciones de Microservicios (Downstream API)

El Gateway redirige las solicitudes HTTP entrantes a tres microservicios principales. En entornos de producción o desarrollo contenerizados, estos hosts locales (`localhost`) se reemplazan típicamente por nombres de servicio de Docker Compose o Kubernetes DNS.

| ID del Servicio | URI Base por Defecto | Propósito principal | Dependencia Crítica |
|---|---|---|---|
| `auth-ms` | `http://localhost:8082` | Gestión de cuentas de usuario, autenticación, renovación y expiración de tokens. | Sí (Generador de JWT) |
| `ms-rag` | `http://localhost:8002` | Motor de Recuperación Aumentada por Generación (RAG) y consulta a modelos de lenguaje (LLM). | No |
| `ms-admin` | `http://localhost:8083` | Panel y endpoints de administración y configuración global. | No |

### Dependencia de Autenticación
Aunque no hay una llamada HTTP directa (síncrona) de Gateway hacia `auth-ms` para validar el JWT (ya que la validación se hace de forma criptográfica local/asíncrona), el Gateway depende de que `auth-ms` firme los tokens JWT utilizando la **misma clave simétrica** configurada en `jwt.secret`. Si las claves de ambos servicios no coinciden, todas las peticiones a rutas protegidas fallarán con error `401 Unauthorized`.

---

## 2. Integraciones de Telemetría y Monitoreo (Observabilidad)

El Gateway incluye soporte integrado para exportación de trazas distribuídas y recolección de métricas a nivel de red, configurado en el archivo [pom.xml](file:///home/gusgus/eclipse-workspace/Gateway/pom.xml):

### OpenTelemetry (OTLP Exporter)
- **Tecnología:** `io.opentelemetry:opentelemetry-exporter-otlp` y `io.micrometer:micrometer-tracing-bridge-otel`.
- **Propósito:** Exportación automática de trazas de peticiones HTTP (tiempos de respuesta, errores en filtros, latencia de red) utilizando el protocolo estándar OTLP (OpenTelemetry Protocol).
- **Destino:** Un colector de OpenTelemetry (OTel Collector), Jaeger, o Zipkin que esté escuchando en la red (configurable por variables de entorno de OTel).

### Prometheus Metrics
- **Tecnología:** `io.micrometer:micrometer-registry-prometheus`.
- **Propósito:** Mapeo de métricas internas del framework (uso de memoria, hilos del servidor Netty, número de peticiones entrantes, códigos de error HTTP de salida) en un formato compatible con Prometheus.
- **Punto de Entrada para Raspado (Scraping):** El endpoint `/actuator/prometheus` es consultado periódicamente por el servidor central de Prometheus para alimentar tableros de visualización (ej. Grafana).

---

> **Nota para IA:** Al añadir un nuevo microservicio al ecosistema de eduLLM, asegúrate de darlo de alta en la sección de `spring.cloud.gateway.routes` del archivo `application.yml`. Asimismo, es fundamental que el nuevo servicio pueda leer los headers de identidad (`X-User-Id`, `X-User-Role`, `X-Username`) propagados por el Gateway.

---

### Última revisión
- **Fecha:** 2026-05-25 01:20:13
- **Commit:** `364990c`

## Instrucciones para actualizar este doc
- Si añades una integración externa o cambias un microservicio downstream → actualiza `INTEGRATIONS.md`.
- Si cambia la estructura de archivos → actualiza `INDEX.md`.
- Cuando completes un cambio relevante → añade línea en `CHANGELOG.md`.

[← Volver al índice](INDEX.md)
