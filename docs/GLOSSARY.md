[← Volver al índice](INDEX.md)

# Glosario - Gateway de eduLLM

Este glosario define los términos técnicos y conceptos de dominio clave utilizados en la arquitectura y documentación del **Gateway** de **eduLLM**.

| Término | Definición |
|---|---|
| **Spring Cloud Gateway** | Framework basado en Spring Boot 3 y Project Reactor para la construcción de API Gateways eficaces, reactivos y no bloqueantes. |
| **Proxy Inverso** | Servidor intermedio que se sitúa frente a los servidores de origen (microservicios) y redirige las solicitudes de los clientes a estos, ocultando la infraestructura interna. |
| **Downstream Service** | Todo microservicio interno situado detrás del Gateway al que se le reenvía el tráfico web (ej. `ms-rag`, `auth-ms`, `ms-admin`). |
| **JWT (JSON Web Token)** | Estándar abierto (RFC 7519) que define un modo compacto y autónomo de transmitir información entre partes como un objeto JSON de forma segura y firmada. |
| **Claims** | Atributos o declaraciones de datos empaquetados dentro de un JWT (ej: `idUsuario`, `rol`, `sub` para el usuario). |
| **HMAC-SHA256 (HS256)** | Algoritmo de firma digital simétrica que utiliza una única clave secreta compartida para firmar y verificar tokens de manera segura. |
| **Header Spoofing** | Tipo de ataque donde un cliente malintencionado intenta forjar cabeceras HTTP de identidad (como `X-User-Id`) para saltarse el control de autenticación. |
| **Spring Boot Actuator** | Módulo de Spring Boot que añade endpoints de producción listos para su uso, permitiendo monitorear e interactuar con la aplicación (ej. estado de salud, métricas). |
| **Prometheus** | Sistema de monitoreo y base de datos de series temporales de código abierto utilizado para recopilar y almacenar métricas de rendimiento. |
| **OpenTelemetry (OTel)** | Colección de herramientas, APIs y SDKs que se utilizan para instrumentar, generar, recopilar y exportar datos de telemetría (trazas, métricas y registros). |
| **Filtro Global** | Interceptor de Spring Cloud Gateway que se aplica de manera condicional o incondicional a todas las rutas que procesa el proxy. |

---

> **Nota para IA:** Si al explicar o proponer un nuevo diseño técnico utilizas acrónimos o términos complejos no listados aquí, añádelos a este glosario para facilitar la lectura de los desarrolladores.

---

### Última revisión
- **Fecha:** 2026-05-25 01:20:13
- **Commit:** `364990c`

## Instrucciones para actualizar este doc
- Si introduces nuevos conceptos arquitectónicos o de dominio → actualiza `GLOSSARY.md`.
- Si cambia la estructura de archivos → actualiza `INDEX.md`.
- Cuando completes un cambio relevante → añade línea en `CHANGELOG.md`.

[← Volver al índice](INDEX.md)
