# Seguridad y privacidad — Fragmind

## 1. Objetivo del documento

Este documento describe cómo Fragmind protege los datos de sus usuarios y cómo cumple con las obligaciones legales aplicables. Aterriza operativamente los principios definidos en `technical/non-functional-requirements.md` (secciones 8 y 9) y sirve como base para redactar la política de privacidad pública.

Cubre:

- Tratamiento de datos personales y obligaciones GDPR.
- Política operativa de envío de datos al proveedor IA.
- Autenticación, gestión de sesiones y contraseñas.
- Borrado, retención y portabilidad.
- Gestión de incidentes de seguridad.

No cubre infraestructura concreta (eso vivirá en `operations/deployment.md`) ni decisiones técnicas con su contexto (esos serán ADRs en `decisions/`).

---

## 2. Marco legal aplicable

Fragmind se desarrolla y opera desde España. Los marcos legales aplicables en V1 son:

- **GDPR** (Reglamento UE 2016/679) por tratar datos personales de residentes en el Espacio Económico Europeo.
- **LOPDGDD** (Ley Orgánica 3/2018) como desarrollo nacional del GDPR en España.
- **LSSI-CE** (Ley 34/2002) en lo relativo a comunicaciones electrónicas y cookies.

Quedan fuera del alcance de V1: HIPAA, normativa sectorial financiera, certificaciones formales (ISO 27001, SOC 2).

---

## 3. Roles y responsabilidades en el tratamiento

Bajo GDPR:

- **Responsable del tratamiento (Data Controller)**: la entidad propietaria de Fragmind. Decide qué datos se tratan y con qué finalidad.
- **Encargados del tratamiento (Data Processors)**:
    - **OpenAI**: procesa el contenido de fragmentos y notas cuando el usuario lanza una operación IA.
    - **AWS**: aloja la infraestructura y la base de datos (región Frankfurt, `eu-central-1`).
    - **Vercel**: aloja el frontend.
    - **Resend**: envía correos del sistema (recuperación de contraseña, notificaciones).

Con cada encargado debe firmarse un **DPA (Data Processing Agreement)** antes de iniciar operaciones en producción.

---

## 4. Categorías de datos tratados

Fragmind trata las siguientes categorías de datos personales:

| Categoría | Datos | Finalidad | Base legal |
|---|---|---|---|
| Identificación | Email, contraseña (hash) | Crear y operar la cuenta | Ejecución del contrato |
| Perfil | Nombre opcional, idioma preferido | Personalizar la experiencia | Ejecución del contrato |
| Contenido | Fragmentos, notas, workspaces, etiquetas | Funcionalidad principal del producto | Ejecución del contrato |
| Técnicos | IP, user-agent, logs de acceso | Seguridad, prevención de abuso | Interés legítimo |
| Uso de IA | Texto enviado al proveedor IA | Generar resúmenes, glosarios, etc. | Consentimiento explícito |

**Fragmind no trata datos personales de categorías especiales** (artículo 9 GDPR: salud, ideología, orientación sexual, etc.). Si el usuario decide capturar fragmentos que contengan datos de esas categorías, lo hace bajo su propia responsabilidad.

---

## 5. Política de envío de datos al proveedor IA

Esta sección define **qué se envía exactamente a OpenAI en cada tipo de operación IA**. Es la referencia canónica para la política de privacidad y para implementar los servicios IA en el backend.

### 5.1 Principios

- **Nunca se envía a OpenAI información de identificación del usuario.** No se transmite email, nombre, ID de usuario ni cualquier otro dato que permita identificar quién hizo la petición.
- **Solo se envía el contenido necesario para la operación.** El backend construye el prompt de forma específica para cada tipo de operación, incluyendo únicamente los campos del modelo que aportan valor a esa operación.
- **El usuario es informado** en la política de privacidad y en la propia interfaz al lanzar operaciones IA por primera vez.

### 5.2 Detalle por operación

| Operación IA | Datos enviados a OpenAI |
|---|---|
| **Resumen de workspace** | Texto de los fragmentos del workspace + notas asociadas + nombre del workspace. No se envían: URLs, dominios, etiquetas. |
| **Glosario** | Texto de los fragmentos + notas asociadas. No se envían: URLs, dominios, nombre del workspace. |
| **Ideas principales** | Texto de los fragmentos + notas asociadas. No se envían: URLs, dominios, etiquetas. |
| **Sugerencia de etiquetas** | Texto del fragmento + notas asociadas a ese fragmento. No se envían: URL, dominio, otros fragmentos. |
| **Sugerencia de búsqueda / investigación** | Texto de los fragmentos + nombre y descripción del workspace. No se envían: URLs, dominios, etiquetas, notas. |

Nota: las **URLs y dominios nunca se envían a OpenAI**. Permanecen exclusivamente en la base de datos de Fragmind. Esto reduce la superficie de datos compartidos y evita que el proveedor IA infiera patrones de navegación del usuario.

### 5.3 Configuración del proveedor

- Uso de la **API empresarial de OpenAI** (no ChatGPT consumer).
- **DPA firmado** con OpenAI antes de producción.
- **Opt-out de entrenamiento** explícitamente activado (por defecto la API empresarial no usa los datos para entrenar).
- **Retención de datos en OpenAI configurada al mínimo permitido** por el plan contratado (idealmente "zero data retention" si está disponible).
- **Transferencia internacional** amparada en las SCCs (Standard Contractual Clauses) y el EU-US Data Privacy Framework.

### 5.4 Trazabilidad interna

Por cada operación IA, Fragmind registra internamente (sin enviar a OpenAI):

- ID de la operación, usuario, workspace, tipo de operación.
- Tamaño aproximado del payload enviado (en tokens).
- Tiempo total y resultado (`COMPLETED` o `FAILED`).
- Coste estimado de la llamada.

Este registro vive en el sistema durante el tiempo necesario para fines de auditoría, control de cuotas y depuración.

---

## 6. Autenticación y gestión de sesiones

### 6.1 Método de autenticación en V1

- **Único método**: email + contraseña.
- **OAuth** (Google, GitHub) está contemplado como evolución futura.

### 6.2 Verificación de email

- **No se exige en V1.** El usuario puede usar la aplicación inmediatamente tras el registro.
- Implicación: las funcionalidades que dependen del email (recuperación de contraseña) asumen que el usuario tiene acceso real al buzón. Si el email es falso, el usuario pierde acceso a su cuenta. Esto se documenta en los términos.
- La verificación obligatoria queda registrada en `future-evolution-notes.md` (sección 15).

### 6.3 Política de contraseñas

- Longitud mínima: **8 caracteres**.
- Se rechazan contraseñas comunes (lista negra: "12345678", "password", etc.).
- No se exigen reglas de complejidad arbitrarias (mayúsculas, símbolos): la longitud y el rechazo de contraseñas comunes son más efectivos según NIST 800-63B.
- Almacenamiento: **argon2id** con parámetros recomendados, o bcrypt con cost ≥ 12.
- Las contraseñas **nunca se loggean, ni siquiera en debug**.

### 6.4 Tokens de sesión

- **Access token**: JWT firmado, vida útil 15-30 minutos.
- **Refresh token**: JWT firmado o token opaco con registro en BBDD, vida útil 7-30 días.
- Los refresh tokens se invalidan al hacer logout, cambio de contraseña o eliminación de cuenta.
- En caso de detección de compromiso (robo de token), se ofrece un endpoint para revocar todas las sesiones del usuario.

### 6.5 Protección contra ataques de credenciales

- **Bloqueo temporal** del email tras 5 intentos fallidos consecutivos en 15 minutos.
- **Rate limit** por IP en el endpoint de login.
- No se distingue en los mensajes de error entre "email no existe" y "contraseña incorrecta", para evitar enumeración de usuarios.

### 6.6 Recuperación de contraseña

- El usuario solicita un email de recuperación con un token de un solo uso, vida útil 1 hora.
- El token se almacena hasheado en la base de datos.
- Tras restablecer la contraseña, todas las sesiones activas del usuario se invalidan.

---

## 7. Autorización y aislamiento de datos

- Todo recurso (workspace, fragmento, etiqueta, operación IA) pertenece a un único usuario.
- El backend verifica en cada petición que el recurso pertenece al usuario autenticado, incluso si el ID del recurso es conocido.
- No existen roles, permisos avanzados ni recursos compartidos entre usuarios en V1.
- La autorización se centraliza en una capa específica (filtros / interceptores de Spring) para evitar que controladores nuevos olviden aplicar la comprobación.

---

## 8. Comunicaciones seguras

- Todo el tráfico cliente-servidor usa **HTTPS con TLS 1.2 mínimo (1.3 preferido)**.
- En producción se habilita **HSTS** con `max-age` razonable.
- Cabeceras de seguridad estándar en respuestas HTTP:
    - `X-Content-Type-Options: nosniff`
    - `X-Frame-Options: DENY`
    - `Referrer-Policy: strict-origin-when-cross-origin`
    - `Content-Security-Policy` adecuada para Next.js
- CORS configurado de forma restrictiva: solo se aceptan peticiones desde el dominio del frontend y los IDs de las extensiones de Chrome y Firefox.

---

## 9. Gestión de secretos

- Claves de API (OpenAI), credenciales de base de datos, secretos JWT y similares **nunca se commitean al repositorio**.
- En desarrollo se usan archivos `.env` ignorados por Git y plantillas `.env.example` sin valores reales.
- En producción se usa el servicio de secretos del proveedor cloud elegido (AWS Secrets Manager, GCP Secret Manager o Azure Key Vault).
- Las claves se **rotan al menos una vez al año** y de forma inmediata si hay sospecha de compromiso.

---

## 10. Sanitización de contenido

Los fragmentos capturados desde la extensión pueden contener HTML, JavaScript o caracteres especiales por venir de páginas web arbitrarias.

- **Al capturar**: la extensión envía solo texto plano. El backend valida que la entrada no contenga código HTML/script activo.
- **Al almacenar**: el contenido se guarda como texto plano. Si en el futuro se decide preservar formato básico (negrita, listas), se sanitiza con una librería establecida (DOMPurify en frontend, OWASP Java HTML Sanitizer en backend).
- **Al renderizar**: el frontend nunca usa `dangerouslySetInnerHTML` con contenido de fragmentos.

---

## 11. Derechos del usuario bajo GDPR

Fragmind ofrece los siguientes derechos desde la propia aplicación, sin necesidad de pasar por soporte:

### 11.1 Acceso

El usuario puede ver toda su información desde su perfil y sus workspaces. No hace falta un export específico para visualización.

### 11.2 Rectificación

El usuario puede editar libremente nombre, idioma, email (con flujo de cambio de email cuando se implemente), workspaces, fragmentos, notas y etiquetas.

### 11.3 Portabilidad

El usuario puede solicitar un **export completo en JSON** de sus datos desde el perfil. Contiene:

- Perfil (sin contraseña).
- Workspaces (incluidos archivados).
- Fragmentos (con todos sus metadatos y fuentes).
- Notas asociadas.
- Etiquetas.
- Histórico de operaciones IA y sus resultados.

El export se genera bajo demanda y se entrega al usuario por un enlace temporal de descarga, válido durante 24 horas.

### 11.4 Eliminación ("derecho al olvido")

- El usuario solicita la baja desde su perfil.
- La cuenta pasa a estado `DELETED` y entra en un **periodo de gracia de 30 días**.
- Durante esos 30 días el usuario puede reactivar la cuenta contactando con soporte o usando un flujo de "reactivar" si se implementa.
- Tras los 30 días, se realiza el **borrado físico** de todos los datos personales y de contenido en la base de datos principal.
- Los backups que contengan los datos del usuario se purgan según el ciclo natural de rotación de backups (detalle en `operations/deployment.md`). Esto debe documentarse al usuario.

### 11.5 Oposición y limitación

El usuario puede oponerse al tratamiento basado en interés legítimo (logs técnicos). En la práctica esto implica solicitar la baja, ya que los logs son necesarios para operar el servicio.

### 11.6 Revocación del consentimiento para IA

El usuario puede dejar de usar las funcionalidades de IA en cualquier momento. No se requiere consentimiento separado revocable; el consentimiento se otorga al usar la funcionalidad y se revoca al dejar de usarla.

---

## 12. Cookies y rastreo

En V1, Fragmind utiliza **únicamente cookies técnicamente necesarias**:

- Sesión / preferencia de idioma.
- No se usan cookies de analítica, publicidad ni terceros.

Por tanto, en V1 no se requiere banner de consentimiento de cookies bajo la interpretación común de la LSSI-CE. Si en el futuro se añade analítica (Plausible, Matomo, GA4...), se evaluará y, si procede, se implementará el banner adecuado.

---

## 13. Logs y datos en producción

### 13.1 Qué se loggea

- Peticiones HTTP: método, ruta, código de respuesta, duración, `requestId`, ID de usuario (si autenticado), IP.
- Eventos de seguridad: login, logout, cambios de contraseña, intentos fallidos, accesos denegados.
- Errores de aplicación con stack trace.
- Eventos de dominio relevantes.

### 13.2 Qué NO se loggea

- Contraseñas en ningún formato (ni hash, ni texto plano, ni token de recuperación).
- Tokens JWT completos. Si se necesita trazabilidad, se loggea solo el ID del token.
- Contenido completo de fragmentos o notas (puede contener información sensible del usuario).
- Datos enviados o recibidos del proveedor IA.

### 13.3 Retención de logs

- Logs de aplicación: 30 días en almacenamiento caliente.
- Logs de seguridad: 90 días.
- Los logs no se exportan fuera del proveedor cloud salvo necesidad operativa documentada.

---

## 14. Gestión de incidentes

### 14.1 Qué se considera incidente de seguridad

- Acceso no autorizado a datos de usuarios.
- Filtración de credenciales o secretos.
- Compromiso de un encargado del tratamiento (OpenAI, cloud, email).
- Indisponibilidad prolongada del servicio que impida ejercer derechos del usuario.

### 14.2 Protocolo de respuesta

1. **Detección y contención inmediata**: cortar acceso, rotar credenciales comprometidas, aislar componentes afectados.
2. **Evaluación de impacto**: qué datos, cuántos usuarios, durante cuánto tiempo, si hay riesgo para los afectados.
3. **Notificación a la AEPD** (Agencia Española de Protección de Datos) en un plazo máximo de **72 horas** si el incidente supone riesgo para los derechos y libertades de los afectados, según el artículo 33 GDPR.
4. **Notificación a los usuarios afectados** sin dilación indebida si hay alto riesgo para sus derechos.
5. **Análisis post mortem** documentado, con acciones para evitar recurrencia.

### 14.3 Vías de contacto

- Canal interno para que cualquier persona del equipo o usuario externo reporte un incidente o vulnerabilidad.
- Dirección de contacto pública para responsable de privacidad / DPO (cuando aplique).

---

## 15. Política operativa interna

- **Acceso al entorno de producción**: limitado a personas concretas con MFA obligatorio.
- **Acceso a la base de datos de producción**: solo lectura para depuración salvo necesidad operativa puntual, registrado.
- **No se usan datos reales de producción en entornos de desarrollo o staging.** Si hace falta poblar staging, se usan datos sintéticos o se anonimizan los reales.
- **Revisión de dependencias**: las vulnerabilidades críticas conocidas (CVE) se atienden de forma prioritaria. Se usan herramientas automáticas (Dependabot, Renovate).
- **Backups**: cifrados en reposo, en la región del proveedor cloud, con prueba de restauración periódica (ver `operations/deployment.md`).

---

## 16. Documentos relacionados y pendientes

- **Política de privacidad pública**: documento separado de cara al usuario, redactado a partir de las secciones 3-5 y 11 de este documento. Pendiente de redactar antes del lanzamiento.
- **Términos de servicio**: pendiente, debe cubrir uso aceptable, propiedad del contenido capturado, exenciones de responsabilidad.
- **Registro de actividades de tratamiento (RAT)**: obligatorio bajo GDPR si hay tratamiento sistemático. Pendiente.
- **DPAs firmados**: con OpenAI, proveedor cloud y proveedor de email. Pendiente de gestionar antes de producción.
- **Detalle de infraestructura, backups y rotación de secretos**: `operations/deployment.md` (pendiente de generar).
- **ADR sobre transferencia de datos a EEUU**: registrar la decisión pragmática tomada en `non-functional-requirements.md` (sección 9.2).
