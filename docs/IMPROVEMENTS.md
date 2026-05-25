[← Volver al índice](INDEX.md)

# Mejoras y Deuda Técnica - Gateway de eduLLM

Este documento consolida las oportunidades de mejora arquitectónica, la deuda técnica acumulada y la hoja de ruta sugerida para optimizar el **Gateway** de **eduLLM**.

---

## 1. Deuda Técnica Prioritaria

- **Hardcoding de Clave Secreta JWT:**
  La clave secreta para la validación de tokens está escrita en texto plano en `src/main/resources/application.yml`.
  - *Acción correctiva:* Mover la propiedad a una variable de entorno (`jwt.secret: ${JWT_SECRET}`) o integrarla con Spring Cloud Config / HashiCorp Vault.
- **Ausencia de Pruebas Automatizadas:**
  La carpeta `src/test/java` está vacía. No existen pruebas unitarias para `JwtUtil` ni pruebas de integración para `JwtAuthenticationFilter`.
  - *Acción correctiva:* Crear pruebas de integración simulando llamadas HTTP reactivas con `WebTestClient` para garantizar que el filtro bloquee los tokens corruptos y propague las cabeceras correctamente.
- **Mantenimiento Manual de Rutas Públicas:**
  Las rutas públicas se definen de forma rígida dentro del código en la constante `PUBLIC_PATHS` de `JwtAuthenticationFilter.java`.
  - *Acción correctiva:* Mover esta lista al archivo de configuración `application.yml` e inyectarla dinámicamente usando `@ConfigurationProperties`.

---

## 2. Roadmap y Mejoras Arquitectónicas (Propuestas)

### A. Rate Limiting (Limitador de Tasa) con Redis
Para prevenir ataques de denegación de servicio (DDoS) y abusos de la API, se propone configurar el filtro `RequestRateLimiter` nativo de Spring Cloud Gateway.
- **Implementación:**
  1. Añadir la dependencia `spring-boot-starter-data-redis-reactive` en `pom.xml`.
  2. Implementar un `KeyResolver` en Java que devuelva la IP del cliente o el ID de usuario (obtenido del JWT).
  3. Configurar la tasa de peticiones permitidas (ej. 10 peticiones por segundo) directamente en el `application.yml`.

### B. Listas Negras de Tokens (Token Revocation / Logout)
Actualmente, un token JWT es válido hasta que expira naturalmente. Si un usuario cierra sesión o cambia su contraseña, su token anterior sigue siendo válido.
- **Implementación:**
  1. Al hacer logout, guardar la firma del token en Redis con un tiempo de vida (TTL) igual a su tiempo restante de expiración.
  2. Modificar `JwtAuthenticationFilter` para comprobar si el token entrante existe en la base de datos de tokens revocados de Redis.

### C. Manejo Centralizado de CORS
Actualmente, los microservicios individuales configuran sus políticas CORS. Esto puede generar conflictos si la cabecera es inyectada o duplicada por el Gateway.
- **Implementación:** Configurar el soporte CORS global de Spring Cloud Gateway en `application.yml` para unificar el dominio permitido (ej. el puerto del Frontend React/Vue).

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "http://localhost:3000"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
            allowedHeaders: "*"
```

---

> **Nota para IA:** Si propones cambios de infraestructura en el proyecto (ej. añadir una base de datos local o cambiar puertos), revisa primero si no existe ya una mejora propuesta en este archivo. Trata siempre de priorizar la resolución de la deuda técnica de pruebas unitarias.

---

### Última revisión
- **Fecha:** 2026-05-25 01:20:13
- **Commit:** `364990c`

## Instrucciones para actualizar este doc
- Para mejoras arquitectónicas, deuda técnica descubierta o roadmap → edita `IMPROVEMENTS.md`.
- Si cambia la estructura de archivos → actualiza `INDEX.md`.
- Cuando completes un cambio relevante → añade línea en `CHANGELOG.md`.

[← Volver al índice](INDEX.md)
