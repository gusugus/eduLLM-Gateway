[← Volver al índice](INDEX.md)

# Vistas e Interfaz de Usuario - Gateway de eduLLM

## Estado del Módulo: No Aplica

El **Gateway** de **eduLLM** es un componente de backend de bajo nivel y proxy de red. Su propósito es interceptar, validar y redirigir el tráfico HTTP de la API.

### Justificación Técnica

- **Sin interfaz visual:** Este proyecto no incluye plantillas HTML, hojas de estilo CSS, scripts de frontend (como React, Angular o Vue.js) ni dependencias destinadas a renderizar interfaces gráficas de usuario.
- **Redirección de Vistas:** El Gateway enruta peticiones de vistas (como `/login`, `/forgot-password`, `/reset-password` y `/dashboard`) hacia el servidor frontend o el microservicio correspondiente (`auth-ms`), pero no aloja ni renderiza el contenido de estas páginas directamente.

### Visualización e Interacción

Cualquier interacción visual con los endpoints de este Gateway se realiza de manera programática mediante:

- Herramientas de pruebas de APIs (ej. Postman, cURL).
- Navegadores web que realizan peticiones asíncronas desde el frontend cliente hacia los endpoints protegidos.
- Dashboards externos de monitoreo que se conectan al endpoint `/actuator/prometheus` (como Grafana).

---

> **Nota para IA:** Si deseas modificar la interfaz visual de eduLLM (estilos, pantallas, componentes web), debes dirigirte al repositorio del Frontend o al microservicio que aloje las vistas específicas, no a este proyecto de Gateway.

---

### Última revisión
- **Fecha:** 2026-05-25 01:20:13
- **Commit:** `364990c`

## Instrucciones para actualizar este doc
- Si se agregan o modifican vistas o interfaces de usuario a nivel de Gateway (ej. una página interna de estado de Gateway) → actualiza `VIEWS.md`.
- Si cambia la estructura de archivos → actualiza `INDEX.md`.
- Cuando completes un cambio relevante → añade línea en `CHANGELOG.md`.

[← Volver al índice](INDEX.md)
