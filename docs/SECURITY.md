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

## 2. Propagación de Identidad e Inyección de Cabeceras (Headers)

Una vez que el token es verificado con éxito, el Gateway extrae la información de identidad encapsulada en los claims y la propaga internamente mediante cabeceras HTTP limpias:

| Cabecera Inyectada | Claim de Origen | Formato / Tipo | Propósito |
|---|---|---|---|
| `X-User-Id` | `idUsuario` | Numérico (`Long`) | ID de usuario en la base de datos |
| `X-User-Role` | `rol` | Texto (`String`) | Rol asignado (ej: `ADMIN`, `TEACHER`, `STUDENT`) |
| `X-Username` | Subject (`sub`) | Texto (`String`) | Correo o nombre de usuario |

Los microservicios internos **no** necesitan validar el token JWT; simplemente confían en el contenido de estas cabeceras.

---

## 3. Seguridad de Red y Mitigación de Riesgos

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

> **Nota para IA:** Si implementas validación de autorizaciones específicas por rol (RBAC - Role-Based Access Control) en el Gateway en el futuro, puedes modificar `JwtAuthenticationFilter.java` para evaluar el claim `rol` antes de reenviar la petición a rutas administrativas como `/api/admin/**`.

---

### Última revisión
- **Fecha:** 2026-05-25 01:20:13
- **Commit:** `364990c`

## Instrucciones para actualizar este doc
- Si cambia el mecanismo de autenticación, la firma de tokens o los headers propagados → actualiza `SECURITY.md`.
- Si cambia la estructura de archivos → actualiza `INDEX.md`.
- Cuando completes un cambio relevante → añade línea en `CHANGELOG.md`.

[← Volver al índice](INDEX.md)
