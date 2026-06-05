[← Volver al índice](INDEX.md)

# API Reference - Gateway de eduLLM

El Gateway no implementa lógica de negocio propia ni expone controladores REST personalizados. Su API pública consiste en el mapeo y reenvío de peticiones (Routing) y la exposición de endpoints de diagnóstico y monitoreo (Actuator).

## Rutas y Enrutamiento (Routing)

Todas las rutas entrantes que se describen a continuación son recibidas en el puerto del Gateway (`8089`) y redirigidas a sus respectivos microservicios sin alterar sus prefijos (`StripPrefix=0`).

### 1. Servicio de Autenticación (`auth-ms`)
- **ID de Ruta:** `auth-ms`
- **URI Destino:** `http://localhost:8082` (o servicio `auth-ms` en entorno contenerizado)
- **Predicados (Paths mapeados):**
  - `/api/auth/**` (Ej. `/api/auth/login`, `/api/auth/forgot-password`, `/api/auth/reset-password`)
  - `/login`
  - `/forgot-password`
  - `/reset-password`
  - `/dashboard`

### 2. Servicio de RAG y LLM (`ms-rag`)
- **ID de Ruta:** `ms-rag`
- **URI Destino:** `http://localhost:8002` (o servicio `ms-rag` en entorno contenerizado)
- **Predicados (Paths mapeados):**
  - `/api/rag/**` (Ej. `/api/rag/ask`, `/api/rag/query`)

### 3. Servicio de Administración (`ms-admin`)
- **ID de Ruta:** `ms-admin`
- **URI Destino:** `http://localhost:8083` (o servicio `ms-admin` en entorno contenerizado)
- **Predicados (Paths mapeados):**
  - `/api/admin/**` (Ej. `/api/admin/users`, `/api/admin/config`)

---

## Endpoints Públicos vs Protegidos

El Gateway determina la necesidad de autenticación según el path de la petición.

### Endpoints Manejados Directamente por el Gateway

| Método HTTP | Path | Propósito |
|---|---|---|
| `GET` | `/login-success` | Valida el token (cookie `jwtToken`), determina el rol y redirige (302) al dashboard del frontend correspondiente (admin/profesor/estudiante). |
| `GET` | `/api/auth/verify` | Verifica el token (cookie o header) y retorna JSON con `{authenticated, username, rol, idUsuario}`. Usado por el frontend para comprobar sesión activa. |

### Rutas Públicas (Bypass de Filtro JWT)
Las siguientes peticiones no requieren autenticación y son enviadas directamente a los microservicios correspondientes:

| Método HTTP | Path de Entrada | Servicio Destino | Propósito |
|---|---|---|---|
| `POST` | `/api/auth/login` | `auth-ms` | Inicio de sesión y obtención del JWT |
| `POST` | `/api/auth/forgot-password` | `auth-ms` | Solicitud de restablecimiento de contraseña |
| `POST` | `/api/auth/reset-password` | `auth-ms` | Restablecimiento final de contraseña |
| `GET` | `/login` | `auth-ms` | Vista de inicio de sesión |
| `GET` | `/forgot-password` | `auth-ms` | Vista de recuperación de contraseña |
| `GET` | `/reset-password` | `auth-ms` | Vista de cambio de contraseña |

*Nota: Cualquier sub-ruta que comience con los prefijos anteriores también es tratada como pública por el Gateway.*

### Rutas Protegidas (Requieren Token JWT Válido)
Toda ruta que **no** coincida con la lista pública anterior ni con los endpoints directos es considerada protegida.
- **Extracción de Token (por orden de prioridad):**
  1. Cookie `jwtToken` (para peticiones desde navegador).
  2. Cabecera `Authorization: Bearer <token_jwt>` (para peticiones API).
- **Autorización por Rol:** El Gateway verifica que el rol del usuario tenga permiso para acceder a la ruta solicitada mediante las reglas configuradas en `gateway.security.role-rules`.
- **Respuesta en caso de error:** El Gateway retorna un JSON descriptivo:
  - `401 Unauthorized`: `{"error": "Token requerido"}` o `{"error": "Token inválido"}`
  - `403 Forbidden`: `{"error": "Sin permiso"}`

---

## Endpoints de Diagnóstico (Spring Boot Actuator)

El Gateway expone endpoints internos en el puerto `8089` para chequeos de salud y observabilidad.

| Método HTTP | Endpoint | Acceso | Propósito |
|---|---|---|---|
| `GET` | `/actuator/health` | Público | Comprobación del estado del Gateway (UP/DOWN) |
| `GET` | `/actuator/prometheus` | Interno (Prometheus) | Métricas formateadas para recolección en Prometheus |

---

## Headers Inyectados en el Reenvío (Downstream Headers)

Cuando una petición protegida es validada con éxito por el Gateway, este inyecta cabeceras HTTP adicionales que los microservicios downstream utilizan para conocer la identidad del usuario sin necesidad de volver a procesar el JWT:

- **`X-User-Id`:** El identificador numérico interno de la cuenta de usuario (ej. `12`).
- **`X-User-Role`:** Rol del usuario asociado (ej. `STUDENT`, `TEACHER`, `ADMIN`).
- **X-Username:** El nombre de usuario o dirección de correo electrónico del usuario (ej. `user@edullm.com`).

### Ejemplo de Cabeceras Modificadas

**Petición entrante del cliente al Gateway:**
```http
GET /api/rag/history HTTP/1.1
Host: localhost:8089
Authorization: Bearer eyJhbGciOi...
```

**Petición saliente del Gateway al Microservicio RAG:**
```http
GET /api/rag/history HTTP/1.1
Host: localhost:8002
X-User-Id: 45
X-User-Role: STUDENT
X-Username: student@edullm.com
```

---

> **Nota para IA:** Si desarrollas un nuevo microservicio, debes registrar sus prefijos en el archivo `application.yml` en la propiedad `spring.cloud.gateway.routes`. Si ese microservicio expone algún endpoint público que deba saltarse la validación JWT, deberás agregarlo también a `PUBLIC_PATHS` en `JwtAuthenticationFilter.java`.

---

### Última revisión
- **Fecha:** 2026-05-30
- **Commit:** `HEAD` (cambios sin commit)

## Instrucciones para actualizar este doc
- Si añades una nueva ruta en `application.yml` o cambias los endpoints expuestos → actualiza `API.md`.
- Si cambia la estructura de archivos → actualiza `INDEX.md`.
- Cuando completes un cambio relevante → añade línea en `CHANGELOG.md`.

[← Volver al índice](INDEX.md)
