# Especificación API — Fragmind

## 1. Objetivo del documento

Este documento define una primera especificación de API para **Fragmind**, una aplicación de investigación asistida por IA que permite capturar fragmentos relevantes desde páginas web, organizarlos en workspaces y procesarlos mediante operaciones de inteligencia artificial.

La especificación está pensada para guiar la implementación del backend, la aplicación web y la extensión de navegador.

---

## 2. Alcance de la API V1

La API V1 debe cubrir el flujo principal del producto:

```text
Usuario se registra o inicia sesión
→ crea un workspace
→ guarda fragmentos desde la extensión
→ consulta y organiza fragmentos en la aplicación web
→ asigna etiquetas
→ solicita operaciones IA asíncronas
→ consulta el estado y resultado de las operaciones IA
```

### Incluido en V1

- Registro, login, logout y sesión de usuario.
- Consulta y actualización básica del perfil.
- Creación, listado, edición y eliminación lógica de workspaces.
- Captura de fragmentos desde extensión de navegador.
- Listado, búsqueda, filtrado, actualización y eliminación lógica de fragmentos.
- Gestión de etiquetas dentro de un workspace.
- Asignación y retirada de etiquetas a fragmentos.
- Creación y consulta de operaciones IA asíncronas.
- Consulta básica de cuotas de uso.
- Endpoints pensados para uso desde la extensión.

### Fuera de V1

- Colaboración entre usuarios.
- Roles avanzados y permisos por workspace.
- Subcarpetas o jerarquías internas.
- Notas independientes no asociadas a fragmentos.
- Scraping automático de páginas completas.
- Chat contextual persistente.
- Búsqueda semántica.
- Exportación a PDF, Markdown o TXT.
- Administración interna avanzada.

---

## 3. Principios generales de diseño

- La API será REST sobre HTTP.
- El formato de intercambio será JSON.
- Todos los endpoints privados requieren autenticación.
- El backend siempre debe validar propiedad y permisos, aunque el frontend o la extensión ya hayan validado previamente.
- Los recursos eliminados lógicamente no aparecen en listados normales.
- Las operaciones IA se crean de forma asíncrona y se consultan por estado.
- La IA no modifica automáticamente contenido del usuario sin confirmación.
- Los errores deben devolver códigos estables y mensajes comprensibles.
- La API debe poder ser consumida tanto por la aplicación web como por la extensión de navegador.

---

## 4. Convenciones técnicas

### 4.1 Base URL

```text
/api/v1
```

Ejemplo:

```text
https://api.fragmind.app/api/v1/workspaces
```

Para desarrollo local:

```text
http://localhost:8080/api/v1
```

---

### 4.2 Formato de fechas

Todas las fechas se enviarán en formato ISO 8601 UTC.

```json
"2026-05-19T17:30:00Z"
```

---

### 4.3 Identificadores

Todos los identificadores públicos de entidades serán UUID.

```json
"3f5b6d8c-8f7d-4a4a-9d0c-4b0c2d6d7a11"
```

---

### 4.4 Content-Type

Todas las peticiones con body deben enviar:

```http
Content-Type: application/json
```

Todas las respuestas devuelven:

```http
Content-Type: application/json
```

---

### 4.5 Autenticación

Para V1 se usará autenticación mediante **access token JWT de corta duración** y **refresh token**.

El access token se enviará en la cabecera `Authorization` como Bearer token.

```http
Authorization: Bearer <accessToken>
```

El refresh token se usará para obtener nuevos access tokens mediante el endpoint de refresco. El cliente debe almacenarlo de la forma más segura posible según el entorno de ejecución de la aplicación web o de la extensión.

---

### 4.6 Paginación

Los endpoints de listado usarán paginación por página.

Parámetros comunes:

```text
page: número de página, empezando en 0
size: tamaño de página
sort: campo de ordenación
direction: ASC | DESC
```

Respuesta paginada estándar:

```json
{
  "data": [],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 125,
    "totalPages": 7,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

## 5. Formato estándar de respuesta

### 5.1 Respuesta simple

```json
{
  "data": {
    "id": "resource-id"
  }
}
```

### 5.2 Respuesta paginada

```json
{
  "data": [],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

### 5.3 Respuesta de error

```json
{
  "error": {
    "code": "WORKSPACE_NOT_FOUND",
    "message": "Workspace not found.",
    "details": [
      {
        "field": "workspaceId",
        "message": "The workspace does not exist or does not belong to the authenticated user."
      }
    ],
    "traceId": "9f3c4f4a7b7e4a8f"
  }
}
```

---

## 6. Códigos HTTP

| Código | Uso |
|---:|---|
| 200 | Operación completada correctamente. |
| 201 | Recurso creado correctamente. |
| 202 | Operación aceptada para procesamiento asíncrono. |
| 204 | Operación completada sin body de respuesta. |
| 400 | Petición inválida o error de validación. |
| 401 | Usuario no autenticado o token inválido. |
| 403 | Usuario autenticado sin permisos sobre el recurso. |
| 404 | Recurso no encontrado o no accesible por el usuario. |
| 409 | Conflicto de negocio, como email duplicado o etiqueta duplicada. |
| 422 | Petición semánticamente válida pero no procesable por reglas de negocio. |
| 429 | Límite de uso alcanzado. |
| 500 | Error interno no controlado. |
| 503 | Servicio externo no disponible, por ejemplo proveedor IA. |

---

## 7. Códigos de error comunes

```text
VALIDATION_ERROR
UNAUTHENTICATED
INVALID_TOKEN
EXPIRED_TOKEN
ACCESS_DENIED
RESOURCE_NOT_FOUND
USER_DISABLED
USER_DELETED
EMAIL_ALREADY_REGISTERED
INVALID_CREDENTIALS
INVALID_CURRENT_PASSWORD
WORKSPACE_NOT_FOUND
WORKSPACE_ARCHIVED
WORKSPACE_DELETED
FRAGMENT_NOT_FOUND
FRAGMENT_ARCHIVED
FRAGMENT_DELETED
TAG_NOT_FOUND
TAG_ALREADY_EXISTS
AI_OPERATION_NOT_FOUND
AI_OPERATION_FAILED
AI_USAGE_LIMIT_REACHED
AI_PROVIDER_ERROR
AI_RESPONSE_INVALID
INSUFFICIENT_FRAGMENTS
CONTENT_TOO_LONG
```

---

# 8. Autenticación

## 8.1 Registrar usuario

```http
POST /auth/register
```

### Request

```json
{
  "email": "kevin@example.com",
  "password": "SecurePassword123!",
  "displayName": "Kevin"
}
```

### Validaciones

- `email` es obligatorio.
- `email` debe tener formato válido.
- `email` debe ser único.
- `password` es obligatoria.
- `password` debe cumplir requisitos mínimos de seguridad.
- `displayName` es opcional.

### Response 201

```json
{
  "data": {
    "user": {
      "id": "0c57b1a9-df22-47d0-b4f3-3fbc942cd7a1",
      "email": "kevin@example.com",
      "displayName": "Kevin",
      "defaultWorkspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
      "status": "ACTIVE",
      "emailVerified": false,
      "createdAt": "2026-05-19T17:30:00Z",
      "updatedAt": "2026-05-19T17:30:00Z"
    },
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi..."
  }
}
```

### Errores

| Código HTTP | Error |
|---:|---|
| 400 | `VALIDATION_ERROR` |
| 409 | `EMAIL_ALREADY_REGISTERED` |

---

## 8.2 Login

```http
POST /auth/login
```

### Request

```json
{
  "email": "kevin@example.com",
  "password": "SecurePassword123!"
}
```

### Response 200

```json
{
  "data": {
    "user": {
      "id": "0c57b1a9-df22-47d0-b4f3-3fbc942cd7a1",
      "email": "kevin@example.com",
      "displayName": "Kevin",
      "defaultWorkspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
      "status": "ACTIVE",
      "emailVerified": false
    },
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi..."
  }
}
```

### Errores

| Código HTTP | Error |
|---:|---|
| 400 | `VALIDATION_ERROR` |
| 401 | `INVALID_CREDENTIALS` |
| 403 | `USER_DISABLED`, `USER_DELETED` |

---

## 8.3 Refrescar token

```http
POST /auth/refresh
```

### Request

```json
{
  "refreshToken": "eyJhbGciOi..."
}
```

### Response 200

```json
{
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi..."
  }
}
```

---

## 8.4 Logout

```http
POST /auth/logout
```

### Request

```json
{
  "refreshToken": "eyJhbGciOi..."
}
```

### Response 204

Sin body.

---

# 9. Usuario y perfil

## 9.1 Obtener perfil

```http
GET /users/me
```

### Response 200

```json
{
  "data": {
    "id": "0c57b1a9-df22-47d0-b4f3-3fbc942cd7a1",
    "email": "kevin@example.com",
    "displayName": "Kevin",
    "defaultWorkspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "status": "ACTIVE",
    "emailVerified": false,
    "createdAt": "2026-05-19T17:30:00Z",
    "updatedAt": "2026-05-19T17:30:00Z"
  }
}
```

---

## 9.2 Actualizar perfil

```http
PATCH /users/me
```

### Alcance V1

En V1 este endpoint permite actualizar únicamente `displayName`. El cambio de email queda fuera de V1.

### Request

```json
{
  "displayName": "Kevin Joel"
}
```

### Response 200

```json
{
  "data": {
    "id": "0c57b1a9-df22-47d0-b4f3-3fbc942cd7a1",
    "email": "kevin@example.com",
    "displayName": "Kevin Joel",
    "defaultWorkspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "status": "ACTIVE",
    "emailVerified": false,
    "updatedAt": "2026-05-19T18:00:00Z"
  }
}
```

---

## 9.3 Cambiar workspace por defecto

```http
PUT /users/me/default-workspace
```

### Request

```json
{
  "workspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b"
}
```

### Reglas

- El workspace debe pertenecer al usuario autenticado.
- El workspace debe estar en estado `ACTIVE`.
- Un workspace `ARCHIVED` o `DELETED` no puede ser workspace por defecto.

### Response 200

```json
{
  "data": {
    "defaultWorkspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "updatedAt": "2026-05-19T18:05:00Z"
  }
}
```

---

## 9.4 Cambiar contraseña

```http
POST /users/me/change-password
```

### Request

```json
{
  "currentPassword": "SecurePassword123!",
  "newPassword": "NewSecurePassword123!"
}
```

### Response 204

Sin body.

### Errores

| Código HTTP | Error |
|---:|---|
| 400 | `VALIDATION_ERROR` |
| 401 | `UNAUTHENTICATED` |
| 422 | `INVALID_CURRENT_PASSWORD` |

---

## 9.5 Solicitar eliminación de cuenta

```http
DELETE /users/me
```

### Comportamiento

- La cuenta pasa a estado `DELETED`.
- Se registra `deletedAt`.
- El usuario deja de poder iniciar sesión de forma normal.
- La eliminación física se realiza según el periodo de retención definido.

### Response 204

Sin body.

---

# 10. Workspaces

## 10.1 Crear workspace

```http
POST /workspaces
```

### Request

```json
{
  "name": "Arquitectura de microservicios",
  "description": "Investigación sobre patrones, comunicación y despliegue de microservicios."
}
```

### Validaciones

- `name` es obligatorio.
- `name` no puede quedar vacío tras normalización.
- `description` es opcional.

### Response 201

```json
{
  "data": {
    "id": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "name": "Arquitectura de microservicios",
    "description": "Investigación sobre patrones, comunicación y despliegue de microservicios.",
    "status": "ACTIVE",
    "createdAt": "2026-05-19T18:10:00Z",
    "updatedAt": "2026-05-19T18:10:00Z"
  }
}
```

---

## 10.2 Listar workspaces

```http
GET /workspaces
```

### Query params

```text
status=ACTIVE
q=microservicios
page=0
size=20
sort=createdAt
direction=DESC
```

### Response 200

```json
{
  "data": [
    {
      "id": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
      "name": "Arquitectura de microservicios",
      "description": "Investigación sobre patrones, comunicación y despliegue de microservicios.",
      "status": "ACTIVE",
      "fragmentCount": 12,
      "createdAt": "2026-05-19T18:10:00Z",
      "updatedAt": "2026-05-19T18:10:00Z"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

## 10.3 Obtener workspace

```http
GET /workspaces/{workspaceId}
```

### Response 200

```json
{
  "data": {
    "id": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "name": "Arquitectura de microservicios",
    "description": "Investigación sobre patrones, comunicación y despliegue de microservicios.",
    "status": "ACTIVE",
    "fragmentCount": 12,
    "tagCount": 5,
    "createdAt": "2026-05-19T18:10:00Z",
    "updatedAt": "2026-05-19T18:10:00Z"
  }
}
```

---

## 10.4 Actualizar workspace

```http
PATCH /workspaces/{workspaceId}
```

### Request

```json
{
  "name": "Microservicios con Spring",
  "description": "Notas sobre arquitectura, patrones y despliegue."
}
```

### Response 200

```json
{
  "data": {
    "id": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "name": "Microservicios con Spring",
    "description": "Notas sobre arquitectura, patrones y despliegue.",
    "status": "ACTIVE",
    "updatedAt": "2026-05-19T18:20:00Z"
  }
}
```

---

## 10.5 Eliminar workspace

```http
DELETE /workspaces/{workspaceId}
```

### Comportamiento

- Se aplica eliminación lógica.
- El workspace pasa a `DELETED`.
- No aparece en listados normales.
- No puede recibir nuevos fragmentos.
- Sus fragmentos quedan ocultos del flujo normal por pertenecer a un workspace eliminado.

### Response 204

Sin body.

---

## 10.6 Archivar workspace

```http
POST /workspaces/{workspaceId}/archive
```

### Comportamiento

- El workspace pasa a `ARCHIVED`.
- No aparece en el selector rápido de la extensión.
- No puede ser workspace por defecto.
- No recibe nuevos fragmentos desde el flujo normal de captura.

### Response 200

```json
{
  "data": {
    "id": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "status": "ARCHIVED",
    "updatedAt": "2026-05-19T18:30:00Z"
  }
}
```

---

## 10.7 Restaurar workspace archivado

```http
POST /workspaces/{workspaceId}/restore
```

### Comportamiento

- El workspace pasa de `ARCHIVED` a `ACTIVE`.
- Puede volver a aparecer en listados normales y selector de extensión.

### Response 200

```json
{
  "data": {
    "id": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "status": "ACTIVE",
    "updatedAt": "2026-05-19T18:35:00Z"
  }
}
```

---

## 10.8 Resumen del workspace

```http
GET /workspaces/{workspaceId}/summary
```

### Uso

Endpoint de apoyo para pantallas de detalle. No ejecuta IA. Devuelve métricas simples del workspace.

### Response 200

```json
{
  "data": {
    "workspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "fragmentCount": 12,
    "tagCount": 5,
    "sourceDomainCount": 4,
    "lastFragmentCreatedAt": "2026-05-19T18:30:00Z"
  }
}
```

---

# 11. Fragmentos

## 11.1 Crear fragmento

```http
POST /workspaces/{workspaceId}/fragments
```

Este endpoint puede ser usado por la aplicación web. La extensión usa el endpoint específico de captura documentado en el módulo de extensión.

### Request

```json
{
  "selectedText": "Spring Cloud Gateway permite enrutar peticiones hacia distintos microservicios.",
  "sourceUrl": "https://example.com/spring-cloud-gateway",
  "sourceDomain": "example.com",
  "pageTitle": "Spring Cloud Gateway Guide",
  "faviconUrl": "https://example.com/favicon.ico",
  "extractedAt": "2026-05-19T18:25:00Z",
  "userNote": "Revisar para la parte de gateway.",
  "language": "es"
}
```

### Validaciones

- `selectedText` es obligatorio y no puede estar vacío.
- `sourceUrl` es obligatoria y debe ser una URL válida.
- `sourceDomain` es obligatorio o debe poder derivarse de `sourceUrl`.
- `workspaceId` debe pertenecer al usuario autenticado.
- El workspace debe estar `ACTIVE`; no se permite capturar en workspaces `ARCHIVED` o `DELETED`.
- `pageTitle`, `faviconUrl`, `userNote` y `language` son opcionales.
- La extensión puede enviar `language` si lo conoce, pero el backend puede validarlo, corregirlo o detectarlo automáticamente a partir de `selectedText`.

### Response 201

```json
{
  "data": {
    "id": "54c87995-f90b-4e68-b1df-c112ee9c80cb",
    "workspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "selectedText": "Spring Cloud Gateway permite enrutar peticiones hacia distintos microservicios.",
    "source": {
      "url": "https://example.com/spring-cloud-gateway",
      "domain": "example.com",
      "pageTitle": "Spring Cloud Gateway Guide",
      "faviconUrl": "https://example.com/favicon.ico"
    },
    "userNote": "Revisar para la parte de gateway.",
    "language": "es",
    "status": "ACTIVE",
    "extractedAt": "2026-05-19T18:25:00Z",
    "createdAt": "2026-05-19T18:25:03Z",
    "updatedAt": "2026-05-19T18:25:03Z",
    "tags": []
  }
}
```

### Errores esperados

| Código HTTP | Error |
|---:|---|
| 400 | `VALIDATION_ERROR` |
| 401 | `UNAUTHENTICATED` |
| 403 | `ACCESS_DENIED` |
| 404 | `WORKSPACE_NOT_FOUND` |
| 422 | `WORKSPACE_ARCHIVED`, `WORKSPACE_DELETED` |

---

## 11.2 Listar y buscar fragmentos de un workspace

```http
GET /workspaces/{workspaceId}/fragments
```

### Query params

```text
q=gateway
tagIds=5fd2d3a0-6dfd-4f06-8a38-a164c0e42131,af2e4a9d-bc53-4d6f-8c16-a946605fe13e
sourceDomain=example.com
language=es
createdFrom=2026-05-01T00:00:00Z
createdTo=2026-05-31T23:59:59Z
page=0
size=20
sort=createdAt
direction=DESC
```

### Response 200

```json
{
  "data": [
    {
      "id": "54c87995-f90b-4e68-b1df-c112ee9c80cb",
      "workspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
      "selectedText": "Spring Cloud Gateway permite enrutar peticiones hacia distintos microservicios.",
      "source": {
        "url": "https://example.com/spring-cloud-gateway",
        "domain": "example.com",
        "pageTitle": "Spring Cloud Gateway Guide",
        "faviconUrl": "https://example.com/favicon.ico"
      },
      "userNote": "Revisar para la parte de gateway.",
      "language": "es",
      "status": "ACTIVE",
      "extractedAt": "2026-05-19T18:25:00Z",
      "createdAt": "2026-05-19T18:25:03Z",
      "updatedAt": "2026-05-19T18:25:03Z",
      "tags": [
        {
          "id": "5fd2d3a0-6dfd-4f06-8a38-a164c0e42131",
          "name": "Spring",
          "color": "#6DB33F",
          "createdByAi": false
        }
      ]
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

## 11.3 Obtener fragmento

```http
GET /workspaces/{workspaceId}/fragments/{fragmentId}
```

### Response 200

```json
{
  "data": {
    "id": "54c87995-f90b-4e68-b1df-c112ee9c80cb",
    "workspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "selectedText": "Spring Cloud Gateway permite enrutar peticiones hacia distintos microservicios.",
    "source": {
      "url": "https://example.com/spring-cloud-gateway",
      "domain": "example.com",
      "pageTitle": "Spring Cloud Gateway Guide",
      "faviconUrl": "https://example.com/favicon.ico"
    },
    "userNote": "Revisar para la parte de gateway.",
    "language": "es",
    "status": "ACTIVE",
    "extractedAt": "2026-05-19T18:25:00Z",
    "createdAt": "2026-05-19T18:25:03Z",
    "updatedAt": "2026-05-19T18:25:03Z",
    "tags": []
  }
}
```

---

## 11.4 Actualizar fragmento

```http
PATCH /workspaces/{workspaceId}/fragments/{fragmentId}
```

### Request

```json
{
  "userNote": "Este fragmento puede servir para explicar el patrón gateway.",
  "language": "es"
}
```

### Campos editables en V1

```text
userNote
language
```

No se recomienda editar `selectedText`, `sourceUrl`, `sourceDomain` ni `extractedAt` en V1 para preservar fidelidad de captura.

### Response 200

```json
{
  "data": {
    "id": "54c87995-f90b-4e68-b1df-c112ee9c80cb",
    "workspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "userNote": "Este fragmento puede servir para explicar el patrón gateway.",
    "language": "es",
    "updatedAt": "2026-05-19T18:40:00Z"
  }
}
```

---

## 11.5 Mover fragmento a otro workspace

```http
POST /workspaces/{workspaceId}/fragments/{fragmentId}/move
```

### Request

```json
{
  "targetWorkspaceId": "d10a4a3e-f8c2-4630-b5d7-580083f63560"
}
```

### Reglas

- El fragmento debe pertenecer al usuario autenticado.
- El workspace origen debe pertenecer al usuario autenticado.
- El workspace destino debe pertenecer al usuario autenticado.
- El workspace destino debe estar `ACTIVE`.
- Las etiquetas actuales se eliminan si no existen en el workspace destino.

### Response 200

```json
{
  "data": {
    "id": "54c87995-f90b-4e68-b1df-c112ee9c80cb",
    "previousWorkspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "workspaceId": "d10a4a3e-f8c2-4630-b5d7-580083f63560",
    "updatedAt": "2026-05-19T18:45:00Z"
  }
}
```

---

## 11.6 Archivar fragmento

```http
POST /workspaces/{workspaceId}/fragments/{fragmentId}/archive
```

### Comportamiento

- El fragmento pasa a `ARCHIVED`.
- No aparece en listados principales.
- No se utiliza en operaciones IA por defecto.
- Puede restaurarse.

### Response 200

```json
{
  "data": {
    "id": "54c87995-f90b-4e68-b1df-c112ee9c80cb",
    "status": "ARCHIVED",
    "updatedAt": "2026-05-19T18:50:00Z"
  }
}
```

---

## 11.7 Restaurar fragmento

```http
POST /workspaces/{workspaceId}/fragments/{fragmentId}/restore
```

### Comportamiento

- El fragmento pasa a `ACTIVE` si pertenece a un workspace activo.
- En V1 este endpoint restaura fragmentos `ARCHIVED`.
- La restauración de fragmentos `DELETED` queda fuera del flujo normal y requeriría una papelera o listado específico en una versión futura.

### Response 200

```json
{
  "data": {
    "id": "54c87995-f90b-4e68-b1df-c112ee9c80cb",
    "status": "ACTIVE",
    "updatedAt": "2026-05-19T18:55:00Z"
  }
}
```

---

## 11.8 Eliminar fragmento

```http
DELETE /workspaces/{workspaceId}/fragments/{fragmentId}
```

### Comportamiento

- Se aplica eliminación lógica.
- El fragmento pasa a `DELETED`.
- No aparece en listados normales.
- No se utiliza en operaciones IA.

### Response 204

Sin body.

---

# 12. Etiquetas

## 12.1 Crear etiqueta

```http
POST /workspaces/{workspaceId}/tags
```

### Request

```json
{
  "name": "Spring",
  "color": "#6DB33F"
}
```

### Validaciones

- `name` es obligatorio.
- No puede existir otra etiqueta con el mismo nombre normalizado en el workspace.
- `color` es opcional.

### Response 201

```json
{
  "data": {
    "id": "5fd2d3a0-6dfd-4f06-8a38-a164c0e42131",
    "workspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "name": "Spring",
    "normalizedName": "spring",
    "color": "#6DB33F",
    "createdByAi": false,
    "createdAt": "2026-05-19T18:50:00Z",
    "updatedAt": "2026-05-19T18:50:00Z"
  }
}
```

---

## 12.2 Listar etiquetas de un workspace

```http
GET /workspaces/{workspaceId}/tags
```

### Response 200

```json
{
  "data": [
    {
      "id": "5fd2d3a0-6dfd-4f06-8a38-a164c0e42131",
      "workspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
      "name": "Spring",
      "normalizedName": "spring",
      "color": "#6DB33F",
      "createdByAi": false,
      "fragmentCount": 3,
      "createdAt": "2026-05-19T18:50:00Z",
      "updatedAt": "2026-05-19T18:50:00Z"
    }
  ]
}
```

---

## 12.3 Actualizar etiqueta

```http
PATCH /workspaces/{workspaceId}/tags/{tagId}
```

### Request

```json
{
  "name": "Spring Framework",
  "color": "#6DB33F"
}
```

### Response 200

```json
{
  "data": {
    "id": "5fd2d3a0-6dfd-4f06-8a38-a164c0e42131",
    "workspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "name": "Spring Framework",
    "normalizedName": "spring-framework",
    "color": "#6DB33F",
    "createdByAi": false,
    "updatedAt": "2026-05-19T18:55:00Z"
  }
}
```

---

## 12.4 Eliminar etiqueta

```http
DELETE /workspaces/{workspaceId}/tags/{tagId}
```

### Comportamiento

- La etiqueta deja de aparecer en el workspace.
- Se eliminan sus asignaciones a fragmentos.
- No elimina los fragmentos asociados.

### Response 204

Sin body.

---

## 12.5 Asignar etiquetas a fragmento

```http
POST /workspaces/{workspaceId}/fragments/{fragmentId}/tags
```

### Request

```json
{
  "tagIds": [
    "5fd2d3a0-6dfd-4f06-8a38-a164c0e42131",
    "af2e4a9d-bc53-4d6f-8c16-a946605fe13e"
  ]
}
```

### Comportamiento

- Añade las etiquetas indicadas al fragmento.
- No duplica asignaciones existentes.
- Todas las etiquetas deben pertenecer al mismo workspace que el fragmento.

### Response 200

```json
{
  "data": {
    "fragmentId": "54c87995-f90b-4e68-b1df-c112ee9c80cb",
    "tags": [
      {
        "id": "5fd2d3a0-6dfd-4f06-8a38-a164c0e42131",
        "name": "Spring",
        "color": "#6DB33F",
        "createdByAi": false
      }
    ]
  }
}
```

---

## 12.6 Reemplazar etiquetas de un fragmento

```http
PUT /workspaces/{workspaceId}/fragments/{fragmentId}/tags
```

### Request

```json
{
  "tagIds": [
    "5fd2d3a0-6dfd-4f06-8a38-a164c0e42131"
  ]
}
```

### Comportamiento

- Sustituye todas las etiquetas actuales por las enviadas.
- Si `tagIds` está vacío, el fragmento queda sin etiquetas.

### Response 200

```json
{
  "data": {
    "fragmentId": "54c87995-f90b-4e68-b1df-c112ee9c80cb",
    "tags": [
      {
        "id": "5fd2d3a0-6dfd-4f06-8a38-a164c0e42131",
        "name": "Spring",
        "color": "#6DB33F",
        "createdByAi": false
      }
    ]
  }
}
```

---

## 12.7 Quitar una etiqueta de un fragmento

```http
DELETE /workspaces/{workspaceId}/fragments/{fragmentId}/tags/{tagId}
```

### Response 204

Sin body.

---

# 13. Captura desde extensión de navegador

## 13.1 Obtener workspaces disponibles para la extensión

```http
GET /extension/workspaces
```

### Uso

Permite que la extensión muestre los workspaces activos del usuario al guardar un fragmento. Los workspaces archivados o eliminados no se devuelven en este selector.

### Response 200

```json
{
  "data": [
    {
      "id": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
      "name": "Arquitectura de microservicios",
      "description": "Investigación sobre patrones, comunicación y despliegue de microservicios.",
      "isDefault": true
    }
  ]
}
```

---

## 13.2 Capturar fragmento desde extensión

```http
POST /extension/captures
```

### Request

```json
{
  "workspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
  "selectedText": "Spring Cloud Gateway permite enrutar peticiones hacia distintos microservicios.",
  "sourceUrl": "https://example.com/spring-cloud-gateway",
  "sourceDomain": "example.com",
  "pageTitle": "Spring Cloud Gateway Guide",
  "faviconUrl": "https://example.com/favicon.ico",
  "extractedAt": "2026-05-19T18:25:00Z",
  "userNote": "Revisar más tarde.",
  "language": "es"
}
```

### Validaciones

- El usuario debe estar autenticado.
- `workspaceId` es obligatorio. Si el usuario no elige uno explícitamente, la extensión puede utilizar el workspace por defecto del usuario.
- `selectedText` no puede estar vacío.
- `sourceUrl` debe ser válida.
- `sourceDomain` debe coincidir con la URL o poder derivarse de ella.
- El workspace debe existir, estar activo y pertenecer al usuario.
- Si `pageTitle` o `faviconUrl` no están disponibles, la captura debe poder guardarse igualmente.
- La extensión puede enviar `language` si lo conoce, pero el backend puede validarlo, corregirlo o detectarlo automáticamente a partir de `selectedText`.

### Response 201

```json
{
  "data": {
    "fragmentId": "54c87995-f90b-4e68-b1df-c112ee9c80cb",
    "workspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "status": "CREATED",
    "message": "Fragment saved successfully.",
    "createdAt": "2026-05-19T18:25:03Z"
  }
}
```

### Errores esperados

| Código HTTP | Error |
|---:|---|
| 401 | `UNAUTHENTICATED`, `EXPIRED_TOKEN` |
| 400 | `VALIDATION_ERROR` |
| 404 | `WORKSPACE_NOT_FOUND` |
| 403 | `ACCESS_DENIED` |
| 422 | `WORKSPACE_ARCHIVED`, `WORKSPACE_DELETED` |

---

# 14. Operaciones IA

## 14.1 Crear operación IA

```http
POST /workspaces/{workspaceId}/ai-operations
```

### Request

```json
{
  "operationType": "WORKSPACE_SUMMARY",
  "inputScope": "WORKSPACE",
  "fragmentIds": [],
  "options": {
    "language": "es",
    "tone": "clear",
    "maxLength": "medium"
  }
}
```

### Tipos de operación IA MVP

```text
WORKSPACE_SUMMARY
GLOSSARY_GENERATION
MAIN_IDEAS_DETECTION
TAG_SUGGESTION
RESEARCH_QUERY_SUGGESTION
```

### Alcances de entrada

```text
WORKSPACE
```

`SELECTED_FRAGMENTS` queda reservado para una versión futura, cuando se permita ejecutar operaciones IA sobre fragmentos seleccionados manualmente.

### Validaciones

- El usuario debe estar autenticado.
- El workspace debe pertenecer al usuario.
- El workspace debe estar `ACTIVE`.
- La operación debe estar permitida por el plan/cuota del usuario.
- En V1, `inputScope` debe ser `WORKSPACE` y se usan los fragmentos activos del workspace.
- `fragmentIds` debe enviarse vacío o no utilizarse en V1.
- En V1 solo se envían fragmentos `ACTIVE` a la IA; los fragmentos `ARCHIVED` o `DELETED` no se procesan.
- Debe haber contenido suficiente para ejecutar la operación.

### Comportamiento

- Se crea una operación en estado `PENDING`.
- El backend encola el procesamiento asíncrono.
- La respuesta devuelve `202 Accepted`.
- El usuario puede consultar el estado con el endpoint de detalle.

### Response 202

```json
{
  "data": {
    "id": "94e3fbd6-4ec3-44a1-9e80-84c2a52e1818",
    "workspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "operationType": "WORKSPACE_SUMMARY",
    "inputScope": "WORKSPACE",
    "status": "PENDING",
    "createdAt": "2026-05-19T19:00:00Z",
    "updatedAt": "2026-05-19T19:00:00Z"
  }
}
```

### Errores esperados

| Código HTTP | Error |
|---:|---|
| 400 | `VALIDATION_ERROR` |
| 401 | `UNAUTHENTICATED` |
| 403 | `ACCESS_DENIED`, `USER_DISABLED`, `USER_DELETED` |
| 404 | `WORKSPACE_NOT_FOUND` |
| 422 | `INSUFFICIENT_FRAGMENTS`, `CONTENT_TOO_LONG` |
| 429 | `AI_USAGE_LIMIT_REACHED` |

---

## 14.2 Listar operaciones IA de un workspace

```http
GET /workspaces/{workspaceId}/ai-operations
```

### Query params

```text
status=COMPLETED
operationType=WORKSPACE_SUMMARY
page=0
size=20
sort=createdAt
direction=DESC
```

### Response 200

```json
{
  "data": [
    {
      "id": "94e3fbd6-4ec3-44a1-9e80-84c2a52e1818",
      "workspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
      "operationType": "WORKSPACE_SUMMARY",
      "inputScope": "WORKSPACE",
      "status": "COMPLETED",
      "createdAt": "2026-05-19T19:00:00Z",
      "startedAt": "2026-05-19T19:00:05Z",
      "completedAt": "2026-05-19T19:00:40Z",
      "updatedAt": "2026-05-19T19:00:40Z"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

## 14.3 Obtener operación IA

```http
GET /workspaces/{workspaceId}/ai-operations/{aiOperationId}
```

### Response 200: operación completada

```json
{
  "data": {
    "id": "94e3fbd6-4ec3-44a1-9e80-84c2a52e1818",
    "workspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "operationType": "WORKSPACE_SUMMARY",
    "inputScope": "WORKSPACE",
    "inputFragmentIds": [],
    "status": "COMPLETED",
    "result": {
      "format": "markdown",
      "content": "## Resumen\n\nLos fragmentos recopilados explican el papel de Spring Cloud Gateway..."
    },
    "errorCode": null,
    "errorMessage": null,
    "createdAt": "2026-05-19T19:00:00Z",
    "startedAt": "2026-05-19T19:00:05Z",
    "completedAt": "2026-05-19T19:00:40Z",
    "failedAt": null,
    "cancelledAt": null,
    "updatedAt": "2026-05-19T19:00:40Z"
  }
}
```

### Response 200: operación fallida

```json
{
  "data": {
    "id": "94e3fbd6-4ec3-44a1-9e80-84c2a52e1818",
    "workspaceId": "8dfeaa5e-ecb7-4374-b7b0-44d3d3ef996b",
    "operationType": "WORKSPACE_SUMMARY",
    "inputScope": "WORKSPACE",
    "status": "FAILED",
    "result": null,
    "errorCode": "AI_PROVIDER_ERROR",
    "errorMessage": "The AI provider could not process the request.",
    "createdAt": "2026-05-19T19:00:00Z",
    "startedAt": "2026-05-19T19:00:05Z",
    "failedAt": "2026-05-19T19:00:20Z",
    "updatedAt": "2026-05-19T19:00:20Z"
  }
}
```

---

## 14.4 Cancelar operación IA

```http
POST /workspaces/{workspaceId}/ai-operations/{aiOperationId}/cancel
```

### Comportamiento

- Solo se puede cancelar si la operación está en `PENDING` o si el sistema soporta cancelar operaciones en `PROCESSING`.
- La operación pasa a `CANCELLED`.
- Si el proveedor IA ya ha comenzado a procesar y no permite cancelación real, se marca como cancelación solicitada solo si técnicamente es posible.

### Response 200

```json
{
  "data": {
    "id": "94e3fbd6-4ec3-44a1-9e80-84c2a52e1818",
    "status": "CANCELLED",
    "cancelledAt": "2026-05-19T19:01:00Z",
    "updatedAt": "2026-05-19T19:01:00Z"
  }
}
```

---

## 14.5 Aplicar sugerencias de etiquetas generadas por IA

```http
POST /workspaces/{workspaceId}/ai-operations/{aiOperationId}/apply-tag-suggestions
```

### Uso

Este endpoint solo aplica para operaciones `TAG_SUGGESTION` completadas. Permite que el usuario acepte explícitamente sugerencias de etiquetas.

### Request

```json
{
  "tags": [
    {
      "name": "Spring Cloud",
      "color": "#6DB33F"
    },
    {
      "name": "Gateway",
      "color": null
    }
  ],
  "assignments": [
    {
      "fragmentId": "54c87995-f90b-4e68-b1df-c112ee9c80cb",
      "tagNames": ["Spring Cloud", "Gateway"]
    }
  ]
}
```

### Comportamiento

- Crea etiquetas que no existan en el workspace.
- Reutiliza etiquetas existentes si coinciden por nombre normalizado.
- Asigna las etiquetas a los fragmentos indicados.
- Requiere confirmación explícita del usuario mediante esta llamada.

### Response 200

```json
{
  "data": {
    "createdTags": [
      {
        "id": "4ef97ba0-15ad-49f3-aeb7-27f5f2291bb9",
        "name": "Spring Cloud",
        "createdByAi": true
      }
    ],
    "updatedFragments": [
      {
        "fragmentId": "54c87995-f90b-4e68-b1df-c112ee9c80cb",
        "tagCount": 2
      }
    ]
  }
}
```

---

## 14.6 Mapeo entre operación IA y cuota consumida

Cada operación IA consume una funcionalidad limitable concreta.

| Operación IA | Feature de cuota |
|---|---|
| `WORKSPACE_SUMMARY` | `AI_SUMMARY` |
| `GLOSSARY_GENERATION` | `AI_GLOSSARY` |
| `MAIN_IDEAS_DETECTION` | `AI_MAIN_IDEAS` |
| `TAG_SUGGESTION` | `AI_TAG_SUGGESTION` |
| `RESEARCH_QUERY_SUGGESTION` | `AI_RESEARCH_QUERY_SUGGESTION` |

---

# 15. Cuotas y límites de uso

## 15.1 Obtener cuota actual del usuario

```http
GET /usage/quotas
```

### Response 200

```json
{
  "data": [
    {
      "feature": "AI_SUMMARY",
      "period": "MONTHLY",
      "limit": 10,
      "used": 3,
      "remaining": 7,
      "resetAt": "2026-06-01T00:00:00Z"
    },
    {
      "feature": "AI_GLOSSARY",
      "period": "MONTHLY",
      "limit": 10,
      "used": 1,
      "remaining": 9,
      "resetAt": "2026-06-01T00:00:00Z"
    }
  ]
}
```

---

## 15.2 Obtener cuota por funcionalidad

```http
GET /usage/quotas/{feature}
```

### Ejemplo

```http
GET /usage/quotas/AI_SUMMARY
```

### Response 200

```json
{
  "data": {
    "feature": "AI_SUMMARY",
    "period": "MONTHLY",
    "limit": 10,
    "used": 3,
    "remaining": 7,
    "resetAt": "2026-06-01T00:00:00Z"
  }
}
```

---

# 16. Planes

## 16.1 Obtener plan actual del usuario

```http
GET /plans/me
```

### Response 200

```json
{
  "data": {
    "plan": {
      "id": "88d92e72-65fc-4c83-a138-bff1aa640061",
      "name": "Free",
      "type": "FREE",
      "price": 0,
      "billingPeriod": "NONE",
      "isActive": true
    },
    "userPlan": {
      "status": "ACTIVE",
      "startedAt": "2026-05-19T17:30:00Z",
      "expiresAt": null
    }
  }
}
```

---

## 16.2 Listar planes disponibles

```http
GET /plans
```

### Response 200

```json
{
  "data": [
    {
      "id": "88d92e72-65fc-4c83-a138-bff1aa640061",
      "name": "Free",
      "type": "FREE",
      "price": 0,
      "billingPeriod": "NONE",
      "isActive": true
    },
    {
      "id": "2e7d2c60-ffba-42a1-94e0-843252f30901",
      "name": "Paid",
      "type": "PAID",
      "price": null,
      "billingPeriod": "MONTHLY",
      "isActive": false
    }
  ]
}
```

---

# 17. Schemas principales

## 17.1 UserResponse

```json
{
  "id": "uuid",
  "email": "string",
  "displayName": "string | null",
  "defaultWorkspaceId": "uuid | null",
  "status": "ACTIVE | DISABLED | DELETED",
  "emailVerified": false,
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

---

## 17.2 WorkspaceResponse

```json
{
  "id": "uuid",
  "name": "string",
  "description": "string | null",
  "status": "ACTIVE | ARCHIVED | DELETED",
  "fragmentCount": 0,
  "tagCount": 0,
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

---

## 17.3 FragmentResponse

```json
{
  "id": "uuid",
  "workspaceId": "uuid",
  "selectedText": "string",
  "source": {
    "url": "string",
    "domain": "string",
    "pageTitle": "string | null",
    "faviconUrl": "string | null"
  },
  "userNote": "string | null",
  "language": "string | null",
  "status": "ACTIVE | ARCHIVED | DELETED",
  "extractedAt": "datetime",
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "tags": []
}
```

---

## 17.4 TagResponse

```json
{
  "id": "uuid",
  "workspaceId": "uuid",
  "name": "string",
  "normalizedName": "string",
  "color": "string | null",
  "createdByAi": false,
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

---

## 17.5 AiOperationResponse

```json
{
  "id": "uuid",
  "workspaceId": "uuid",
  "operationType": "WORKSPACE_SUMMARY | GLOSSARY_GENERATION | MAIN_IDEAS_DETECTION | TAG_SUGGESTION | RESEARCH_QUERY_SUGGESTION",
  "inputScope": "WORKSPACE",
  "inputFragmentIds": [],
  "status": "PENDING | PROCESSING | COMPLETED | FAILED | CANCELLED",
  "result": "object | null",
  "errorCode": "string | null",
  "errorMessage": "string | null",
  "createdAt": "datetime",
  "startedAt": "datetime | null",
  "completedAt": "datetime | null",
  "failedAt": "datetime | null",
  "cancelledAt": "datetime | null",
  "updatedAt": "datetime"
}
```

---

# 18. Enumeraciones

## 18.1 UserStatus

```text
ACTIVE
DISABLED
DELETED
```

## 18.2 WorkspaceStatus

```text
ACTIVE
ARCHIVED
DELETED
```

## 18.3 FragmentStatus

```text
ACTIVE
ARCHIVED
DELETED
```

## 18.4 AiOperationStatus

```text
PENDING
PROCESSING
COMPLETED
FAILED
CANCELLED
```

## 18.5 AiOperationType

```text
WORKSPACE_SUMMARY
GLOSSARY_GENERATION
MAIN_IDEAS_DETECTION
TAG_SUGGESTION
RESEARCH_QUERY_SUGGESTION
```

## 18.6 AiInputScope

```text
WORKSPACE
```

`SELECTED_FRAGMENTS` queda reservado para una versión futura.

## 18.7 PlanType

```text
FREE
PAID
```

## 18.8 UserPlanStatus

```text
ACTIVE
EXPIRED
CANCELLED
```

## 18.9 BillingPeriod

```text
NONE
MONTHLY
YEARLY
```

## 18.10 UsagePeriod

```text
DAILY
MONTHLY
YEARLY
LIFETIME
```

## 18.11 LimitableFeature

```text
AI_SUMMARY
AI_GLOSSARY
AI_TAG_SUGGESTION
AI_MAIN_IDEAS
AI_RESEARCH_QUERY_SUGGESTION
WORKSPACE_CREATION
FRAGMENT_STORAGE
```

`PDF_EXPORT` queda reservado para versiones futuras, ya que la exportación a PDF, Markdown o TXT está fuera de V1.

---

# 19. Seguridad y permisos

## 19.1 Reglas generales

- Todos los endpoints privados requieren autenticación.
- El backend debe validar propiedad del recurso en cada operación.
- El usuario solo puede acceder a sus propios workspaces.
- El usuario solo puede acceder a fragmentos de sus workspaces.
- El usuario solo puede gestionar etiquetas de sus workspaces.
- El usuario solo puede ejecutar IA sobre sus workspaces.
- Un usuario `DISABLED` o `DELETED` no puede acceder a módulos privados.
- Agotar la cuota de IA no cambia el estado del usuario.
- No se debe confiar únicamente en IDs enviados por cliente.

---

## 19.2 CORS para extensión

La extensión de navegador necesitará autorización CORS específica.

Configuración recomendada:

```text
Permitir únicamente los orígenes de la aplicación web y el identificador/origen de la extensión publicada.
No permitir comodines en producción.
Permitir métodos necesarios: GET, POST, PATCH, PUT, DELETE, OPTIONS.
Permitir cabeceras: Authorization, Content-Type.
```

---

## 19.3 Rate limiting

Se recomienda aplicar rate limiting por usuario/IP en:

```text
POST /auth/login
POST /auth/register
POST /extension/captures
POST /workspaces/{workspaceId}/ai-operations
```

---

# 20. Validaciones transversales

## 20.1 Texto seleccionado

```text
selectedText obligatorio
trim(selectedText) no vacío
longitud máxima configurable
no debe aceptarse una página completa como uso normal de V1
```

## 20.2 URLs

```text
sourceUrl obligatoria
sourceUrl debe tener formato URL válido
sourceDomain debe derivarse o validarse contra sourceUrl
faviconUrl opcional, pero si se informa debe ser URL válida
```

## 20.3 Nombres

```text
Workspace.name obligatorio
Tag.name obligatorio
normalizar nombres para evitar duplicados visuales
```

## 20.4 IA

```text
No procesar fragmentos archivados ni eliminados
No procesar workspaces archivados ni eliminados
Validar cuota antes de crear operación
Registrar operación antes de enviar a proveedor IA
Registrar fallos controlados
```

---

# 21. Flujos principales

## 21.1 Flujo de captura desde extensión

```text
1. La extensión obtiene sesión del usuario.
2. La extensión solicita GET /extension/workspaces.
3. El usuario selecciona texto en una página.
4. El usuario elige workspace destino.
5. La extensión envía POST /extension/captures.
6. El backend valida autenticación, workspace y contenido.
7. El backend guarda Fragment con SourceMetadata.
8. El backend devuelve fragmentId.
9. La extensión muestra confirmación.
```

---

## 21.2 Flujo de resumen con IA

```text
1. El usuario abre un workspace.
2. Solicita resumen del workspace.
3. Frontend llama POST /workspaces/{workspaceId}/ai-operations.
4. Backend valida permisos y cuota.
5. Backend crea AiOperation en PENDING.
6. Backend encola procesamiento asíncrono.
7. Frontend consulta GET /workspaces/{workspaceId}/ai-operations/{aiOperationId}.
8. La operación pasa a PROCESSING.
9. La operación termina en COMPLETED o FAILED.
10. El usuario revisa el resultado.
```

---

## 21.3 Flujo de sugerencia de etiquetas con IA

```text
1. El usuario solicita TAG_SUGGESTION.
2. Backend crea operación IA asíncrona.
3. IA devuelve sugerencias de etiquetas y posibles asignaciones.
4. El usuario revisa sugerencias.
5. El usuario acepta algunas sugerencias.
6. Frontend llama apply-tag-suggestions.
7. Backend crea/reutiliza etiquetas y las asigna a fragmentos.
```

---

# 22. Endpoints resumidos

## Autenticación

```text
POST   /auth/register
POST   /auth/login
POST   /auth/refresh
POST   /auth/logout
```

## Usuario

```text
GET    /users/me
PATCH  /users/me
PUT    /users/me/default-workspace
POST   /users/me/change-password
DELETE /users/me
```

## Workspaces

```text
POST   /workspaces
GET    /workspaces
GET    /workspaces/{workspaceId}
PATCH  /workspaces/{workspaceId}
DELETE /workspaces/{workspaceId}
POST   /workspaces/{workspaceId}/archive
POST   /workspaces/{workspaceId}/restore
GET    /workspaces/{workspaceId}/summary
```

## Fragmentos

```text
POST   /workspaces/{workspaceId}/fragments
GET    /workspaces/{workspaceId}/fragments
GET    /workspaces/{workspaceId}/fragments/{fragmentId}
PATCH  /workspaces/{workspaceId}/fragments/{fragmentId}
POST   /workspaces/{workspaceId}/fragments/{fragmentId}/move
POST   /workspaces/{workspaceId}/fragments/{fragmentId}/archive
POST   /workspaces/{workspaceId}/fragments/{fragmentId}/restore
DELETE /workspaces/{workspaceId}/fragments/{fragmentId}
```

## Etiquetas

```text
POST   /workspaces/{workspaceId}/tags
GET    /workspaces/{workspaceId}/tags
PATCH  /workspaces/{workspaceId}/tags/{tagId}
DELETE /workspaces/{workspaceId}/tags/{tagId}
POST   /workspaces/{workspaceId}/fragments/{fragmentId}/tags
PUT    /workspaces/{workspaceId}/fragments/{fragmentId}/tags
DELETE /workspaces/{workspaceId}/fragments/{fragmentId}/tags/{tagId}
```

## Extensión

```text
GET    /extension/workspaces
POST   /extension/captures
```

## IA

```text
POST   /workspaces/{workspaceId}/ai-operations
GET    /workspaces/{workspaceId}/ai-operations
GET    /workspaces/{workspaceId}/ai-operations/{aiOperationId}
POST   /workspaces/{workspaceId}/ai-operations/{aiOperationId}/cancel
POST   /workspaces/{workspaceId}/ai-operations/{aiOperationId}/apply-tag-suggestions
```

## Cuotas y planes

```text
GET    /usage/quotas
GET    /usage/quotas/{feature}
GET    /plans/me
GET    /plans
```

---

# 23. Decisiones de V1

## 23.1 Autenticación

La API V1 utiliza JWT con access token de corta duración y refresh token.

## 23.2 Límites exactos del plan gratuito

Queda pendiente definir el número exacto de operaciones IA incluidas y los límites por tipo de operación tras analizar el coste real de uso.

## 23.3 Resultados IA

Para V1, los resultados de IA se devolverán como objeto flexible con formato y contenido.

```json
{
  "format": "markdown",
  "content": "..."
}
```

---

# 24. Conclusión

La API V1 de Fragmind se centra en habilitar el flujo esencial del producto:

1. Autenticación de usuario.
2. Gestión de workspaces.
3. Captura de fragmentos desde la extensión.
4. Organización mediante etiquetas, búsqueda y filtros.
5. Operaciones IA asíncronas con control de estado y límites de uso.

La especificación evita introducir complejidad fuera del MVP y mantiene una base coherente para implementar autenticación, workspaces, fragmentos, etiquetas, cuotas y operaciones IA asíncronas.
