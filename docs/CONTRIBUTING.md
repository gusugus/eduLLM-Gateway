[← Volver al índice](INDEX.md)

# Guía del Desarrollador (Contributing) - Gateway de eduLLM

Esta guía proporciona a los desarrolladores los pasos necesarios para modificar y extender el funcionamiento del **Gateway** de **eduLLM**.

---

## 1. Cómo Agregar una Nueva Ruta (Nuevo Microservicio)

Para conectar un nuevo microservicio al ecosistema a través del Gateway, sigue estos pasos:

1. Abre el archivo de configuración [application.yml](file:///home/gusgus/eclipse-workspace/Gateway/src/main/resources/application.yml).
2. Localiza la sección `spring.cloud.gateway.routes`.
3. Añade una nueva entrada con el siguiente formato:

```yaml
        # Ruta para el nuevo microservicio (ej. ms-ejemplo)
        - id: ms-ejemplo
          uri: http://localhost:8085  # Dirección del microservicio en desarrollo o Docker DNS
          predicates:
            - Path=/api/ejemplo/**   # Todos los paths que coincidan con este prefijo serán enrutados
          filters:
            - StripPrefix=0          # Mantiene el prefijo /api/ejemplo al enviar la petición downstream
```

4. Guarda el archivo y reinicia el Gateway.

---

## 2. Cómo Excluir una Nueva Ruta de la Autenticación (Hacerla Pública)

Si el nuevo microservicio expone endpoints que deben ser accesibles públicamente (sin token JWT, como por ejemplo, `/api/ejemplo/public-info`):

1. Abre el archivo [JwtAuthenticationFilter.java](file:///home/gusgus/eclipse-workspace/Gateway/src/main/java/com/edullm/gateway/filter/JwtAuthenticationFilter.java).
2. Localiza la constante `PUBLIC_PATHS`:

```java
    // Rutas que no requieren autenticación
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/auth/login",
        "/api/auth/forgot-password",
        "/api/auth/reset-password",
        "/login",
        "/forgot-password",
        "/reset-password"
        // Agrega tu ruta aquí, ej:
        // "/api/ejemplo/public-info"
    );
```

3. Añade la ruta correspondiente a la lista (utiliza coincidencias por prefijo, ya que el filtro evalúa con `startsWith`).
4. Vuelve a compilar el proyecto y realiza pruebas para verificar que no requiere cabecera de autenticación.

---

## 3. Estilo de Código y Prácticas Clave

- **Uso de Lombok:** Utiliza anotaciones como `@Slf4j` para logging y `@Autowired` o constructores para inyección de dependencias. Evita escribir código boilerplate.
- **Programación Reactiva:** Spring Cloud Gateway utiliza WebFlux y Project Reactor. Cualquier filtro personalizado o procesamiento debe realizarse en un contexto reactivo no bloqueante (retornando `Mono<Void>` u objetos reactivos similares). **Evita** el uso de llamadas bloqueantes de red o de archivo (`Thread.sleep()`, APIs de entrada/salida bloqueantes clásicas) dentro de los filtros globales.
- **Monitoreo de Cambios en Dependencias:** Si introduces nuevas dependencias de Maven, asegúrate de registrarlas y justificar su inclusión en el archivo `DEPENDENCIES.md`.

---

> **Nota para IA:** Cuando propongas cambios en las rutas, asegúrate de indicarle al desarrollador que también actualice la lista de endpoints en `API.md` y registre la integración correspondiente en `INTEGRATIONS.md`.

---

### Última revisión
- **Fecha:** 2026-05-25 01:20:13
- **Commit:** `364990c`

## Instrucciones para actualizar este doc
- Si cambia el flujo de contribución, el estilo de código o las instrucciones de despliegue local → actualiza `CONTRIBUTING.md`.
- Si cambia la estructura de archivos → actualiza `INDEX.md`.
- Cuando completes un cambio relevante → añade línea en `CHANGELOG.md`.

[← Volver al índice](INDEX.md)
