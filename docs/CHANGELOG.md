[← Volver al índice](INDEX.md)

# Changelog - Gateway de eduLLM

Todos los cambios notables realizados en el proyecto del **Gateway** de **eduLLM** se registrarán en este archivo. El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/).

---

## [0.0.3-SNAPSHOT] - 2026-06-02

### Añadido
- **CORS centralizado en Gateway:** Configurado `globalcors` permitiendo orígenes de los frontends (`localhost:8001`, `8002`, `8003`) con `allowCredentials: true`.
- **Cabeceras de seguridad HTTP (Security Headers):** Configuradas como `default-filters` en `application.yml`:
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: DENY`
  - `Strict-Transport-Security: max-age=31536000; includeSubDomains`
  - `X-XSS-Protection: 0`
  - `Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:`

---

## [0.0.2-SNAPSHOT] - 2026-05-30

### Modificado
- **Refactor completo de `JwtAuthenticationFilter`:** Separación de la lógica de filtrado en métodos especializados (`handleLoginSuccessAndRedirect`, `handleVerify`, `handleProtectedRoute`, `extractToken`, `isAuthorized`).
- **Soporte de extracción de token desde cookie `jwtToken`:** El filtro ahora intenta obtener el JWT primero de la cookie, con fallback al header `Authorization: Bearer`.
- **Respuestas de error en JSON:** En lugar de códigos HTTP planos, el Gateway retorna `{"error": "..."}` para errores de autenticación y autorización.

### Añadido
- **Endpoint `/login-success`:** Maneja la redirección post-login al dashboard del frontend correspondiente según el rol del usuario.
- **Endpoint `/api/auth/verify`:** Endpoint para que el frontend verifique el estado de la sesión activa, retornando datos del usuario en JSON.
- **Autorización por Rol (RBAC):** Implementación de `isAuthorized()` que consulta `RoleRulesProperties` para validar que el rol del usuario tenga acceso a la ruta solicitada.
- **Nuevas clases de configuración:**
  - `FrontendProperties` (`@ConfigurationProperties(prefix = "app.frontend")`): Mapea URLs de frontend por rol.
  - `RoleRulesProperties` (`@ConfigurationProperties(prefix = "gateway.security")`): Define reglas de acceso por rol.
- **Dependencia `spring-boot-configuration-processor`:** Para generar metadatos de configuración de las propiedades personalizadas.

---

## [0.0.1-SNAPSHOT] - 2026-05-25

Esta es la versión inicial del microservicio Gateway en el ecosistema eduLLM.

### Añadido
- **Enrutamiento Reactivo:** Configuración de rutas principales para redirigir peticiones de clientes a microservicios downstream (`auth-ms`, `ms-rag`, `ms-admin`) mediante Spring Cloud Gateway en `application.yml`.
- **Filtro Global de Autenticación:** Implementación de `JwtAuthenticationFilter` para interceptar todas las peticiones protegidas de la API y comprobar la validez de los tokens JWT de manera reactiva.
- **Utilidad de Firma JWT:** Implementación de `JwtUtil` para verificar firmas simétricas HMAC-SHA y extraer claims básicos (`idUsuario`, `rol`, `username`).
- **Propagación de Identidad:** Inyección automática de cabeceras HTTP internas (`X-User-Id`, `X-User-Role`, `X-Username`) en las peticiones enrutadas downstream.
- **Exclusión de Rutas Públicas:** Soporte de bypass de seguridad para rutas de login, registro y restauración de contraseñas.
- **Monitoreo y Métricas:** Integración de Spring Boot Actuator con endpoints de salud (`/actuator/health`) y métricas de Prometheus (`/actuator/prometheus`).
- **Telemetría Distribuida:** Soporte de trazas OpenTelemetry mediante OTLP con Micrometer Tracing.

---

> **Nota para IA:** Cuando realices cambios significativos en el código de este microservicio (como añadir nuevas rutas o cambiar el comportamiento del filtro), asegúrate de documentarlo agregando una sección en este archivo especificando si el cambio fue `Añadido`, `Modificado`, `Corregido` o `Eliminado`.

---

### Última revisión
- **Fecha:** 2026-05-25 01:20:13
- **Commit:** `364990c`

## Instrucciones para actualizar este doc
- Cuando completes un cambio relevante en el código o configuración del Gateway → añade línea en `CHANGELOG.md`.
- Si cambia la estructura de archivos → actualiza `INDEX.md`.

[← Volver al índice](INDEX.md)
