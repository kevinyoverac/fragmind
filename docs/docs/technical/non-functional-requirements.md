# Requisitos no funcionales — Fragmind

## 1. Objetivo del documento

Este documento define los requisitos no funcionales de Fragmind V1: rendimiento esperado, límites del sistema, compatibilidad, accesibilidad, internacionalización, seguridad y privacidad a alto nivel, y obligaciones legales.

No describe cómo se implementan estos requisitos (eso vive en `architecture.md` y `operations/`), sino qué debe cumplir el sistema y por qué.

---

## 2. Contexto y escala objetivo

Fragmind V1 es un MVP destinado a validar el producto con usuarios reales. La escala esperada en los primeros 6-12 meses es de **decenas a cientos de usuarios activos**, no miles.

Esto condiciona muchas de las decisiones de este documento: los requisitos están dimensionados para esa escala, no sobre-ingenierizados para un producto maduro. Cuando la escala cambie, este documento deberá revisarse.

---

## 3. Rendimiento

### 3.1 Tiempos de respuesta objetivo

Medidos en p95 (percentil 95) bajo carga normal:

| Operación | Objetivo p95 |
|---|---|
| Login / refresh token | < 500 ms |
| Crear / editar fragmento | < 500 ms |
| Listar fragmentos de un workspace (≤ 50) | < 800 ms |
| Búsqueda dentro de un workspace | < 1 s |
| Crear operación IA (devolver ID `PENDING`) | < 500 ms |
| Consulta de estado de operación IA | < 300 ms |
| Captura de fragmento desde extensión | < 1 s |

Las operaciones IA en sí (resúmenes, glosarios, etc.) no tienen objetivo de tiempo total porque son asíncronas y dependen del proveedor externo. Sí debe haber un timeout máximo configurable por tipo de operación (orientativo: 60 s para resúmenes, 30 s para etiquetas), tras el cual la operación se marca como `FAILED` con un error claro.

### 3.2 Carga concurrente

V1 debe soportar como mínimo:

- 100 usuarios concurrentes navegando la aplicación web.
- 20 operaciones IA en proceso simultáneamente (encoladas o ejecutándose).
- 10 capturas por segundo desde extensiones.

Estos números son orientativos para dimensionar la primera infraestructura, no contractuales.

### 3.3 Disponibilidad

Objetivo: **99% mensual** (aprox. 7 horas de downtime al mes permitidas). No se ofrece SLA al usuario en V1. Las ventanas de mantenimiento se comunican con antelación cuando sea posible.

---

## 4. Límites del sistema

Los límites duros previenen abuso y descontrol de costes. Los blandos son recomendaciones de UX que el frontend puede flexibilizar.

### 4.1 Datos del usuario

| Recurso | Límite |
|---|---|
| Longitud de un fragmento (texto seleccionado) | 10.000 caracteres |
| Longitud de una nota personal asociada a fragmento | 2.000 caracteres |
| Workspaces por usuario | 50 |
| Fragmentos por workspace | 1.000 |
| Etiquetas por workspace | 100 |
| Etiquetas por fragmento | 10 |
| Longitud de nombre de workspace | 100 caracteres |
| Longitud de descripción de workspace | 500 caracteres |
| Longitud de nombre de etiqueta | 30 caracteres |

Estos límites se documentan al usuario y se aplican tanto en frontend (validación inmediata) como en backend (validación canónica).

### 4.2 Cuotas de IA

Enfoque V1: **estricto** para evitar sustos en factura y permitir validar costes reales antes de relajarlos.

Cuotas iniciales por usuario y plan gratuito (revisables tras los primeros meses de uso real):

| Operación IA | Cuota |
|---|---|
| Resúmenes de workspace | 10 / mes |
| Glosarios | 10 / mes |
| Ideas principales | 10 / mes |
| Sugerencias de etiquetas | 30 / mes |
| Sugerencias de búsqueda / investigación | 20 / mes |
| Total operaciones IA | 50 / mes |

Las cuotas se reinician el día 1 de cada mes. El sistema de planes y cuotas ya está modelado en `domain-models.md` (sección 5) y en `api-spec.md` (sección 15).

Además de cuotas por usuario, el sistema aplica:

- **Rate limit global de la API**: límite por IP/usuario para evitar abuso (orientativo: 100 peticiones/min por usuario autenticado, 20/min por IP no autenticada).
- **Circuit breaker en el proveedor IA**: si OpenAI falla repetidamente o se agota la cuota del proveedor, el sistema deja de aceptar nuevas operaciones IA y devuelve un error claro al usuario.

### 4.3 Tamaños de payload

- Tamaño máximo de petición HTTP: 100 KB para endpoints normales, 50 KB para captura desde extensión (un fragmento + metadatos).
- Tamaño máximo de respuesta paginada: 100 elementos por página.

---

## 5. Compatibilidad

### 5.1 Aplicación web

**Plataformas soportadas en V1:**

- Escritorio (resolución mínima 1280×720).

**No soportado en V1:**

- Móvil y tableta. La interfaz puede funcionar visualmente pero no se garantiza la usabilidad ni se prioriza el diseño responsive más allá de lo básico. El soporte móvil completo se valora para versiones futuras.

**Navegadores soportados** (últimas dos versiones estables al momento del lanzamiento):

- Chrome / Edge / Brave (Chromium)
- Firefox
- Safari

### 5.2 Extensión de navegador

- Chrome / Chromium ≥ versión que soporte Manifest V3.
- Firefox ≥ versión que soporte Manifest V3 (Firefox 109+).

### 5.3 Backend

- Java 21 LTS.
- PostgreSQL 16+.

---

## 6. Accesibilidad

**Objetivo**: cumplir **WCAG 2.1 nivel AA** en la aplicación web.

Esto implica como mínimo:

- Contraste de color suficiente (ratio 4.5:1 para texto normal, 3:1 para texto grande).
- Navegación completa por teclado, sin trampas de foco.
- Indicadores de foco visibles.
- Etiquetas semánticas correctas en formularios.
- Texto alternativo en imágenes informativas.
- Estructura de encabezados coherente.
- Estados y errores comunicados también de forma textual, no solo por color.
- Soporte para lectores de pantalla en las pantallas principales.
- Posibilidad de aumentar el tamaño de texto hasta 200% sin pérdida de funcionalidad.

La extensión de navegador queda fuera del alcance estricto de WCAG en V1, pero debe ser navegable por teclado.

La accesibilidad se verifica con herramientas automáticas (axe, Lighthouse) en CI y con revisión manual de las pantallas principales antes del lanzamiento.

---

## 7. Internacionalización

**Idiomas soportados en V1:**

- Español (idioma por defecto).
- Inglés.

El idioma se detecta inicialmente del navegador y puede cambiarse manualmente desde el perfil de usuario.

**Implicaciones técnicas:**

- Todos los textos de la interfaz se gestionan mediante claves de traducción, no hardcoded.
- Las fechas se formatean según el locale del usuario.
- Los mensajes de error de la API devuelven códigos estables (`error.code`) y mensajes en inglés; la traducción ocurre en el cliente. Esto está alineado con `api-spec.md` (sección 7).

**Operaciones IA:** la IA responde en el idioma del contenido del workspace o, si es ambiguo, en el idioma de la interfaz del usuario. El campo `language` del fragmento (ya previsto en el modelo) ayuda a tomar esta decisión.

---

## 8. Seguridad

Esta sección describe requisitos mínimos. El detalle operativo y el cumplimiento legal se trata en `operations/security-and-privacy.md`.

### 8.1 Autenticación y sesión

- Contraseñas almacenadas con hash bcrypt o argon2 (nunca en texto plano).
- Política mínima de contraseñas: 8+ caracteres, no permitir contraseñas comunes.
- Access token de corta vida (15-30 min). Refresh token de larga vida (7-30 días).
- Refresh tokens revocables al hacer logout o cambio de contraseña.
- Bloqueo temporal tras múltiples intentos fallidos de login.

### 8.2 Comunicaciones

- Todo el tráfico cliente-servidor mediante HTTPS (TLS 1.2 mínimo, 1.3 preferido).
- HSTS habilitado en producción.
- Cabeceras de seguridad básicas: `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, CSP en el frontend.

### 8.3 Autorización

- Todo recurso (workspace, fragmento, etiqueta, operación IA) es propiedad de un único usuario.
- El backend verifica en cada petición que el recurso pertenece al usuario autenticado.
- No existen roles ni permisos avanzados en V1.

### 8.4 Protección frente a abuso

- Rate limiting por usuario y por IP (ver sección 4.2).
- Validación estricta de entrada en todos los endpoints.
- Protección contra inyecciones SQL (queries parametrizadas vía JPA).
- Protección CSRF cuando aplique (sobre todo si se introduce cookie HTTP-only en el futuro).
- Sanitización del contenido de fragmentos antes de renderizarlo (puede contener HTML al haberse copiado de una web).

### 8.5 Gestión de secretos

- Las claves de API (OpenAI, etc.) y credenciales de base de datos nunca se commitean al repositorio.
- Se gestionan vía variables de entorno o un servicio de secretos del proveedor cloud elegido.

---

## 9. Privacidad y obligaciones legales

### 9.1 GDPR

Fragmind opera desde la UE y trata datos personales de usuarios, por lo que está sujeto a GDPR. Requisitos mínimos:

- **Base legal**: ejecución del contrato (cuenta de usuario) y consentimiento explícito para envío de datos al proveedor IA.
- **Información al usuario**: política de privacidad clara antes del registro.
- **Derechos del usuario**: acceso, rectificación, exportación de datos y eliminación.
- **Minimización**: no se recoge más información personal de la necesaria.
- **Cookies**: solo las técnicamente necesarias en V1. Si se añaden analíticas, se requiere banner de consentimiento.

### 9.2 Transferencia de datos a EEUU (OpenAI)

**Decisión para V1: pragmática.** Se acepta el uso de OpenAI bajo las siguientes condiciones:

- DPA (Data Processing Agreement) firmado con OpenAI.
- Uso de la API empresarial con `data retention` configurado al mínimo y el opt-out de entrenamiento activado (por defecto en la API).
- Amparo legal en las SCCs (Standard Contractual Clauses) y el EU-US Data Privacy Framework.
- Información explícita al usuario en la política de privacidad sobre qué se envía a OpenAI y por qué.
- No se envían a OpenAI datos personales identificables del usuario (nombre, email): solo el contenido de los fragmentos y notas que el usuario haya guardado conscientemente.

Esta decisión se registrará como ADR.

**Alternativas contempladas para el futuro:** Azure OpenAI región UE, o modelos europeos (Mistral). Se reevaluará si el producto evoluciona hacia clientes empresariales o sectores regulados.

### 9.3 Retención de datos

- **Cuenta activa**: los datos se conservan mientras la cuenta esté activa.
- **Eliminación de cuenta**: al solicitar la baja, la cuenta pasa a estado `DELETED` y se inicia un **periodo de gracia de 30 días** durante el cual el usuario puede restaurarla. Tras los 30 días se realiza el borrado físico de todos los datos personales y de contenido.
- **Workspaces y fragmentos eliminados**: borrado lógico inmediato, borrado físico tras 30 días.
- **Logs y backups**: los datos personales en backups se purgan según el ciclo de retención de backups (ver `operations/deployment.md` cuando se genere).

### 9.4 Contenido de terceros

Los fragmentos guardados son extractos de páginas web de terceros. Implicaciones:

- El usuario es responsable de respetar los términos de las páginas de las que extrae contenido.
- Fragmind almacena el fragmento como uso privado del usuario; no lo publica ni lo comparte con otros usuarios en V1.
- La política de uso del producto deja claro que Fragmind no se hace responsable de un uso indebido del contenido capturado.

---

## 10. Observabilidad

Mínimos para V1 (el detalle de herramientas se decidirá en `operations/deployment.md`):

- **Logs estructurados** (JSON) en backend con nivel configurable, sin incluir datos personales ni contraseñas.
- **Métricas básicas**: tiempos de respuesta, tasa de errores 5xx, profundidad de la cola IA, consumo de la API de OpenAI, número de usuarios activos.
- **Alertas**: errores 5xx por encima de umbral, cola IA bloqueada, fallos del proveedor IA, espacio en disco bajo.
- **Trazabilidad**: cada petición tiene un `requestId` que se propaga a logs y a los workers IA, útil para depurar fallos extremo a extremo.

---

## 11. Mantenibilidad

- Cobertura de tests mínima razonable para V1: tests de integración en los flujos principales (registro, login, captura, operación IA completa), tests unitarios en las reglas de negocio críticas. No se persigue cobertura porcentual concreta como objetivo.
- Linting y formateo automatizados en CI.
- Migraciones de base de datos versionadas con Flyway (ver ADR-011).
- Las dependencias se actualizan periódicamente; las vulnerabilidades críticas se atienden de forma prioritaria.

---

## 12. Lo que queda fuera de V1

Se considera explícitamente fuera de los requisitos no funcionales de V1, para evitar sobre-ingeniería:

- Alta disponibilidad multirregión.
- Auto-scaling agresivo.
- SLA contractual al usuario.
- Cifrado en reposo a nivel de aplicación (más allá del que ofrezca el proveedor de base de datos gestionada).
- Auditoría completa de accesos (logs de auditoría por acción).
- Certificaciones formales (ISO 27001, SOC 2).
- Soporte móvil completo (responsive nativo o app).
- Soporte multi-idioma más allá de español e inglés.

Estos puntos se evaluarán si la escala, el modelo de negocio o el tipo de cliente lo justifican.

---

## 13. Relación con otros documentos

- **Arquitectura**: `technical/architecture.md` describe cómo se implementan estos requisitos.
- **Modelo de datos**: los límites de la sección 4 se reflejan en validaciones del modelo en `domain-models.md`.
- **API**: los rate limits y códigos de error están en `api-spec.md` (sección 7).
- **Operaciones**: el detalle de seguridad operativa, despliegue y observabilidad vivirá en `operations/`.
- **Evolución futura**: los requisitos descartados para V1 se reflejan en `future-evolution-notes.md` cuando proceda.
