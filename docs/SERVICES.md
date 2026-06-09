[← Volver al índice](INDEX.md)

# Servicios Internos - Gateway de eduLLM

El Gateway se organiza en torno a dos componentes de lógica interna para la gestión de seguridad y autenticación. A continuación, se detallan sus propósitos, métodos públicos e implementación técnica.

---

## 1. Filtro Global: `JwtAuthenticationFilter`

El componente [JwtAuthenticationFilter](file:///home/gusgus/eclipse-workspace/Gateway/src/main/java/com/edullm/gateway/filter/JwtAuthenticationFilter.java) es un filtro de red global reactivo que intercepta todas las solicitudes entrantes al Gateway. Su objetivo principal es actuar como una barrera de seguridad de pre-autenticación.

- **Tipo:** `@Component` de Spring. Implementa `GlobalFilter` y `Ordered`.
- **Precedencia (`getOrder()`):** Retorna `-100`. Se ejecuta antes que los filtros de enrutamiento y balanceo de carga para denegar peticiones no autorizadas de forma inmediata.
- **Dependencias inyectadas:**
  - `JwtUtil`: Utilidad criptográfica y descodificadora del token.
  - `RoleRulesProperties`: Configuración de reglas de autorización por rol (RBAC).
  - `FrontendProperties`: Mapeo de URLs de frontend por rol del usuario.

### Lógica de Filtrado (`filter(ServerWebExchange, GatewayFilterChain)`)

El flujo principal evalúa en orden:

1. **`/login-success`**: Si el path coincide exactamente, se delega a `handleLoginSuccessAndRedirect()`.
2. **`/api/auth/verify`**: Si el path coincide exactamente, se delega a `handleVerify()`.
3. **Rutas públicas**: Si el path comienza con algún prefijo en `PUBLIC_PATHS`, se permite el paso sin autenticación.
4. **Rutas protegidas**: Se delega a `handleProtectedRoute()` para validar token y autorización por rol.

```mermaid
graph TD
    A[Inicio de petición HTTP] --> B{path == /login-success?}
    B -- Sí --> C[handleLoginSuccessAndRedirect]
    C --> C1[Extraer token de cookie/header]
    C1 --> C2{Token válido?}
    C2 -- No --> C3[Redirect a /login]
    C2 -- Sí --> C4[Obtener URL frontend según rol]
    C4 --> C5[Redirect 302 a frontend/dashboard]
    
    B -- No --> D{path == /api/auth/verify?}
    D -- Sí --> E[handleVerify]
    E --> E1[Extraer token]
    E1 --> E2{Token válido?}
    E2 -- No --> E3[JSON: {authenticated: false}]
    E2 -- Sí --> E4[JSON: {authenticated, username, rol, idUsuario}]

    D -- No --> F{¿Comienza con ruta en PUBLIC_PATHS?}
    F -- Sí --> G[Bypass: chain.filter]

    F -- No --> H[handleProtectedRoute]
    H --> H1[Extraer token de cookie/header]
    H1 --> H2{Token presente?}
    H2 -- No --> H3[JSON error 401: Token requerido]
    H2 -- Sí --> H4{validateToken?}
    H4 -- No --> H5[JSON error 401: Token inválido]
    H4 -- Sí --> H6{Obtener rol del token}
    H6 --> H7{isAuthorized path + rol?}
    H7 -- No --> H8[JSON error 403: Sin permiso]
    H7 -- Sí --> H9[Extraer claims e inyectar headers X-*]
    H9 --> H10[chain.filter con request mutada]
```

### Rutas Públicas Excluidas (`PUBLIC_PATHS`)
El filtro define una lista interna de rutas que no requieren comprobación de token:
- `/api/auth/login`
- `/api/auth/forgot-password`
- `/api/auth/reset-password`
- `/login`
- `/forgot-password`
- `/reset-password`

### Métodos Internos

| Método | Visibilidad | Descripción |
|---|---|---|
| `handleLoginSuccessAndRedirect(exchange, chain)` | `private` | Valida token (cookie o header) y redirige al dashboard del frontend correspondiente según el rol. Usa `FrontendProperties.getUrlByRole()`. |
| `handleVerify(exchange, chain)` | `private` | Valida token y retorna JSON con estado de autenticación (`authenticated`, `username`, `rol`, `idUsuario`). |
| `handleProtectedRoute(exchange, chain)` | `private` | Extrae token, valida, verifica autorización por rol (`isAuthorized`) e inyecta headers `X-User-*` en la request mutada. |
| `redirectToLogin(exchange)` | `private` | Redirige (302) a `/login`. |
| `extractToken(exchange)` | `private` | Extrae el JWT primero de la cookie `jwtToken`, luego del header `Authorization: Bearer`. |
| `isAuthorized(path, rol)` | `private` | Verifica si el rol tiene permiso para acceder al path según las reglas en `RoleRulesProperties`. |
| `errorResponse(exchange, status, msg)` | `private` | Retorna respuesta JSON con campo `error`. |
| `writeJsonResponse(exchange, status, body)` | `private` | Retorna respuesta JSON genérica a partir de un `Map`. |

---

## 2. Utilidad Criptográfica: `JwtUtil`

El componente [JwtUtil](file:///home/gusgus/eclipse-workspace/Gateway/src/main/java/com/edullm/gateway/util/JwtUtil.java) encapsula las operaciones criptográficas sobre los JSON Web Tokens utilizando la biblioteca Java JWT (JJWT).

- **Tipo:** `@Component` de Spring.
- **Propiedades configuradas:**
  - `${jwt.secret}`: Secreto hexadecimal inyectado desde `application.yml`.

### Métodos Principales

| Método | Retorno | Parámetros | Descripción |
|---|---|---|---|
| `validateToken(token)` | `boolean` | `String token` | Valida que la firma sea íntegra y que el token no haya expirado. Retorna `false` ante cualquier excepción de firma o parseo. |
| `extractUsername(token)` | `String` | `String token` | Extrae el Subject (`sub`) del token JWT. |
| `extractIdUsuario(token)` | `Long` | `String token` | Extrae la propiedad personalizada `idUsuario` desde los claims. |
| `extractRol(token)` | `String` | `String token` | Extrae la propiedad personalizada `rol` (rol del usuario, ej: `ADMIN`, `STUDENT`) desde los claims. |
| `isTokenExpired(token)` | `boolean` | `String token` | Compara el claim de expiración (`exp`) con la fecha actual del sistema. |
| `extractAllClaims(token)` | `Claims` | `String token` | Método privado que realiza el parseo de la firma del token utilizando la clave secreta generada en `getSignKey()`. |
| `getSignKey()` | `SecretKey` | Ninguno | Método privado que convierte el String hexadecimal de `jwt.secret` en bytes `UTF_8` y construye la llave criptográfica mediante `Keys.hmacShaKeyFor()`. |

---

> **Nota para IA:** Si necesitas extender los datos del usuario que se propagan a los microservicios downstream, primero debes asegurar que dichos claims (ej. `email`, `organización`) estén incluidos en el JWT generado por `auth-ms`. Luego, puedes añadir un método extractor en `JwtUtil.java` e inyectar la cabecera correspondiente en `JwtAuthenticationFilter.java`. Recuerda también actualizar la configuración de `FrontendProperties` si agregas un nuevo rol.

---

### Última revisión
- **Fecha:** 2026-05-30
- **Commit:** `HEAD` (cambios sin commit)

## Instrucciones para actualizar este doc
- Si añades o modificas servicios internos, filtros globales o clases de utilidad → actualiza `SERVICES.md`.
- Si cambia un flujo de enrutamiento → actualiza `ARCHITECTURE.md`.
- Si cambia la estructura de archivos → actualiza `INDEX.md`.
- Cuando completes un cambio relevante → añade línea en `CHANGELOG.md`.

[← Volver al índice](INDEX.md)
