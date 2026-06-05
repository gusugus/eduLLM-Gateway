[← Volver al índice](INDEX.md)

# Seguridad - Gateway de eduLLM

La seguridad en el ecosistema **eduLLM** se gestiona de forma centralizada. El Gateway actúa como el guardián perimetral del sistema, autenticando todas las solicitudes externas antes de permitirles el acceso a los microservicios downstream.

---

## 1. Mecanismo de Autenticación (JWT)

El Gateway utiliza JSON Web Tokens (JWT) firmados mediante algoritmos simétricos para validar la identidad de los clientes.

### Flujo de Verificación Criptográfica
1. **Firma Simétrica:** Se utiliza la clave configurada en `jwt.secret` (en `application.yml`) para validar la integridad del token.
2. **Algoritmo:** HMAC-SHA (mínimo de 256 bits). La clave simétrica de desarrollo configurada es:
   `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970`
3. **Validación de Expiración:** `JwtUtil.isTokenExpired()` comprueba que el claim de expiración (`exp`) del token sea posterior a la hora actual del servidor. Si el token está caducado, se lanza una excepción y se deniega el paso.

---

## 2. Extracción del Token

El Gateway soporta dos mecanismos para obtener el JWT, evaluados en orden de prioridad:

1. **Cookie `jwtToken`:** Para peticiones desde navegador (login-success, verify, SPA).
2. **Cabecera `Authorization: Bearer <token>`:** Para peticiones API tradicionales.

## 3. Autorización por Rol (RBAC)

El Gateway implementa control de acceso basado en roles mediante `RoleRulesProperties` (configurado con prefijo `gateway.security.role-rules`). Después de validar el token, verifica que el rol del usuario tenga permiso para acceder al path solicitado. Si no, responde con `403 Forbidden` y `{"error": "Sin permiso"}`.

```yaml
# Ejemplo de configuración en application.yml
gateway:
  security:
    role-rules:
      ROLE_ADMINISTRADOR:
        - "/api/admin/**"
        - "/api/rag/**"
      ROLE_PROFESOR:
        - "/api/rag/**"
      ROLE_ESTUDIANTE:
        - "/api/rag/**"
```

## 4. Propagación de Identidad e Inyección de Cabeceras (Headers)

Una vez que el token es verificado con éxito y el rol está autorizado, el Gateway extrae la información de identidad encapsulada en los claims y la propaga internamente mediante cabeceras HTTP limpias:

| Cabecera Inyectada | Claim de Origen | Formato / Tipo | Propósito |
|---|---|---|---|
| `X-User-Id` | `idUsuario` | Numérico (`Long`) | ID de usuario en la base de datos |
| `X-User-Role` | `rol` | Texto (`String`) | Rol asignado (ej: `ADMIN`, `TEACHER`, `STUDENT`) |
| `X-Username` | Subject (`sub`) | Texto (`String`) | Correo o nombre de usuario |

Los microservicios internos **no** necesitan validar el token JWT; simplemente confían en el contenido de estas cabeceras.

---

## 5. Control de Acceso CORS

El Gateway gestiona CORS de forma centralizada mediante `spring.cloud.gateway.globalcors` en `application.yml`, permitiendo únicamente los orígenes de los frontend conocidos:

| Origen | Frontend |
|---|---|
| `http://localhost:8001` | Administrador |
| `http://localhost:8002` | Profesor |
| `http://localhost:8003` | Estudiante |

`allowCredentials: true` permite el envío de cookies HttpOnly (`jwtToken`) desde los frontends.

---

## 6. Cabeceras de Seguridad HTTP (Security Headers)

El Gateway incluye un conjunto de cabeceras de seguridad en todas las respuestas mediante `default-filters` en `application.yml`:

| Cabecera | Valor | Propósito |
|---|---|---|
| `X-Content-Type-Options` | `nosniff` | Evita que el navegador interprete MIME types no declarados. |
| `X-Frame-Options` | `DENY` | Previene ataques de clickjacking al denegar la carga en iframes. |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains` | Obliga a conexiones HTTPS durante 1 año (aplica solo si la conexión ya es HTTPS). |
| `X-XSS-Protection` | `0` | Desactiva el legacy XSS filter de navegadores antiguos (obsoleto, reemplazado por CSP). |
| `Content-Security-Policy` | `default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:` | Restringe orígenes de recursos cargados (scripts, estilos, imágenes) para mitigar XSS. |

---

## 7. Seguridad de Red y Mitigación de Riesgos

### Prevención de Suplantación de Cabeceras (Header Spoofing)
Dado que los microservicios internos confían plenamente en las cabeceras `X-User-Id`, `X-User-Role` y `X-Username`, existe el riesgo de que un atacante intente realizar una petición directa a un microservicio (saltándose el Gateway) inyectando manualmente estas cabeceras.

**Mitigaciones requeridas:**
1. **Aislamiento de Red:** Los microservicios downstream (`auth-ms`, `ms-rag`, `ms-admin`) **no deben** exponer sus puertos al tráfico de internet público. Deben estar desplegados en una red interna privada (ej. una red virtual de Docker o una subred privada en AWS).
2. **Filtro de Limpieza en Gateway:** Aunque actualmente el Gateway muta la petición agregando los headers basados en un token válido, se recomienda agregar un filtro inicial que elimine cualquier cabecera entrante que comience con `X-User-` o `X-Username` provista por el cliente de forma maliciosa.

### Gestión de Secretos en Producción
La clave secreta JWT en `application.yml` está escrita en texto plano.
> [!WARNING]
> **Nunca** expongas la clave de producción en el repositorio. Para despliegues productivos, la propiedad `jwt.secret` debe inyectarse a través de variables de entorno del sistema operativo o mediante un sistema seguro de gestión de secretos (Vault, AWS Secrets Manager).

---

> **Nota para IA:** La funcionalidad de autorización por rol (RBAC) ya está implementada en el Gateway mediante `RoleRulesProperties` y el método `isAuthorized()` en `JwtAuthenticationFilter`. Para agregar o modificar reglas, edita la propiedad `gateway.security.role-rules` en `application.yml`.

---

### Última revisión
- **Fecha:** 2026-06-02
- **Commit:** `HEAD` (cambios sin commit)

## Instrucciones para actualizar este doc
- Si cambia el mecanismo de autenticación, la firma de tokens o los headers propagados → actualiza `SECURITY.md`.
- Si cambia la estructura de archivos → actualiza `INDEX.md`.
- Cuando completes un cambio relevante → añade línea en `CHANGELOG.md`.

[← Volver al índice](INDEX.md)
