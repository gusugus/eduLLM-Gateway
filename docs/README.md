[← Volver al índice](INDEX.md)

# README - Gateway de eduLLM

El **Gateway** es el punto único de entrada para todas las peticiones externas al ecosistema de microservicios de **eduLLM**. Construido con **Spring Cloud Gateway** (sobre Spring Boot 3.4.4 y Java 17), se encarga de interceptar y enrutar las solicitudes de forma segura a los servicios correspondientes (`auth-ms`, `ms-rag`, `ms-admin`).

Además, implementa filtros globales reactivos para verificar y decodificar tokens JWT (JSON Web Tokens), propagando la identidad del usuario a través de cabeceras HTTP hacia los servicios internos.

## Prerrequisitos

Para compilar y ejecutar este proyecto de forma local, necesitarás:

- **Java Development Kit (JDK) 17** o superior.
- **Apache Maven 3.8+** (o utilizar el wrapper de Maven si estuviese configurado).
- Los microservicios de destino corriendo localmente o en red accesible:
  - **ms-login / auth-ms** (Puerto 8082)
  - **ms-rag** (Puerto 8002)
  - **ms-admin** (Puerto 8083)

## Configuración y Variables de Entorno

El proyecto se configura mediante el archivo [application.yml](file:///home/gusgus/eclipse-workspace/Gateway/src/main/resources/application.yml). Las propiedades clave son:

- `server.port`: Puerto en el que escucha el Gateway (`8089`).
- `jwt.secret`: Clave secreta hexadecimal utilizada para la verificación de firmas JWT.
- `spring.cloud.gateway.routes`: Mapeo de rutas hacia los microservicios downstream.

## Guía de Inicio Rápido

### 1. Compilación
Para limpiar y compilar el proyecto generando el archivo ejecutable `.jar`:

```bash
mvn clean package -DskipTests
```

### 2. Ejecución local
Para iniciar el Gateway desde la línea de comandos:

```bash
mvn spring-boot:run
```

O ejecutando directamente el jar empaquetado:

```bash
java -jar target/Gateway-0.0.1-SNAPSHOT.jar
```

El Gateway estará escuchando en `http://localhost:8089`.

## Diagnóstico y Observabilidad

El Gateway incluye **Spring Boot Actuator** y exportación de métricas de **Prometheus / OpenTelemetry (OTLP)**. Los endpoints principales de salud y métricas son:

- **Salud del Sistema:** `http://localhost:8089/actuator/health`
- **Métricas Prometheus:** `http://localhost:8089/actuator/prometheus`

---

> **Nota para IA:** El Gateway es el primer punto de falla si el enrutamiento o el cifrado fallan. Asegúrate de verificar las configuraciones de puertos y rutas en el archivo `application.yml` antes de proponer cambios de infraestructura.

---

### Última revisión
- **Fecha:** 2026-05-25 01:20:13
- **Commit:** `364990c`

## Instrucciones para actualizar este doc
- Si cambias la estructura de archivos o configuración de ejecución rápida → actualiza `README.md`.
- Si cambia la estructura de archivos → actualiza `INDEX.md`.
- Cuando completes un cambio relevante → añade línea en `CHANGELOG.md`.

[← Volver al índice](INDEX.md)
