# Modelos de dominio — Fragmind

## 1. Objetivo del documento

Este documento define los modelos de dominio iniciales necesarios para construir **Fragmind**, una aplicación de investigación asistida por IA centrada en capturar fragmentos relevantes desde la web, organizarlos en workspaces y procesarlos mediante operaciones de inteligencia artificial.

El documento se basa en los conceptos, reglas y entidades definidos en `product-brief.md` y `functional-specification.md`.

---

## 2. Principios de modelado

Los modelos de dominio se han definido siguiendo estos principios:

- El **workspace** es el agregado principal de investigación.
- El usuario es propietario de sus recursos.
- Los fragmentos son el contenido central del producto.
- La fuente original de cada fragmento debe conservarse siempre.
- Las etiquetas organizan fragmentos dentro de un workspace.
- Las operaciones de IA se ejecutan de forma asíncrona y no modifican automáticamente el contenido del usuario.
- La eliminación lógica se aplica a las entidades principales.
- Los límites de uso y planes no forman parte del estado principal del usuario.
- El modelo debe permitir controlar planes, cuotas de uso y operaciones de IA sin mezclar estas responsabilidades con el estado principal del usuario.

---

## 3. Vista general del dominio

```text
User
 ├── Workspace
 │    ├── Fragment
 │    │    ├── SourceMetadata
 │    │    └── FragmentTag
 │    ├── Tag
 │    └── AiOperation
 ├── UserPlan
 │    └── Plan
 └── UsageQuota
```

Relaciones principales:

- Un usuario puede tener múltiples workspaces.
- Un workspace pertenece a un único usuario.
- Un workspace puede contener múltiples fragmentos.
- Un fragmento pertenece a un usuario y a un workspace.
- Un fragmento contiene metadatos de fuente.
- Un workspace puede tener múltiples etiquetas.
- Un fragmento puede tener múltiples etiquetas.
- Una operación IA pertenece a un usuario y normalmente a un workspace.
- Una operación IA puede estar asociada a todos los fragmentos activos de un workspace o a una selección concreta de fragmentos.
- Un usuario puede tener un plan activo.
- Un usuario tiene cuotas de uso asociadas a funcionalidades limitables.

---

## 4. Agregados principales

### 4.1 User

Representa a una persona registrada en Fragmind.

#### Responsabilidades

- Identificar al usuario dentro del sistema.
- Mantener la información mínima de perfil.
- Controlar el estado de acceso a la aplicación.
- Actuar como propietario de workspaces, fragmentos, operaciones IA y cuotas.

#### Campos

| Campo | Tipo sugerido | Obligatorio | Descripción |
|---|---:|:---:|---|
| id | UUID | Sí | Identificador único del usuario. |
| email | EmailAddress | Sí | Email único del usuario. |
| passwordHash | String | Sí | Contraseña cifrada o hasheada. Nunca texto plano. |
| displayName | String | No | Nombre visible del usuario. |
| defaultWorkspaceId | UUID | No | Workspace usado por defecto para capturas rápidas. |
| status | UserStatus | Sí | Estado de la cuenta. |
| emailVerified | Boolean | No | Indica si el email está verificado. En V1 no bloquea el uso. |
| emailVerifiedAt | Instant | No | Fecha de verificación del email, si existe. |
| createdAt | Instant | Sí | Fecha de creación. |
| updatedAt | Instant | Sí | Fecha de última actualización. |
| deletedAt | Instant | No | Fecha de eliminación lógica. |

#### Estados

```text
ACTIVE
DISABLED
DELETED
```

#### Reglas de negocio

- El email debe ser único.
- El email debe tener formato válido.
- La contraseña debe cumplir requisitos mínimos de seguridad.
- Un usuario recién registrado queda en estado `ACTIVE`.
- Un usuario `DISABLED` no puede acceder a módulos privados.
- Un usuario `DELETED` no puede iniciar sesión ni acceder a módulos privados.
- La eliminación de usuario en V1 es lógica.
- Un usuario `DELETED` puede solicitar reactivación mientras no se haya ejecutado la eliminación física.
- Mientras la cuenta esté en estado `DELETED`, el email no podrá reutilizarse.
- Agotar la cuota de IA no cambia el estado del usuario.
- Al registrar un usuario, el sistema crea un workspace inicial y lo asigna como `defaultWorkspaceId`.

#### Invariantes

- `email` no puede estar vacío.
- `passwordHash` no puede estar vacío.
- `status` nunca puede ser nulo.
- Si `status = DELETED`, `deletedAt` debería estar informado.

---

### 4.2 Workspace

Representa un espacio de investigación sobre un tema concreto.

#### Responsabilidades

- Agrupar fragmentos relacionados con una investigación.
- Agrupar etiquetas propias del workspace.
- Servir como contexto principal para operaciones de IA.

#### Campos

| Campo | Tipo sugerido | Obligatorio | Descripción |
|---|---:|:---:|---|
| id | UUID | Sí | Identificador único del workspace. |
| userId | UUID | Sí | Usuario propietario. |
| name | String | Sí | Nombre del workspace. |
| description | String | No | Descripción opcional. |
| status | WorkspaceStatus | Sí | Estado del workspace. |
| createdAt | Instant | Sí | Fecha de creación. |
| updatedAt | Instant | Sí | Fecha de última actualización. |
| deletedAt | Instant | No | Fecha de eliminación lógica. |

#### Estados

```text
ACTIVE
ARCHIVED
DELETED
```

#### Reglas de negocio

- Un workspace pertenece a un único usuario.
- Un usuario puede tener múltiples workspaces.
- Un usuario tiene un workspace por defecto para capturas rápidas.
- El nombre del workspace es obligatorio.
- El usuario no puede acceder a workspaces de otros usuarios.
- Un workspace archivado no aparece en el selector rápido de la extensión ni puede ser destino por defecto.
- Un workspace archivado puede restaurarse a `ACTIVE`.
- Un workspace eliminado no aparece en listados normales.
- Un workspace eliminado no puede recibir nuevos fragmentos.
- En V1 no existen subcarpetas ni jerarquías internas.
- La organización dentro del workspace se realiza mediante etiquetas, búsqueda y filtros.

#### Invariantes

- `userId` no puede estar vacío.
- `name` debe tener contenido útil tras normalización.
- Si `status = DELETED`, `deletedAt` debería estar informado.

---

### 4.3 Fragment

Representa un fragmento de texto seleccionado por el usuario desde una página web.

#### Responsabilidades

- Guardar el texto relevante seleccionado por el usuario.
- Conservar la fuente original.
- Permitir organización mediante etiquetas.
- Servir como unidad principal de entrada para operaciones IA.

#### Campos

| Campo | Tipo sugerido | Obligatorio | Descripción |
|---|---:|:---:|---|
| id | UUID | Sí | Identificador único del fragmento. |
| userId | UUID | Sí | Usuario propietario. |
| workspaceId | UUID | Sí | Workspace al que pertenece. |
| selectedText | Text | Sí | Texto seleccionado por el usuario. |
| source | SourceMetadata | Sí | Metadatos de la fuente original. |
| userNote | Text | No | Nota personal asociada al fragmento. No es una nota independiente y puede usarse como contexto adicional para IA. |
| language | String | No | Idioma detectado automáticamente, preferiblemente en backend. |
| status | FragmentStatus | Sí | Estado del fragmento. |
| extractedAt | Instant | Sí | Momento en que se capturó el fragmento desde la web. |
| createdAt | Instant | Sí | Momento en que se guardó el fragmento en Fragmind. |
| updatedAt | Instant | Sí | Fecha de última actualización. |
| deletedAt | Instant | No | Fecha de eliminación lógica. |

#### Estados

```text
ACTIVE
ARCHIVED
DELETED
```

#### Reglas de negocio

- Todo fragmento pertenece a un usuario.
- Todo fragmento pertenece a un workspace.
- El texto seleccionado no puede estar vacío.
- Todo fragmento debe conservar URL, dominio y fecha de extracción.
- El usuario solo puede acceder a sus propios fragmentos.
- Un fragmento archivado no aparece en listados principales.
- Un fragmento archivado no se envía a operaciones IA en V1.
- Un fragmento eliminado no aparece en listados normales.
- Un fragmento eliminado no se envía a operaciones IA.
- No se puede guardar un fragmento en un workspace archivado o eliminado desde el flujo normal de captura.
- La extensión solo debe enviar el texto seleccionado, no páginas completas en V1.
- Las notas personales solo existen asociadas a fragmentos concretos en V1.
- `language` se detecta preferiblemente en backend a partir de `selectedText`; si la extensión lo envía, el backend puede validarlo, corregirlo o dejarlo sin informar cuando no haya confianza suficiente.

#### Invariantes

- `selectedText` debe tener contenido útil tras normalización.
- `source.url` no puede estar vacío.
- `source.domain` no puede estar vacío.
- `extractedAt` no puede ser nulo.
- `workspaceId` debe apuntar a un workspace activo del mismo usuario.
- Si `status = DELETED`, `deletedAt` debería estar informado.

---

### 4.4 SourceMetadata

Objeto de valor que representa la información de origen de un fragmento.

#### Responsabilidades

- Conservar la URL original del fragmento.
- Conservar el dominio de la página.
- Guardar metadatos opcionales de visualización.

#### Campos

| Campo | Tipo sugerido | Obligatorio | Descripción |
|---|---:|:---:|---|
| url | URL | Sí | URL original de la página. |
| domain | String | Sí | Dominio derivado de la URL. |
| pageTitle | String | No | Título de la página. |
| faviconUrl | URL | No | URL del favicon. |

#### Reglas de negocio

- La URL de origen es obligatoria.
- El dominio debe derivarse automáticamente de la URL.
- El título de la página es opcional.
- El favicon es opcional.
- Si no se puede obtener título o favicon, el fragmento debe guardarse igualmente.
- La aplicación no debe depender de scraping avanzado para obtener estos metadatos.

#### Invariantes

- `url` debe ser válida.
- `domain` debe ser válido y coherente con `url`.

#### Nota de diseño

`SourceMetadata` puede implementarse como un objeto embebido dentro de `Fragment` en lugar de una entidad independiente, al menos en V1. Esto simplifica el modelo porque la fuente existe principalmente como metadato asociado al fragmento.


---

### 4.5 Tag

Representa una etiqueta usada para clasificar fragmentos dentro de un workspace.

#### Responsabilidades

- Clasificar fragmentos dentro de un workspace.
- Permitir filtrado y organización.
- Diferenciar etiquetas creadas manualmente y sugeridas por IA.

#### Campos

| Campo | Tipo sugerido | Obligatorio | Descripción |
|---|---:|:---:|---|
| id | UUID | Sí | Identificador único de la etiqueta. |
| workspaceId | UUID | Sí | Workspace al que pertenece. |
| name | String | Sí | Nombre visible de la etiqueta. |
| normalizedName | String | Sí | Nombre normalizado para evitar duplicados. |
| color | String | No | Color opcional para UI. |
| createdByAi | Boolean | Sí | Indica si fue sugerida o creada a partir de IA. |
| createdAt | Instant | Sí | Fecha de creación. |
| updatedAt | Instant | Sí | Fecha de última actualización. |

#### Reglas de negocio

- Una etiqueta pertenece a un workspace.
- Una etiqueta puede aplicarse a múltiples fragmentos.
- Un fragmento puede tener múltiples etiquetas.
- No puede haber dos etiquetas con el mismo nombre dentro del mismo workspace.
- Una etiqueta eliminada debe dejar de aparecer en los fragmentos.
- Las etiquetas sugeridas por IA deben ser revisadas o aceptadas por el usuario antes de aplicarse automáticamente en V1.
- Un usuario no puede gestionar etiquetas de workspaces que no le pertenecen.

#### Invariantes

- `workspaceId` no puede estar vacío.
- `name` debe tener contenido útil tras normalización.
- La combinación `(workspaceId, normalizedName)` debe ser única.

---

### 4.6 FragmentTag

Entidad de relación entre `Fragment` y `Tag`.

#### Responsabilidades

- Representar la asignación de una etiqueta a un fragmento.
- Permitir relaciones muchos-a-muchos.

#### Campos

| Campo | Tipo sugerido | Obligatorio | Descripción |
|---|---:|:---:|---|
| fragmentId | UUID | Sí | Fragmento etiquetado. |
| tagId | UUID | Sí | Etiqueta asignada. |
| createdAt | Instant | Sí | Fecha de asignación. |

#### Reglas de negocio

- Un fragmento no puede tener la misma etiqueta asignada más de una vez.
- El fragmento y la etiqueta deben pertenecer al mismo workspace.
- No se deben asignar etiquetas a fragmentos eliminados.
- No se deben asignar etiquetas eliminadas o inexistentes.

#### Invariantes

- La combinación `(fragmentId, tagId)` debe ser única.
- `fragment.workspaceId` debe coincidir con `tag.workspaceId`.

---

### 4.7 AiOperation

Representa una operación de inteligencia artificial solicitada por el usuario.

#### Responsabilidades

- Registrar solicitudes de IA.
- Controlar el estado de procesamiento asíncrono.
- Asociar resultados de IA a usuario, workspace y entrada procesada.
- Registrar errores de forma controlada.

#### Campos

| Campo | Tipo sugerido | Obligatorio | Descripción |
|---|---:|:---:|---|
| id | UUID | Sí | Identificador único de la operación. |
| userId | UUID | Sí | Usuario que solicita la operación. |
| workspaceId | UUID | Sí | Workspace de contexto. |
| operationType | AiOperationType | Sí | Tipo de operación IA. |
| status | AiOperationStatus | Sí | Estado del procesamiento. |
| inputScope | AiInputScope | Sí | En V1 define que la operación procesa el workspace completo. |
| inputFragmentIds | List<UUID> | No | Reservado para futuras operaciones sobre fragmentos seleccionados. En V1 debería quedar vacío. |
| inputSummary | Text | No | Resumen técnico o descriptivo de la entrada. No debe sustituir al contenido real. |
| result | Text / JSON | No | Resultado generado por IA. |
| errorCode | String | No | Código de error controlado. |
| errorMessage | String | No | Mensaje de error controlado. |
| createdAt | Instant | Sí | Fecha de solicitud. |
| startedAt | Instant | No | Fecha de inicio real del procesamiento. |
| updatedAt | Instant | Sí | Fecha de última actualización. |
| completedAt | Instant | No | Fecha de finalización correcta. |
| failedAt | Instant | No | Fecha de fallo. |
| cancelledAt | Instant | No | Fecha de cancelación. |

#### Estados

```text
PENDING
PROCESSING
COMPLETED
FAILED
CANCELLED
```

#### Tipos de operación IA para MVP

```text
WORKSPACE_SUMMARY
GLOSSARY_GENERATION
MAIN_IDEAS_DETECTION
TAG_SUGGESTION
RESEARCH_QUERY_SUGGESTION
```

#### Reglas de negocio

- Un usuario solo puede ejecutar operaciones IA sobre sus propios workspaces.
- Un usuario `DISABLED` o `DELETED` no puede ejecutar operaciones IA.
- Las operaciones IA se ejecutan de forma asíncrona desde la primera versión.
- La operación debe crearse inicialmente en estado `PENDING`.
- Una operación en ejecución debe pasar a `PROCESSING`.
- Una operación finaliza en `COMPLETED`, `FAILED` o `CANCELLED`.
- En V1, las operaciones IA solo procesan fragmentos `ACTIVE`; los fragmentos `ARCHIVED` o `DELETED` no deben enviarse a la IA.
- Un workspace debe tener fragmentos suficientes para ciertas operaciones.
- La IA no debe modificar automáticamente fragmentos, etiquetas o contenido del usuario sin confirmación.
- Si se sugieren búsquedas, deben estar justificadas en función del contenido guardado.
- Una operación IA fallida no debe afectar al workspace ni a los fragmentos originales.
- En V1 no se permite prompt libre. El usuario puede ajustar opciones controladas de la operación, como idioma de salida, tono o longitud.

#### Invariantes

- `operationType` no puede ser nulo.
- `status` no puede ser nulo.
- `workspaceId` debe pertenecer al mismo usuario que `userId`.
- Si `status = COMPLETED`, `completedAt` debería estar informado.
- Si `status = FAILED`, `failedAt`, `errorCode` o `errorMessage` deberían estar informados.
- Si `status = CANCELLED`, `cancelledAt` debería estar informado.

---

### 4.8 AiInputScope

Objeto de valor o enumerado que define el alcance de entrada de una operación IA.

#### Valores V1

```text
WORKSPACE
```

`SELECTED_FRAGMENTS` queda reservado para una versión futura.

#### Uso

- `WORKSPACE`: la operación procesa los fragmentos activos del workspace.

#### Reglas de negocio

- En `WORKSPACE`, el sistema debe recuperar solo fragmentos activos del workspace.
- Ningún fragmento archivado o eliminado puede formar parte de la entrada.

---

---

## 5. Modelos de planes y límites de uso

### 5.1 Plan

Representa un plan comercial disponible en la aplicación.

#### Responsabilidades

- Definir el tipo de acceso comercial.
- Servir como base para cuotas y funcionalidades limitables.

#### Campos

| Campo | Tipo sugerido | Obligatorio | Descripción |
|---|---:|:---:|---|
| id | UUID | Sí | Identificador único del plan. |
| name | String | Sí | Nombre visible del plan. |
| type | PlanType | Sí | Tipo de plan. |
| price | Money | No | Precio del plan. En FREE puede ser 0. |
| billingPeriod | BillingPeriod | No | Periodicidad de facturación. |
| isActive | Boolean | Sí | Indica si el plan está disponible. |
| createdAt | Instant | Sí | Fecha de creación. |
| updatedAt | Instant | Sí | Fecha de última actualización. |

#### Tipos iniciales

```text
FREE
PAID
```

#### Reglas de negocio

- Debe existir al menos un plan gratuito o plan por defecto para nuevos usuarios.
- Los planes inactivos no deben asignarse a nuevos usuarios.
- Los límites concretos por plan quedan pendientes de definición según costes reales de IA.

---

### 5.2 UserPlan

Representa la relación entre un usuario y su plan actual.

#### Campos

| Campo | Tipo sugerido | Obligatorio | Descripción |
|---|---:|:---:|---|
| id | UUID | Sí | Identificador único. |
| userId | UUID | Sí | Usuario asociado. |
| planId | UUID | Sí | Plan asignado. |
| status | UserPlanStatus | Sí | Estado de la suscripción o asignación. |
| startedAt | Instant | Sí | Inicio del plan. |
| expiresAt | Instant | No | Fin del plan, si aplica. |
| createdAt | Instant | Sí | Fecha de creación. |
| updatedAt | Instant | Sí | Fecha de última actualización. |

#### Estados sugeridos

```text
ACTIVE
EXPIRED
CANCELLED
```

#### Reglas de negocio

- Un usuario debe tener un plan activo para calcular límites.
- Agotar cuotas no cambia `User.status`.
- Un usuario puede seguir accediendo a sus contenidos aunque haya agotado la cuota de IA.

---

### 5.3 UsageQuota

Representa la cuota disponible y consumida por un usuario para una funcionalidad concreta.

#### Campos

| Campo | Tipo sugerido | Obligatorio | Descripción |
|---|---:|:---:|---|
| id | UUID | Sí | Identificador único. |
| userId | UUID | Sí | Usuario asociado. |
| feature | LimitableFeature | Sí | Funcionalidad limitada. |
| period | UsagePeriod | Sí | Periodo de cuota. |
| limit | Integer | Sí | Límite máximo permitido. |
| used | Integer | Sí | Uso consumido en el periodo. |
| resetAt | Instant | Sí | Fecha de reinicio de la cuota. |
| createdAt | Instant | Sí | Fecha de creación. |
| updatedAt | Instant | Sí | Fecha de última actualización. |

#### Funcionalidades limitables

```text
AI_SUMMARY
AI_GLOSSARY
AI_TAG_SUGGESTION
AI_MAIN_IDEAS
AI_RESEARCH_QUERY_SUGGESTION
WORKSPACE_CREATION
FRAGMENT_STORAGE
```

`PDF_EXPORT` queda reservado para una versión futura y no forma parte de los límites activos de V1.

#### Periodos

```text
DAILY
MONTHLY
YEARLY
LIFETIME
```

#### Reglas de negocio

- El sistema debe impedir nuevas operaciones cuando la cuota esté agotada.
- El sistema debe mostrar un mensaje claro cuando se alcance un límite.
- El consumo de IA debe registrarse para evitar costes inesperados.
- El número exacto de operaciones gratuitas queda pendiente hasta analizar costes reales.

#### Invariantes

- `used` no puede ser negativo.
- `limit` no puede ser negativo.
- `used` no debería superar `limit`, salvo que se permita tolerancia técnica controlada.

---

### 5.4 AiUsage

Modelo alternativo o complementario a `UsageQuota` para registrar eventos de consumo IA.

#### Responsabilidades

- Auditar cada consumo concreto.
- Permitir cálculo de costes por operación.
- Facilitar análisis de uso por tipo de operación.

#### Campos

| Campo | Tipo sugerido | Obligatorio | Descripción |
|---|---:|:---:|---|
| id | UUID | Sí | Identificador único. |
| userId | UUID | Sí | Usuario asociado. |
| aiOperationId | UUID | No | Operación IA que generó el consumo. |
| operationType | AiOperationType | Sí | Tipo de operación. |
| inputUnits | Integer | No | Unidades de entrada, por ejemplo tokens estimados. |
| outputUnits | Integer | No | Unidades de salida, por ejemplo tokens estimados. |
| provider | String | No | Proveedor IA utilizado. |
| model | String | No | Modelo utilizado. |
| createdAt | Instant | Sí | Fecha del consumo. |

#### Nota de diseño

Para V1, `UsageQuota` permite aplicar límites. `AiUsage` permite auditar coste real por operación y ajustar cuotas de uso.

---

## 6. Modelos de soporte para extensión

### 6.1 CaptureRequest

Modelo de entrada enviado por la extensión al backend cuando el usuario guarda un fragmento.

#### Campos

| Campo | Tipo sugerido | Obligatorio | Descripción |
|---|---:|:---:|---|
| workspaceId | UUID | Sí | Workspace destino. |
| selectedText | Text | Sí | Texto seleccionado. |
| sourceUrl | URL | Sí | URL actual de la página. |
| sourceDomain | String | Sí | Dominio actual. |
| pageTitle | String | No | Título de la página. |
| faviconUrl | URL | No | Favicon de la página. |
| extractedAt | Instant | Sí | Momento de extracción. |
| userNote | Text | No | Nota inicial opcional. |
| language | String | No | Idioma detectado automáticamente. |

#### Reglas de negocio

- El usuario debe estar autenticado.
- El workspace destino debe existir, estar activo y pertenecer al usuario.
- `selectedText` no puede estar vacío.
- `sourceUrl` debe ser válida.
- `sourceDomain` debe derivarse o validarse contra `sourceUrl`.
- Si `pageTitle` o `faviconUrl` no están disponibles, el fragmento debe poder guardarse igualmente.

---

### 6.2 CaptureResult

Modelo de salida tras guardar un fragmento desde la extensión.

#### Campos

| Campo | Tipo sugerido | Descripción |
|---|---:|---|
| fragmentId | UUID | Fragmento creado. |
| workspaceId | UUID | Workspace destino. |
| status | String | Resultado de la operación. |
| message | String | Mensaje comprensible para la extensión. |
| createdAt | Instant | Fecha de guardado. |

---

## 7. Modelos de búsqueda y filtrado

### 7.1 FragmentSearchCriteria

Objeto de valor para buscar y filtrar fragmentos dentro de un workspace.

#### Campos

| Campo | Tipo sugerido | Descripción |
|---|---:|---|
| userId | UUID | Usuario autenticado. |
| workspaceId | UUID | Workspace donde se busca. |
| textQuery | String | Texto libre para búsqueda textual. |
| tagIds | List<UUID> | Filtro por etiquetas. |
| sourceDomain | String | Filtro por dominio. |
| language | String | Filtro por idioma. |
| createdFrom | Instant | Fecha mínima de creación. |
| createdTo | Instant | Fecha máxima de creación. |
| sortBy | FragmentSortField | Campo de ordenación. |
| sortDirection | SortDirection | Dirección de ordenación. |
| page | Integer | Página. |
| size | Integer | Tamaño de página. |

#### Reglas de negocio

- La búsqueda debe limitarse al usuario autenticado.
- La búsqueda dentro de un workspace solo devuelve fragmentos de ese workspace.
- Los fragmentos eliminados no aparecen en resultados.
- La búsqueda inicial será textual.

---

## 8. Enumeraciones

### 8.1 UserStatus

```text
ACTIVE
DISABLED
DELETED
```

### 8.2 WorkspaceStatus

```text
ACTIVE
ARCHIVED
DELETED
```

### 8.3 FragmentStatus

```text
ACTIVE
ARCHIVED
DELETED
```

### 8.4 AiOperationStatus

```text
PENDING
PROCESSING
COMPLETED
FAILED
CANCELLED
```

### 8.5 AiOperationType

```text
WORKSPACE_SUMMARY
GLOSSARY_GENERATION
MAIN_IDEAS_DETECTION
TAG_SUGGESTION
RESEARCH_QUERY_SUGGESTION
```

### 8.6 Mapeo entre operación IA y feature limitable

```text
WORKSPACE_SUMMARY -> AI_SUMMARY
GLOSSARY_GENERATION -> AI_GLOSSARY
MAIN_IDEAS_DETECTION -> AI_MAIN_IDEAS
TAG_SUGGESTION -> AI_TAG_SUGGESTION
RESEARCH_QUERY_SUGGESTION -> AI_RESEARCH_QUERY_SUGGESTION
```

### 8.7 AiInputScope

```text
WORKSPACE
```

`SELECTED_FRAGMENTS` queda reservado para una versión futura.

### 8.8 PlanType

Inicial:

```text
FREE
PAID
```

Futuro:

```text
PRO
TEAM
ENTERPRISE
```

### 8.9 UserPlanStatus

```text
ACTIVE
EXPIRED
CANCELLED
```

### 8.10 BillingPeriod

```text
NONE
MONTHLY
YEARLY
```

### 8.11 UsagePeriod

```text
DAILY
MONTHLY
YEARLY
LIFETIME
```

### 8.12 LimitableFeature

```text
AI_SUMMARY
AI_GLOSSARY
AI_TAG_SUGGESTION
AI_MAIN_IDEAS
AI_RESEARCH_QUERY_SUGGESTION
WORKSPACE_CREATION
FRAGMENT_STORAGE
```

### 8.13 SortDirection

```text
ASC
DESC
```

---

## 9. Reglas transversales del dominio

### 9.1 Propiedad y permisos

- Todo recurso privado debe estar asociado directa o indirectamente a un usuario.
- El backend debe validar permisos en cada operación.
- No se debe confiar únicamente en validaciones del frontend o de la extensión.
- Un usuario no puede consultar, modificar o eliminar recursos de otro usuario.

### 9.2 Eliminación lógica

Entidades con eliminación lógica en V1:

```text
User
Workspace
Fragment
```

Reglas:

- Los elementos eliminados no aparecen en listados normales.
- Los workspaces eliminados no pueden recibir fragmentos.
- Los fragmentos eliminados no pueden usarse en operaciones IA.
- La eliminación física se ejecutará según el periodo de retención definido.

### 9.3 Auditoría básica

Entidades principales con auditoría:

```text
createdAt
updatedAt
deletedAt opcional
```

Entidades principales:

```text
User
Workspace
Fragment
Tag
AiOperation
Plan
UserPlan
UsageQuota
```

### 9.4 IA

- La IA debe trabajar principalmente sobre contenido guardado por el usuario.
- La IA debe ejecutarse dentro del contexto de un workspace.
- La IA no debe actuar como chat genérico sin contexto.
- Las respuestas IA deben presentarse como asistencia, no como verdad absoluta.
- El usuario debe poder revisar resultados importantes antes de guardarlos como contenido permanente.
- La IA no debe inventar enlaces reales ni presentar fuentes externas no verificadas como si hubieran sido consultadas.

### 9.5 Límites de uso

- Las funcionalidades IA pueden tener límites por usuario, periodo y plan.
- Agotar una cuota no deshabilita la cuenta.
- El usuario puede seguir accediendo a sus contenidos aunque no pueda ejecutar nuevas operaciones IA.
- Los límites exactos quedan pendientes de análisis de costes.

---

## 10. Decisiones de diseño para V1

### 10.1 SourceMetadata como objeto embebido

Para V1 se recomienda que la fuente sea un objeto embebido dentro de `Fragment`, no una entidad independiente.

Motivo:

- Cada fragmento debe conservar su propia fuente.
- No es necesario gestionar fuentes de forma independiente todavía.
- Reduce complejidad inicial.


---

### 10.2 Tag como entidad propia

Las etiquetas sí deberían ser entidades propias.

Motivo:

- Se crean y gestionan dentro de un workspace.
- Se reutilizan en múltiples fragmentos.
- Necesitan unicidad por workspace.
- Pueden ser manuales o sugeridas por IA.

---

### 10.3 AiOperation como entidad persistente desde el inicio

Las operaciones IA deberían persistirse desde V1.

Motivo:

- El procesamiento será asíncrono.
- El usuario necesita consultar el estado.
- El sistema necesita registrar fallos.
- El control de límites y consumo depende de operaciones registradas.

---

### 10.4 Planes y cuotas preparados, aunque simples

Aunque los planes de pago puedan esperar, conviene diseñar el dominio con `Plan`, `UserPlan` y `UsageQuota` desde el inicio si se va a ofrecer un plan gratuito limitado.

Motivo:

- Evita mezclar estado de cuenta con límites comerciales.
- Permite bloquear operaciones IA sin bloquear acceso a contenido.
- Facilita evolucionar a planes de pago.

---

### 10.5 No modelar roles avanzados en V1

No se recomienda incluir roles avanzados en el dominio inicial.

Motivo:

- El producto no contempla colaboración multiusuario en V1.
- Cada workspace pertenece a un único usuario.
- No hay todavía administración compleja, equipos ni permisos compartidos.

---

## 11. Modelo relacional inicial sugerido

```text
users
- id PK
- email UNIQUE
- password_hash
- display_name
- default_workspace_id FK workspaces.id NULL
- status
- email_verified
- email_verified_at
- created_at
- updated_at
- deleted_at

workspaces
- id PK
- user_id FK users.id
- name
- description
- status
- created_at
- updated_at
- deleted_at

fragments
- id PK
- user_id FK users.id
- workspace_id FK workspaces.id
- selected_text
- source_url
- source_domain
- page_title
- favicon_url
- user_note
- language
- status
- extracted_at
- created_at
- updated_at
- deleted_at

tags
- id PK
- workspace_id FK workspaces.id
- name
- normalized_name
- color
- created_by_ai
- created_at
- updated_at

fragment_tags
- fragment_id FK fragments.id
- tag_id FK tags.id
- created_at
- PK(fragment_id, tag_id)

ai_operations
- id PK
- user_id FK users.id
- workspace_id FK workspaces.id
- operation_type
- status
- input_scope
- input_summary
- result
- error_code
- error_message
- created_at
- started_at
- updated_at
- completed_at
- failed_at
- cancelled_at

ai_operation_fragments
- ai_operation_id FK ai_operations.id
- fragment_id FK fragments.id
- created_at
- PK(ai_operation_id, fragment_id)

plans
- id PK
- name
- type
- price
- billing_period
- is_active
- created_at
- updated_at

user_plans
- id PK
- user_id FK users.id
- plan_id FK plans.id
- status
- started_at
- expires_at
- created_at
- updated_at

usage_quotas
- id PK
- user_id FK users.id
- feature
- period
- limit
- used
- reset_at
- created_at
- updated_at

ai_usage
- id PK
- user_id FK users.id
- ai_operation_id FK ai_operations.id NULL
- operation_type
- input_units
- output_units
- provider
- model
- created_at
```

---

## 12. Restricciones únicas e índices

### 12.1 Restricciones únicas

```text
users.email UNIQUE

tags.workspace_id + tags.normalized_name UNIQUE

fragment_tags.fragment_id + fragment_tags.tag_id UNIQUE

ai_operation_fragments.ai_operation_id + ai_operation_fragments.fragment_id UNIQUE
```

### 12.2 Índices

```text
workspaces.user_id
workspaces.user_id + workspaces.status

fragments.user_id
fragments.workspace_id
fragments.workspace_id + fragments.status
fragments.source_domain
fragments.created_at
fragments.extracted_at

tags.workspace_id

ai_operations.user_id
ai_operations.workspace_id
ai_operations.status
ai_operations.operation_type

usage_quotas.user_id
usage_quotas.user_id + usage_quotas.feature + usage_quotas.period
```

---

## 13. Eventos de dominio

Estos eventos no son obligatorios para el MVP, pero ayudan si se diseña una arquitectura orientada a eventos o procesamiento asíncrono.

```text
UserRegistered
UserDeleted
UserReactivated
WorkspaceCreated
WorkspaceDeleted
FragmentCaptured
FragmentMoved
FragmentDeleted
TagCreated
TagAssignedToFragment
AiOperationRequested
AiOperationStarted
AiOperationCompleted
AiOperationFailed
UsageQuotaConsumed
UsageQuotaExceeded
```

Uso:

- `FragmentCaptured`: puede disparar detección de idioma.
- `AiOperationRequested`: debe disparar el procesamiento asíncrono.
- `AiOperationCompleted`: puede actualizar vistas o notificar al usuario.
- `UsageQuotaConsumed`: permitiría auditar consumo y costes.

---

## 14. Modelos que NO se recomiendan para V1

### 14.1 Folder / Subfolder

No se recomienda modelar carpetas o subcarpetas en V1.

Motivo:

- El producto decide evitar jerarquías profundas inicialmente.
- La organización se hará con etiquetas, filtros y búsqueda.

---

### 14.2 IndependentNote

No se recomienda crear notas independientes en V1.

Motivo:

- El MVP permite notas personales asociadas a fragmentos.
- Fragmind no pretende ser una app genérica de notas tipo Notion.

---

### 14.3 SharedWorkspace / CollaborationMember

No se recomienda modelar colaboración en V1.

Motivo:

- La colaboración multiusuario está fuera del MVP.
- Cada workspace pertenece a un único usuario.

---

### 14.4 Role / Permission avanzado

No se recomienda modelar roles avanzados en V1.

Motivo:

- No hay colaboración, equipos ni administración avanzada en el alcance inicial.
- La autorización principal se basa en propiedad del recurso.

---

## 15. Resumen de modelos necesarios para V1

### Imprescindibles

```text
User
Workspace
Fragment
SourceMetadata
Tag
FragmentTag
AiOperation
Plan
UserPlan
UsageQuota
```

### Complementarios

```text
AiUsage
AiOperationFragment
CaptureRequest
FragmentSearchCriteria
```

---

## 16. Conclusión

El dominio inicial de Fragmind debe centrarse en cinco núcleos:

1. Usuarios y acceso.
2. Workspaces de investigación.
3. Fragmentos capturados desde la web con su fuente original.
4. Organización mediante etiquetas y búsqueda.
5. Operaciones IA asíncronas con control de límites.

Este modelo permite construir el MVP sin introducir complejidad innecesaria y mantiene una separación clara entre usuarios, workspaces, fragmentos, etiquetas, operaciones IA, planes y cuotas.
