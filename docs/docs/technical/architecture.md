# Arquitectura técnica — Fragmind

## 1. Objetivo del documento

Este documento describe la arquitectura técnica de Fragmind: los componentes que forman el sistema, cómo se relacionan, qué tecnologías se usan y por qué. Sirve como punto de entrada para cualquier persona que necesite entender el sistema antes de profundizar en `domain-models.md` o `api-spec.md`.

No es un manual de despliegue (ver `operations/deployment.md` cuando se genere) ni una guía de instalación local (ver `README.md` del repositorio cuando se cree).

---

## 2. Visión general

Fragmind se compone de tres aplicaciones cliente y un backend único que las atiende. El backend persiste en una base de datos relacional, encola trabajos de IA para procesarlos en segundo plano y se comunica con un proveedor externo de modelos de lenguaje para resolverlos.

```text
┌──────────────────┐   ┌──────────────────┐
│  Extensión       │   │  Aplicación web  │
│  (Chrome/Firefox)│   │  (Next.js)       │
└────────┬─────────┘   └─────────┬────────┘
         │                       │
         │      HTTPS / JSON     │
         └───────────┬───────────┘
                     │
              ┌──────▼───────┐
              │   Backend    │
              │ (Spring Boot)│
              └──┬────────┬──┘
                 │        │
        ┌────────▼──┐  ┌──▼──────────────┐
        │PostgreSQL │  │ Cola de trabajos│
        └───────────┘  │ (workers IA)    │
                       └────────┬────────┘
                                │
                       ┌────────▼────────┐
                       │  OpenAI API     │
                       └─────────────────┘
```

---

## 3. Componentes del sistema

### 3.1 Aplicación web

Cliente principal del usuario. Permite gestionar workspaces, ver fragmentos, lanzar operaciones IA y consultar resultados.

- **Tecnología**: Next.js (React) con TypeScript.
- **Responsabilidades**:
    - Renderizar la interfaz de usuario.
    - Consumir la API del backend.
    - Gestionar el ciclo de vida del access token y la renovación con refresh token.
    - Mostrar estado en tiempo real de operaciones IA mediante WebSocket o polling.
- **No responsabilidades**:
    - No accede directamente al proveedor de IA.
    - No tiene lógica de negocio crítica; las reglas viven en el backend.

### 3.2 Extensión de navegador

Cliente ligero embebido en el navegador del usuario. Su propósito es capturar texto seleccionado y enviarlo al backend.

- **Tecnología**: Manifest V3, JavaScript/TypeScript. Compatible con Chrome (Chromium) y Firefox.
- **Responsabilidades**:
    - Capturar texto seleccionado y metadatos de la página (URL, dominio, título, favicon).
    - Permitir elegir el workspace destino.
    - Enviar el fragmento al backend mediante un endpoint específico de la extensión.
    - Gestionar autenticación con el backend (el usuario inicia sesión desde la extensión o reutiliza la sesión web según se decida).
- **No responsabilidades**:
    - No procesa el contenido capturado.
    - No interactúa con la IA directamente.

### 3.3 Backend

Núcleo del sistema. Atiende a ambos clientes, aplica las reglas de negocio, persiste los datos y orquesta las operaciones de IA.

- **Tecnología**: Java 21 + Spring Boot 3.x.
- **Módulos principales** (alineados con `functional-specification.md`):
    - Autenticación y gestión de usuarios.
    - Workspaces.
    - Fragmentos.
    - Fuentes.
    - Etiquetas.
    - Búsqueda y filtros.
    - Operaciones IA.
    - Captura desde extensión.
    - Cuotas y planes.
- **Capas internas**: controladores REST → servicios de aplicación → modelo de dominio → repositorios JPA.
- **Responsabilidades transversales**: validación de entrada, autorización por usuario, gestión de errores estandarizada, rate limiting, observabilidad.

### 3.4 Base de datos

Almacenamiento canónico de todos los datos del sistema.

- **Tecnología**: PostgreSQL 16+.
- **Justificación**: el dominio es relacional (usuarios, workspaces, fragmentos, etiquetas con relaciones claras), necesita transacciones ACID y soporta búsqueda full-text nativa, útil para la búsqueda dentro de workspaces.
- **Acceso**: exclusivamente desde el backend mediante JPA/Hibernate. Migraciones gestionadas con **Flyway** (ver ADR-011).

### 3.5 Cola de trabajos y workers IA

Las operaciones IA son asíncronas. El backend encola un trabajo y un worker lo procesa en segundo plano.

- **Tecnología**: **AWS SQS** (ver ADR-009).
- **Flujo**:
    1. El cliente llama a `POST /ai-operations` (ver `api-spec.md`, sección 14.1).
    2. El backend crea la operación en estado `PENDING`, publica un mensaje en SQS y devuelve el ID al cliente.
    3. Un worker consume el mensaje, marca la operación como `RUNNING`, invoca al proveedor IA, persiste el resultado y marca la operación como `COMPLETED` o `FAILED`.
    4. El cliente consulta el estado por polling con backoff (ver ADR-010).
- **Justificación de la asincronía**: las operaciones contra el modelo de lenguaje pueden tardar segundos o decenas de segundos. Bloquear una conexión HTTP durante ese tiempo degrada la experiencia, complica el manejo de errores y dificulta los reintentos.

### 3.6 Proveedor de IA

Servicio externo que ejecuta las operaciones de comprensión, organización e investigación asistida.

- **Tecnología**: OpenAI API.
- **Acceso**: solo desde los workers IA, nunca desde los clientes ni desde los controladores síncronos del backend.
- **Aislamiento**: el código que interactúa con OpenAI se encapsula en un adaptador (`AiProvider`) para poder sustituirlo en el futuro sin afectar al resto del sistema. Esto facilita pruebas con stubs y prepara el terreno para soportar múltiples proveedores si fuera necesario.
- **Modelos por tipo de operación** (configurables por entorno):
    - Operaciones rápidas (sugerencias de etiquetas, ideas principales): **gpt-4o-mini**, optimizado para coste.
    - Operaciones de mayor calidad (resúmenes, glosarios, sugerencias de investigación): **gpt-4o** o equivalente.
    - La asignación modelo-operación es configurable para permitir ajustar el balance coste/calidad sin redesplegar.

### 3.7 Notificaciones de fin de operación IA

Para que el cliente sepa cuándo termina una operación IA, V1 usa **polling con backoff** (ver ADR-010). El cliente consulta `GET /ai-operations/{id}` con intervalos crecientes hasta que la operación esté en estado `COMPLETED` o `FAILED`.

La migración a WebSocket o Server-Sent Events queda contemplada como evolución futura si crece la necesidad de latencia mínima o aparecen otras notificaciones en tiempo real.

---

## 4. Flujos clave

### 4.1 Captura de un fragmento desde la extensión

```text
Usuario selecciona texto en una página
→ Extensión captura texto + URL + dominio + título + favicon
→ Usuario elige workspace destino (lista cargada vía GET /extension/workspaces)
→ Extensión envía POST /extension/fragments con token de acceso
→ Backend valida, persiste el fragmento y la fuente, devuelve el fragmento creado
→ Extensión muestra confirmación
```

### 4.2 Operación IA asíncrona

```text
Cliente web: POST /ai-operations { type: "WORKSPACE_SUMMARY", workspaceId }
→ Backend crea operación en estado PENDING, encola mensaje, devuelve { id, status: PENDING }
→ Worker consume el mensaje
→ Worker marca operación como RUNNING
→ Worker recupera fragmentos del workspace, construye el prompt
→ Worker llama a OpenAI
→ Worker persiste el resultado y marca operación como COMPLETED
→ Backend notifica al cliente por WebSocket (o el cliente la detecta por polling)
→ Cliente muestra el resultado
```

### 4.3 Autenticación

```text
Cliente: POST /auth/login con credenciales
→ Backend valida, emite access token (corta vida) + refresh token (larga vida)
→ Cliente almacena ambos
→ En cada petición autenticada: Authorization: Bearer <accessToken>
→ Cuando el access token expira: POST /auth/refresh con el refresh token
→ Backend emite nuevo par de tokens
```

La evolución hacia cookie HTTP-only está contemplada en `future-evolution-notes.md` (sección 17) pero no forma parte de V1.

---

## 5. Stack tecnológico — Resumen

| Capa | Tecnología | Versión objetivo |
|---|---|---|
| Frontend web | Next.js + React + TypeScript | Next.js 15+ |
| Extensión | Manifest V3 + TypeScript | — |
| Backend | Java + Spring Boot | Java 21, Spring Boot 3.x |
| Persistencia | PostgreSQL | 16+ |
| ORM | JPA / Hibernate | El que traiga Spring Boot |
| Migraciones | Flyway | — |
| Cola | AWS SQS | — |
| IA | OpenAI API | gpt-4o-mini / gpt-4o según operación |
| Notificaciones | Polling con backoff | — |
| Autenticación | JWT (access + refresh) | — |
| Despliegue | AWS (ECS Fargate, región Frankfurt) | — |
| Infraestructura como código | AWS CDK (TypeScript) | — |
| Email transaccional | Resend | — |
| DNS | Cloudflare | — |

---

## 6. Justificación del stack

**Spring Boot** porque el dominio es lo bastante rico (varios agregados, reglas de negocio, estados, transacciones) como para beneficiarse de un framework maduro con buen soporte para JPA, seguridad, validación y testing. Java 21 ofrece virtual threads, útiles si el backend necesita atender muchas conexiones concurrentes durante las consultas de estado de operaciones IA.

**Next.js** como frontend porque combina renderizado del lado servidor (útil para la página de aterrizaje y SEO si se decide tener uno) con cliente React para las pantallas de aplicación. Su modelo de rutas y su soporte para API routes permite añadir una capa fina de backend-for-frontend si fuera necesario, sin reescribir.

**PostgreSQL** porque el modelo de dominio es claramente relacional, necesita integridad referencial (eliminar un workspace debe eliminar sus fragmentos en cascada), y porque su búsqueda full-text es suficiente para V1 sin añadir Elasticsearch.

**OpenAI** como proveedor inicial por madurez de API, documentación y disponibilidad de modelos con buena relación coste/calidad. El adaptador `AiProvider` permite cambiar de proveedor sin tocar la lógica de negocio.

**Operaciones IA asíncronas** porque las latencias del modelo de lenguaje (segundos a decenas de segundos) son incompatibles con peticiones HTTP síncronas razonables. Encolar también permite reintentos, control de rate limits del proveedor, y limitar el coste mediante cuotas.

Las decisiones concretas de cada componente (cola, notificaciones, migraciones, IaC, email) están documentadas como ADRs en `decisions/`.

---

## 7. Decisiones pendientes

Estas decisiones se concretarán cuando llegue el momento operativo correspondiente. No bloquean el inicio del desarrollo:

- Estrategia de autenticación de la extensión (sesión propia vs reutilizar la web).
- Almacenamiento del access token en cliente web (`localStorage` vs memoria + cookie httpOnly) — decisión que se cierra durante la implementación de autenticación.
- Repositorio donde vive el código de infraestructura CDK (separado o dentro del backend).
- Activación de Multi-AZ en RDS — diferido a cuando la disponibilidad lo requiera (ver `operations/deployment.md`).

---

## 8. Restricciones y supuestos

- El sistema opera bajo GDPR. El detalle de tratamiento de datos personales y del contenido enviado a OpenAI se desarrollará en `operations/security-and-privacy.md`.
- El contenido enviado a OpenAI incluye fragmentos extraídos de páginas web de terceros. Esto tiene implicaciones legales y de privacidad que deben revisarse antes de producción.
- V1 no contempla colaboración entre usuarios. Toda la arquitectura asume que un usuario solo accede a sus propios datos.
- V1 no contempla multitenancy ni roles avanzados.

---

## 9. Aspectos no cubiertos en este documento

- Requisitos de rendimiento, límites y compatibilidad → `technical/non-functional-requirements.md`.
- Despliegue, entornos, CI/CD, backups, observabilidad → `operations/deployment.md`.
- Tratamiento de datos personales, GDPR, política con el proveedor IA → `operations/security-and-privacy.md`.
- Decisiones técnicas específicas con su contexto → `decisions/` (ADRs).
- Wireframes y diseño de pantallas → pendiente.
