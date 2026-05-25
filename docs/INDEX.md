# Índice de Documentación - Gateway eduLLM

Este directorio contiene la documentación técnica oficial del **Gateway** de **eduLLM**, un componente clave que actúa como punto de entrada unificado y proxy inverso para todo el ecosistema de microservicios.

## Documentos Disponibles

| Documento | Descripción |
|---|---|
| [README.md](README.md) | Vista general del proyecto, propósito del Gateway y guía de inicio rápido. |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Diagrama general de flujos de enrutamiento y filtros de seguridad en Mermaid. |
| [API.md](API.md) | Configuración de rutas expuestas, endpoints públicos, protegidos e internos (Actuator). |
| [DATABASE.md](DATABASE.md) | Detalle sobre persistencia (No Aplica para este microservicio stateless). |
| [SERVICES.md](SERVICES.md) | Componentes internos (`JwtAuthenticationFilter`, `JwtUtil`). |
| [INTEGRATIONS.md](INTEGRATIONS.md) | Relación con otros microservicios (`auth-ms`, `ms-rag`, `ms-admin`) y observabilidad. |
| [SECURITY.md](SECURITY.md) | Autenticación mediante JWT, validación de claims y headers de propagación. |
| [VIEWS.md](VIEWS.md) | Interfaz de usuario (No Aplica para este microservicio de backend). |
| [IMPROVEMENTS.md](IMPROVEMENTS.md) | Mejoras arquitectónicas sugeridas y deuda técnica identificada. |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Guía de desarrollo y extensión del Gateway. |
| [GLOSSARY.md](GLOSSARY.md) | Glosario de términos del ecosistema y tecnologías utilizadas. |
| [DEPENDENCIES.md](DEPENDENCIES.md) | Listado y justificación de dependencias de Maven (`pom.xml`). |
| [CHANGELOG.md](CHANGELOG.md) | Registro histórico de cambios del Gateway. |

---

> **Nota para IA:** Este índice es el mapa de navegación del proyecto. Siempre que realices cambios estructurales en los archivos de `/docs`, asegúrate de mantener actualizados los enlaces de este índice y los enlaces de retorno `[← Volver al índice](INDEX.md)` en cada archivo.

---

### Última revisión
- **Fecha:** 2026-05-25 01:20:13
- **Commit:** `364990c`

## Instrucciones para actualizar este doc
- Si cambia la estructura de archivos → actualiza `INDEX.md`.
- Cuando completes un cambio relevante → añade línea en `CHANGELOG.md`.
