# SmartLogix · BFF-Service

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen)

**Backend for Frontend** de SmartLogix. Actúa como capa de agregación exclusiva para el dashboard del frontend: recibe una sola petición del cliente, ejecuta en **paralelo** las llamadas a **User-Service** e **Inventory-Service**, combina ambas respuestas y las devuelve en un único payload. Elimina los múltiples round-trips del navegador sin modificar los microservicios de dominio ni el Api-Gateway.

## Características

- Endpoint único `GET /bff/dashboard` que agrega datos de usuarios e inventario.
- Llamadas internas en **paralelo** (`CompletableFuture`) para minimizar la latencia total.
- Reenvío del **JWT** recibido del frontend a cada microservicio — la validación la realizan los servicios destino con su propio `JwtFilter`.
- Sin base de datos, sin caché propia, sin broker de mensajes — servicio liviano de solo agregación.
- Documentación **OpenAPI / Swagger UI** integrada.

## Requisitos previos

- **Java 21** (JDK).
- Maven Wrapper incluido (`mvnw` / `mvnw.cmd`).
- **User-Service** (puerto 8084) e **Inventory-Service** (puerto 8081) accesibles en la red.

## Configuración

La configuración está en [src/main/resources/application.properties](src/main/resources/application.properties). Se sobreescribe por variables de entorno:

| Variable | Por defecto | Descripción |
|----------|-------------|-------------|
| `BFF_SERVICES_USUARIO-URL` | `http://localhost:8084` | URL base de User-Service |
| `BFF_SERVICES_INVENTARIO-URL` | `http://localhost:8081` | URL base de Inventory-Service |
| `BFF_CORS_ALLOWED-ORIGINS` | `http://localhost,http://localhost:5173` | Orígenes permitidos por CORS (separados por coma) |

> Las URLs internas usan los nombres de servicio Docker (`ms-usuario`, `ms-inventario`) cuando se levanta con Docker Compose.

## Instalación

```bash
./mvnw clean package -DskipTests      # Linux/Mac
.\mvnw.cmd clean package -DskipTests  # Windows (PowerShell)
```

## Ejecución

```bash
./mvnw spring-boot:run        # Linux/Mac
.\mvnw.cmd spring-boot:run    # Windows (PowerShell)
```

El servicio queda disponible en **http://localhost:8086**.

- Swagger UI: http://localhost:8086/swagger-ui.html
- Health check: http://localhost:8086/actuator/health

### Docker (recomendado: vía Docker Compose)

La forma recomendada es el **Docker Compose del monorepo**, que gestiona la red interna y las dependencias de arranque:

```bash
# Desde la raíz del monorepo SmartLogix (junto a Docker-compose.yml)
docker compose up bff-service   # solo el BFF (levanta también ms-usuario y ms-inventario)
docker compose up               # toda la plataforma
```

> El BFF depende de que `ms-usuario` y `ms-inventario` estén healthy. Docker Compose lo garantiza mediante `depends_on` con healthcheck.

## Endpoints

> El frontend accede al BFF directamente en el puerto 8086, **sin pasar por el Api-Gateway**.

### BFF — `/bff`

| Método | Endpoint | Auth | Descripción |
|--------|----------|------|-------------|
| GET | `/bff/dashboard` | Bearer JWT | Devuelve en una sola respuesta los datos de usuarios e inventario para el dashboard |

**Ejemplo de petición:**

```bash
curl -X GET http://localhost:8086/bff/dashboard \
  -H "Authorization: Bearer <token>"
```

**Ejemplo de respuesta:**

```json
{
  "usuarios": [...],
  "usuariosPorRol": [
    { "rol": "ADMIN", "cantidad": 2 },
    { "rol": "OPERADOR", "cantidad": 5 }
  ],
  "productos": [...],
  "stockCritico": [
    { "id": 3, "nombre": "Cable HDMI", "stock": 2, "stockMinimo": 10, "deficit": 8 }
  ],
  "ultimosRegistrados": [...],
  "totalProductos": 128,
  "cantidadStockBajo": 5,
  "valorInventario": 4582000
}
```

## Pruebas

No hay Maven global; usa el wrapper. En Windows, `mvnw` necesita `JAVA_HOME`:

```powershell
# PowerShell (Windows)
$env:JAVA_HOME = Split-Path -Parent (Split-Path -Parent (Get-Command java).Source)
.\mvnw.cmd test
```

```bash
# Linux/Mac
./mvnw test
```

Las 5 pruebas (JUnit 5 + Mockito + AssertJ) cubren el controlador (`BffControllerTest`) con MockMvc, el servicio (`BffServiceTest`) con mocks de `RestClient`, y la carga del contexto (`BffApplicationTests`).

## Estructura

```
src/main/java/com/smartlogix/bff/
├── BffApplication.java
├── config/        # RestClientConfig (beans RestClient), CorsConfig
├── controller/    # BffController — GET /bff/dashboard
└── service/       # BffService — llamadas paralelas + agregación
```

## Rol en la arquitectura

```
frontend_ms (React)
      │
      │ GET /bff/dashboard  (puerto 8086)
      ▼
  BFF-Service  ◄──── este servicio
      │
      ├── GET /usuarios/dashboard      → User-Service (8084)   ┐ en paralelo
      └── GET /inventario/dashboard/resumen → Inventory-Service (8081) ┘
```

El Api-Gateway (8080) sigue siendo el punto de entrada para todas las operaciones CRUD del frontend. El BFF es una capa adicional exclusiva para la agregación del dashboard.
