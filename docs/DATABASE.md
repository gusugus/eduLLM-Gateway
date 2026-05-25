[← Volver al índice](INDEX.md)

# Base de Datos - Gateway de eduLLM

## Estado del Módulo: No Aplica

El **Gateway** es un microservicio puramente de enrutamiento y proxy inverso de la API. Está diseñado para ser **stateless** (sin estado) para optimizar el rendimiento y escalado horizontal del sistema.

### Justificación Técnica

- **Sin persistencia local:** El Gateway no requiere almacenar datos en memoria persistente, discos ni conectarse a bases de datos relacionales (como PostgreSQL, MySQL) ni no-relacionales (como MongoDB, Qdrant).
- **Tratamiento del token JWT:** El análisis y validación del JWT se realiza de manera puramente matemática y criptográfica utilizando la clave configurada en `jwt.secret` (firma simétrica HMAC-SHA). No se realiza ninguna consulta a base de datos para recuperar detalles de la sesión.
- **Microservicios downstream:** Cualquier necesidad de persistencia (información de usuarios, historial del RAG, auditorías de administración) es gestionada individualmente por los microservicios downstream (`auth-ms`, `ms-rag` y `ms-admin`), los cuales poseen sus propios esquemas y conexiones de bases de datos.

### Posibles Evoluciones Futuras

Si en el roadmap del proyecto se decidiera añadir características avanzadas a nivel de Gateway, se podría llegar a requerir persistencia en memoria caché ultrarrápida:

- **Redis Cache:** Para el control de límite de tasa de peticiones (Rate Limiting) o para almacenar listas negras de tokens JWT revocados (Token Revocation List - TRL). En ese caso, este documento se actualizará detallando el esquema de claves de Redis.

---

> **Nota para IA:** Si necesitas realizar una consulta de base de datos en los flujos del sistema, recuerda que el Gateway no tiene acceso directo a la persistencia. Dicha funcionalidad debe implementarse en los microservicios internos correspondientes y consumirse a través de llamadas API.

---

### Última revisión
- **Fecha:** 2026-05-25 01:20:13
- **Commit:** `364990c`

## Instrucciones para actualizar este doc
- Si cambias el esquema de BD (tablas, columnas, índices) o añades persistencia (ej. Redis para Rate Limit) → actualiza `DATABASE.md`.
- Si cambia la estructura de archivos → actualiza `INDEX.md`.
- Cuando completes un cambio relevante → añade línea en `CHANGELOG.md`.

[← Volver al índice](INDEX.md)
