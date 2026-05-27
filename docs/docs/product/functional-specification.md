# Especificación funcional — Fragmind

## 1. Introducción

### 1.1 Objetivo del documento

Este documento define el comportamiento funcional inicial de **Fragmind**, una aplicación de investigación asistida por IA que permite al usuario guardar fragmentos relevantes de páginas web, organizarlos en workspaces y analizarlos mediante inteligencia artificial.

La especificación servirá como guía para el desarrollo del backend, frontend web y extensión de navegador.

---

## 2. Conceptos principales

### 2.1 Usuario

Persona registrada en la aplicación que puede crear workspaces, guardar fragmentos y utilizar funcionalidades de IA.

---

### 2.2 Workspace

Espacio de investigación creado por el usuario para organizar información sobre un tema concreto.

Ejemplos:

* “Aprender Spring Security”
* “Investigación sobre IA generativa”
* “Preparación entrevista backend”
* “Trabajo final de máster”

---

### 2.3 Fragmento

Texto seleccionado por el usuario desde una página web y guardado dentro de un workspace.

Un fragmento conserva su fuente original y puede ser organizado mediante etiquetas o procesado por IA.

---

### 2.4 Fuente

Información de origen asociada a un fragmento.

Incluye como mínimo:

* URL original.
* Dominio.
* Fecha de extracción.

Opcionalmente:

* Título de la página.
* Favicon.

---

### 2.5 Etiqueta

Elemento usado para clasificar fragmentos dentro de un workspace.

Puede ser creada manualmente por el usuario o sugerida por IA.

---

### 2.6 Operación IA

Proceso mediante el cual la aplicación utiliza inteligencia artificial para generar contenido, organizar información o asistir la investigación.

Ejemplos V1:

* Resumen.
* Glosario.
* Etiquetas sugeridas.
* Ideas principales.
* Sugerencias de búsqueda o líneas de investigación.

Ejemplos futuros:

* Recomendaciones de fuentes reales.
* Detección de contradicciones.
* Comparación de puntos de vista.

---

# 3. Módulo: Autenticación

## 3.1 Objetivo

Permitir que el usuario cree una cuenta, inicie sesión, cierre sesión y acceda de forma segura a la aplicación.

---

## 3.2 Casos de uso

* Como usuario, quiero registrarme con email y contraseña.
* Como usuario, quiero iniciar sesión con email y contraseña.
* Como usuario, quiero cerrar sesión.
* Como usuario, quiero mantener mi sesión iniciada en la aplicación web.
* Como usuario, quiero que la extensión de navegador pueda utilizar mi sesión para guardar fragmentos.

---

## 3.3 Reglas de negocio

* El email debe ser único.
* El email debe tener un formato válido.
* La contraseña debe cumplir unos requisitos mínimos de seguridad.
* Un usuario recién registrado queda en estado `ACTIVE`.
* Un usuario `DISABLED` no puede iniciar sesión.
* Un usuario `DELETED` no puede iniciar sesión.
* El sistema no debe almacenar contraseñas en texto plano.
* La sesión debe identificar de forma segura al usuario autenticado.
* La extensión solo podrá guardar fragmentos si el usuario está autenticado.

---

## 3.4 Estados de usuario

```text
ACTIVE
DISABLED
DELETED
```

En la V1 se utilizarán los tres estados porque la eliminación de cuenta se gestionará inicialmente mediante eliminación lógica.

`DELETED` representa una cuenta eliminada lógicamente, pendiente de posible reactivación o eliminación física definitiva.

---

## 3.5 Errores esperados

* Email ya registrado.
* Email con formato inválido.
* Contraseña no válida.
* Credenciales incorrectas.
* Cuenta deshabilitada.
* Cuenta eliminada.
* Sesión expirada.
* Usuario no autenticado.

---

# 4. Módulo: Usuario y perfil

## 4.1 Objetivo

Permitir que el usuario gestione información básica de su cuenta y configuración personal.

---

## 4.2 Casos de uso

* Como usuario, quiero ver mi información de perfil.
* Como usuario, quiero modificar mi nombre visible.
* Como usuario, quiero cambiar mi contraseña.
* Como usuario, quiero cambiar mi workspace por defecto para capturas rápidas.
* Como usuario, quiero desactivar mi cuenta.

El cambio de email queda fuera del alcance de V1 para evitar añadir complejidad de seguridad y verificación.

---

## 4.3 Información inicial del usuario

Para la primera versión, se recomienda guardar solo información mínima:

```text
id
email
displayName
password
status
defaultWorkspaceId
createdAt
updatedAt
```

No se recomienda guardar edad, apellidos u otra información personal si no aporta valor directo al producto.

---

## 4.4 Reglas de negocio

* El usuario solo puede modificar su propia información.
* El email debe ser único.
* Para cambiar la contraseña, el usuario debe introducir su contraseña actual.
* La nueva contraseña debe cumplir los requisitos mínimos de seguridad.
* Un usuario `DISABLED` o `DELETED` no puede modificar su perfil.
* El workspace por defecto solo puede cambiarse a un workspace propio en estado `ACTIVE`.

---

## 4.5 Errores esperados

* Contraseña actual incorrecta.
* Nueva contraseña inválida.
* Usuario no autenticado.
* Usuario sin permisos.
* Cuenta deshabilitada.
* Cuenta eliminada.

---

# 5. Módulo: Workspaces

## 5.1 Objetivo

Permitir que el usuario cree y gestione espacios de investigación donde organizar fragmentos relacionados con un tema.

---

## 5.2 Casos de uso

* Como usuario, quiero crear un workspace.
* Como usuario, quiero ver todos mis workspaces.
* Como usuario, quiero acceder a un workspace concreto.
* Como usuario, quiero editar el nombre de un workspace.
* Como usuario, quiero editar la descripción de un workspace.
* Como usuario, quiero eliminar un workspace.
* Como usuario, quiero archivar un workspace que ya no uso.
* Como usuario, quiero restaurar un workspace archivado.
* Como usuario, quiero ver los fragmentos guardados dentro de un workspace.

---

## 5.3 Reglas de negocio

* Un workspace pertenece a un único usuario.
* Un usuario puede tener múltiples workspaces.
* Al registrarse, el sistema crea automáticamente un workspace inicial por defecto.
* Un usuario tiene un workspace por defecto para facilitar la captura rápida desde la extensión.
* Un workspace puede contener múltiples fragmentos.
* El nombre del workspace es obligatorio.
* El usuario no puede acceder a workspaces de otros usuarios.
* Al eliminar un workspace, no se eliminará físicamente de la base de datos en la primera versión.
* Un workspace eliminado no debe aparecer en los listados normales.
* En la V1, un workspace no tendrá subcarpetas ni estructuras jerárquicas internas.
* La organización inicial se realizará mediante etiquetas, búsqueda y filtros.
* Un workspace archivado no debe ser el destino por defecto al guardar nuevos fragmentos.
* Un workspace archivado no aparece en el selector rápido de la extensión, salvo que el usuario lo solicite explícitamente.
* Un workspace archivado puede restaurarse a `ACTIVE`.

---

## 5.4 Estados de workspace

Para la V1:

```text
ACTIVE
ARCHIVED
DELETED
```

`ACTIVE` representa un workspace visible, editable y válido para guardar fragmentos.

`ARCHIVED` representa un workspace conservado, visible en una sección de archivados, pero no disponible como destino por defecto para nuevas capturas.

`DELETED` representa un workspace eliminado lógicamente, oculto de los listados normales y no disponible para nuevas capturas.

---

## 5.5 Errores esperados

* Nombre de workspace vacío.
* Workspace no encontrado.
* Usuario sin permisos sobre el workspace.
* Workspace archivado.
* Workspace eliminado.
* Usuario no autenticado.

---

# 6. Módulo: Fragmentos

## 6.1 Objetivo

Permitir que el usuario gestione los fragmentos de texto seleccionados desde páginas web y guardados dentro de sus workspaces.

---

## 6.2 Casos de uso

* Como usuario, quiero guardar un fragmento desde la extensión de navegador.
* Como usuario, quiero ver los fragmentos guardados en un workspace.
* Como usuario, quiero leer el contenido completo de un fragmento.
* Como usuario, quiero ver la URL original de un fragmento.
* Como usuario, quiero abrir la fuente original del fragmento.
* Como usuario, quiero añadir una nota personal a un fragmento.
* Como usuario, quiero editar la nota personal de un fragmento.
* Como usuario, quiero mover un fragmento a otro workspace.
* Como usuario, quiero archivar un fragmento que no quiero ver en la vista principal.
* Como usuario, quiero eliminar un fragmento que ya no necesito.
* Como usuario, quiero restaurar un fragmento archivado.
* Como usuario, quiero ver cuándo fue guardado un fragmento.

En V1, la restauración de fragmentos `DELETED` queda fuera del flujo normal de usuario. Si se desea restaurar eliminados más adelante, deberá incorporarse una papelera o listado específico.

---

## 6.3 Información mínima de un fragmento

```text
id
userId
workspaceId
selectedText
sourceUrl
sourceDomain
extractedAt
status
createdAt
updatedAt
```

---

## 6.4 Información opcional de un fragmento

```text
pageTitle
faviconUrl
userNote
language
```

---

## 6.5 Reglas de negocio

* Todo fragmento debe pertenecer a un usuario.
* Todo fragmento debe pertenecer a un workspace.
* Todo fragmento debe conservar la URL original.
* Todo fragmento debe conservar el dominio de origen.
* Todo fragmento debe conservar la fecha de extracción.
* Un usuario solo puede acceder a sus propios fragmentos.
* Un fragmento archivado no debe aparecer en los listados principales.
* Un fragmento archivado no debe utilizarse en operaciones de IA en V1.
* Un fragmento eliminado no debe aparecer en los listados normales.
* Un fragmento eliminado no debe utilizarse en operaciones de IA.
* Un fragmento debe tener texto seleccionado no vacío.
* Un fragmento no puede guardarse en un workspace eliminado.
* Un fragmento no puede guardarse en un workspace archivado desde el flujo normal de captura.

---

## 6.6 Estados de fragmento

Para la V1:

```text
ACTIVE
ARCHIVED
DELETED
```

`ACTIVE` representa un fragmento visible en el flujo principal y válido para operaciones de IA.

`ARCHIVED` representa un fragmento oculto de la vista principal, conservado y restaurable.

`DELETED` representa un fragmento eliminado lógicamente. No aparece en listados normales y no se utiliza en operaciones de IA.

---

## 6.7 Errores esperados

* Texto seleccionado vacío.
* URL de origen inválida.
* Workspace no encontrado.
* Usuario sin permisos sobre el workspace.
* Fragmento no encontrado.
* Fragmento archivado.
* Fragmento eliminado.
* Error al guardar el fragmento.
* Usuario no autenticado.

---

# 7. Módulo: Fuentes

## 7.1 Objetivo

Conservar la información básica de origen de cada fragmento guardado para que el usuario pueda volver a consultar la fuente original.

---

## 7.2 Casos de uso

* Como usuario, quiero ver la URL original de un fragmento.
* Como usuario, quiero abrir la página original desde la aplicación.
* Como usuario, quiero saber de qué dominio proviene un fragmento.
* Como usuario, quiero ver cuándo se extrajo el fragmento.
* Como usuario, quiero ver el título de la página, si está disponible.
* Como usuario, quiero ver el favicon de la página, si está disponible.

---

## 7.3 Reglas de negocio

* Todo fragmento debe tener una URL de origen.
* El dominio debe derivarse automáticamente de la URL.
* La fecha de extracción debe registrarse automáticamente.
* El título de la página es opcional.
* El favicon es opcional.
* Si no se puede obtener el título o favicon, el fragmento debe guardarse igualmente.
* La aplicación no debe depender de scraping avanzado para obtener metadatos.

---

## 7.4 Extracción de metadatos desde la extensión

La extensión puede obtener información básica desde la página actual:

```javascript
const pageTitle = document.title;
const sourceUrl = window.location.href;
const sourceDomain = window.location.hostname;
const faviconUrl = document.querySelector('link[rel*="icon"]')?.href;
```

Si no se encuentra favicon, se puede intentar usar:

```text
https://dominio.com/favicon.ico
```

---

## 7.5 Errores esperados

* URL no disponible.
* URL inválida.
* No se pudo obtener el título de la página.
* No se pudo obtener el favicon.
* Dominio no válido.

Los errores relacionados con título o favicon no deben impedir guardar el fragmento.

---

# 8. Módulo: Etiquetas

## 8.1 Objetivo

Permitir que el usuario organice fragmentos mediante etiquetas dentro de un workspace.

---

## 8.2 Casos de uso

* Como usuario, quiero crear etiquetas dentro de un workspace.
* Como usuario, quiero asignar etiquetas a un fragmento.
* Como usuario, quiero quitar etiquetas de un fragmento.
* Como usuario, quiero editar el nombre de una etiqueta.
* Como usuario, quiero eliminar una etiqueta.
* Como usuario, quiero filtrar fragmentos por etiqueta.
* Como usuario, quiero recibir sugerencias de etiquetas generadas por IA.

---

## 8.3 Reglas de negocio

* Una etiqueta pertenece a un workspace.
* Una etiqueta puede aplicarse a múltiples fragmentos.
* Un fragmento puede tener múltiples etiquetas.
* No puede haber dos etiquetas con el mismo nombre dentro del mismo workspace.
* Una etiqueta eliminada debe dejar de aparecer en los fragmentos.
* Las etiquetas sugeridas por IA deben ser revisadas o aceptadas por el usuario antes de aplicarse automáticamente, al menos en la primera versión.
* Un usuario no puede gestionar etiquetas de workspaces que no le pertenecen.

---

## 8.4 Información mínima de una etiqueta

```text
id
workspaceId
name
createdAt
updatedAt
```

Opcionalmente:

```text
color
createdByAi
```

---

## 8.5 Errores esperados

* Nombre de etiqueta vacío.
* Etiqueta duplicada dentro del workspace.
* Etiqueta no encontrada.
* Workspace no encontrado.
* Usuario sin permisos.
* Error al asignar etiqueta.
* Error al eliminar etiqueta.

---

# 9. Módulo: Búsqueda y filtros

## 9.1 Objetivo

Permitir que el usuario encuentre rápidamente fragmentos dentro de un workspace.

---

## 9.2 Casos de uso

* Como usuario, quiero buscar fragmentos por texto.
* Como usuario, quiero filtrar fragmentos por etiqueta.
* Como usuario, quiero filtrar fragmentos por fuente o dominio.
* Como usuario, quiero ordenar fragmentos por fecha de guardado.
* Como usuario, quiero ver solo fragmentos de un workspace concreto.
* Como usuario, quiero buscar contenido relevante dentro de mi investigación.

---

## 9.3 Reglas de negocio

* La búsqueda debe limitarse a los workspaces del usuario autenticado.
* La búsqueda dentro de un workspace solo debe devolver fragmentos de ese workspace.
* Los fragmentos eliminados no deben aparecer en resultados.
* Los workspaces eliminados o archivados quedan fuera de la búsqueda general.
* La búsqueda inicial será textual.

---

## 9.4 Errores esperados

* Usuario no autenticado.
* Workspace no encontrado.
* Usuario sin permisos sobre el workspace.
* Parámetros de búsqueda inválidos.
* Error al ejecutar la búsqueda.

---

# 10. Módulo: Inteligencia Artificial

## 10.1 Objetivo

Permitir que el usuario utilice IA para comprender, organizar y ampliar la información guardada en sus workspaces.

---

## 10.2 Principios funcionales de IA

* La IA debe trabajar principalmente sobre contenido guardado por el usuario.
* La IA debe ejecutarse dentro del contexto de un workspace.
* La IA no debe actuar como un chat genérico sin contexto.
* El usuario debe poder revisar los resultados importantes antes de guardarlos como contenido permanente.
* Las respuestas generadas por IA pueden contener errores y deben presentarse como asistencia, no como verdad absoluta.

---

## 10.3 Categoría: Comprensión

### Casos de uso V1

* Como usuario, quiero generar un resumen de los fragmentos de un workspace.
* Como usuario, quiero obtener un glosario de términos importantes.
* Como usuario, quiero detectar las ideas principales de mi investigación.

### Casos de uso futuros

* Como usuario, quiero generar un resumen de fragmentos seleccionados.
* Como usuario, quiero obtener una explicación sencilla de conceptos complejos.
* Como usuario, quiero generar una FAQ automática a partir de los fragmentos.

---

## 10.4 Categoría: Organización

### Casos de uso V1

* Como usuario, quiero obtener etiquetas sugeridas por IA.

### Casos de uso futuros

* Como usuario, quiero clasificar fragmentos por tema.
* Como usuario, quiero detectar temas recurrentes en un workspace.
* Como usuario, quiero agrupar fragmentos relacionados.
* Como usuario, quiero identificar conceptos importantes dentro del workspace.

---

## 10.5 Categoría: Investigación asistida

### Casos de uso

* Como usuario, quiero recibir sugerencias de búsquedas o líneas de investigación basadas en mi workspace.
* Como usuario, quiero descubrir conceptos relacionados con mi investigación.

---

## 10.6 Reglas de negocio

* Un usuario solo puede ejecutar operaciones de IA sobre sus propios workspaces.
* Un usuario `DISABLED` o `DELETED` no puede usar funcionalidades de IA.
* Un workspace debe tener fragmentos suficientes para ciertas operaciones de IA.
* En V1, las operaciones de IA solo deben procesar fragmentos `ACTIVE`; los fragmentos `ARCHIVED` o `DELETED` no deben enviarse a la IA.
* Las respuestas de IA deben asociarse al usuario y al workspace correspondiente.
* Las operaciones de IA se ejecutarán de forma asíncrona desde la primera versión.
* El sistema debe registrar si una operación de IA ha finalizado correctamente o ha fallado.
* Si se sugieren búsquedas o líneas de investigación, deben estar justificadas en función del contenido guardado.
* La IA no debe inventar enlaces reales ni presentar fuentes externas no verificadas como si hubieran sido consultadas.
* La IA no debe modificar automáticamente fragmentos, etiquetas o contenido del usuario sin confirmación.
* En V1 no se permitirá prompt libre. El usuario podrá configurar opciones controladas de la operación, como idioma de salida, tono o longitud.

---

## 10.7 Operaciones IA de V1

Para la primera versión, se implementarán estas operaciones:

```text
WORKSPACE_SUMMARY
GLOSSARY_GENERATION
TAG_SUGGESTION
MAIN_IDEAS_DETECTION
RESEARCH_QUERY_SUGGESTION
```

`RESEARCH_QUERY_SUGGESTION` representa sugerencias de búsquedas, conceptos o líneas de investigación. No devuelve enlaces externos reales.

---

## 10.8 Estados de operación IA

```text
PENDING
PROCESSING
COMPLETED
FAILED
CANCELLED
```

Las operaciones de IA se implementarán de forma asíncrona desde la primera versión, por lo que estos estados forman parte del comportamiento funcional inicial.

---

## 10.9 Errores esperados

* No hay fragmentos suficientes.
* El contenido es demasiado largo para procesarse en una única petición.
* Error al comunicarse con el proveedor de IA.
* La respuesta de IA no tiene el formato esperado.
* La operación de IA ha fallado.
* Usuario sin permisos sobre el workspace.
* Límite de uso alcanzado.
* Workspace no encontrado.
* Usuario no autenticado.

---

# 11. Módulo: Extensión de navegador

## 11.1 Objetivo

Permitir que el usuario seleccione fragmentos de texto desde páginas web y los guarde en Fragmind.

---

## 11.2 Casos de uso

* Como usuario, quiero seleccionar texto en una página web.
* Como usuario, quiero guardar el texto seleccionado en Fragmind.
* Como usuario, quiero confirmar o cambiar el workspace destino.
* Como usuario, quiero ver si el fragmento se ha guardado correctamente.
* Como usuario, quiero iniciar sesión o conectar la extensión con mi cuenta.
* Como usuario, quiero que la extensión capture automáticamente la URL de origen.
* Como usuario, quiero que la extensión capture el dominio de origen.
* Como usuario, quiero que la extensión capture opcionalmente el título de la página.
* Como usuario, quiero que la extensión capture opcionalmente el favicon.

---

## 11.3 Flujo principal

```text
Usuario selecciona texto en una página web
→ abre la extensión o usa una acción rápida
→ confirma o cambia el workspace destino
→ la extensión obtiene texto, URL, dominio y metadatos básicos
→ envía la información al backend
→ el backend valida la sesión y permisos
→ el backend guarda el fragmento
→ la extensión muestra confirmación
```

---

## 11.4 Reglas de negocio

* El usuario debe estar autenticado para guardar fragmentos.
* El texto seleccionado no puede estar vacío.
* El usuario debe tener un workspace destino seleccionado.
* La extensión puede recordar el último workspace usado o utilizar el workspace por defecto del usuario.
* La extensión no debe guardar fragmentos en workspaces eliminados.
* La extensión no debe guardar fragmentos en workspaces archivados desde el flujo normal de captura.
* Si no se puede obtener título o favicon, el fragmento debe guardarse igualmente.
* Si falla el guardado, la extensión debe informar al usuario.
* La extensión no debe extraer páginas completas en la V1.
* La extensión solo debe enviar el texto seleccionado por el usuario.

---

## 11.5 Errores esperados

* Usuario no autenticado.
* Sesión expirada.
* No hay texto seleccionado.
* Workspace no seleccionado.
* Workspace no encontrado.
* Usuario sin permisos sobre el workspace.
* Error de conexión con el backend.
* Error al guardar fragmento.
* Página no compatible parcialmente.
* Metadatos no disponibles.

---

# 12. Módulo: Límites de uso

## 12.1 Objetivo

Controlar el uso de funcionalidades costosas, especialmente las relacionadas con IA.

---

## 12.2 Casos de uso

* Como sistema, quiero limitar el número de operaciones de IA por usuario.
* Como usuario, quiero saber si he alcanzado un límite de uso.
* Como sistema, quiero evitar abusos o uso excesivo de recursos.
* Como sistema, quiero registrar el consumo de operaciones IA.

---

## 12.3 Reglas de negocio

* Las funcionalidades IA pueden tener límites por usuario.
* Los límites pueden definirse por día, mes o plan.
* Si el usuario supera un límite, el sistema debe mostrar un mensaje claro.
* El guardado de fragmentos puede tener límites amplios o no tener límites en la primera versión.
* Las operaciones de IA deben ser controladas para evitar costes inesperados.

---

## 12.4 Errores esperados

* Límite diario alcanzado.
* Límite mensual alcanzado.
* Operación no disponible para el usuario.
* Uso excesivo detectado.

---

# 13. Módulo: Seguridad y permisos

## 13.1 Objetivo

Garantizar que cada usuario solo pueda acceder y modificar sus propios datos.

---

## 13.2 Reglas de negocio

* Un usuario solo puede acceder a sus propios workspaces.
* Un usuario solo puede acceder a sus propios fragmentos.
* Un usuario solo puede gestionar etiquetas de sus propios workspaces.
* Un usuario solo puede ejecutar IA sobre sus propios workspaces.
* La extensión debe enviar peticiones autenticadas.
* El backend debe validar permisos en cada operación.
* No se debe confiar únicamente en validaciones del frontend o de la extensión.
* Los datos enviados a proveedores de IA deben limitarse al contenido necesario.
* No se deben enviar fragmentos eliminados a la IA.

---

## 13.3 Errores esperados

* Usuario no autenticado.
* Token inválido.
* Sesión expirada.
* Usuario sin permisos.
* Recurso no encontrado.
* Acceso denegado.

---

# 14. Reglas transversales del sistema

Estas reglas aplican a toda la aplicación.

## 14.1 Acceso

* Un usuario no autenticado solo puede acceder a pantallas públicas o de autenticación.
* Un usuario autenticado solo puede acceder a sus propios recursos.
* Un usuario `DISABLED` no puede acceder a módulos privados.
* Un usuario `DELETED` no puede acceder a módulos privados.

---

## 14.2 Eliminación lógica

En la primera versión, se recomienda usar eliminación lógica para entidades principales.

Aplica a:

```text
User
Workspace
Fragment
```

Esto permite evitar pérdida accidental de información y facilita la restauración de workspaces y fragmentos.

---

## 14.3 Auditoría básica

Cada entidad principal debería guardar:

```text
createdAt
updatedAt
```

Opcionalmente:

```text
deletedAt
```

---

## 14.4 Experiencia de usuario

* Las acciones destructivas deben pedir confirmación.
* Los errores deben mostrarse con mensajes comprensibles.
* Las funciones avanzadas no deben saturar la interfaz principal.
* El usuario debe poder guardar fragmentos con pocos pasos.
* La aplicación debe priorizar simplicidad visual aunque tenga capacidades avanzadas.

---

# 15. MVP

## 15.1 Funcionalidades imprescindibles

### Autenticación

* Registro.
* Login.
* Logout.
* Sesión de usuario.
* Creación automática de un workspace inicial por defecto al registrar usuario.

### Workspaces

* Crear workspace.
* Listar workspaces.
* Editar workspace.
* Eliminar workspace.
* Archivar workspace.
* Restaurar workspace archivado.

### Fragmentos

* Guardar fragmento desde extensión.
* Listar fragmentos de un workspace.
* Ver detalle de fragmento.
* Archivar fragmento.
* Eliminar fragmento.
* Restaurar fragmento archivado.
* Ver URL original.

### Fuentes

* Guardar URL.
* Guardar dominio.
* Guardar fecha de extracción.
* Guardar título de página si está disponible.
* Guardar favicon si está disponible.

### Etiquetas

* Crear etiquetas.
* Asignar etiquetas a fragmentos.
* Filtrar por etiquetas.
* Sugerir etiquetas con IA.

### IA

* Generar resumen de workspace.
* Generar glosario.
* Detectar ideas principales.
* Sugerir etiquetas.
* Sugerir búsquedas o líneas de investigación basadas en el workspace.

### Extensión

* Seleccionar texto.
* Elegir workspace.
* Enviar fragmento al backend.
* Mostrar confirmación o error.

---

---

# 16. Flujo principal del producto

```text
Usuario se registra o inicia sesión
→ crea un workspace
→ navega por internet
→ encuentra información relevante
→ selecciona texto en una página web
→ guarda el fragmento mediante la extensión
→ confirma o elige el workspace destino
→ el fragmento queda guardado con su fuente original
→ el usuario consulta sus fragmentos en la app web
→ organiza fragmentos con etiquetas
→ usa IA para resumir, clasificar o ampliar su investigación
```

---

# 17. Flujo de IA recomendado

```text
Usuario abre un workspace
→ selecciona una operación IA
→ el sistema obtiene fragmentos activos del workspace
→ el sistema valida permisos y límites de uso
→ se crea una operación IA en estado `PENDING`
→ el backend procesa la operación de forma asíncrona
→ la operación pasa por `PROCESSING` y termina en `COMPLETED` o `FAILED`
→ si finaliza correctamente, el usuario revisa el resultado
→ el usuario decide si guardar, copiar o descartar el resultado
```

---

# 18. Entidades iniciales sugeridas

## User

```text
id
email
password
displayName
defaultWorkspaceId
status
createdAt
updatedAt
deletedAt
```

---

## Workspace

```text
id
userId
name
description
status
createdAt
updatedAt
deletedAt
```

---

## Fragment

```text
id
userId
workspaceId
selectedText
sourceUrl
sourceDomain
pageTitle
faviconUrl
userNote
language
status
extractedAt
createdAt
updatedAt
deletedAt
```

---

## Tag

```text
id
workspaceId
name
color
createdByAi
createdAt
updatedAt
```

---

## FragmentTag

```text
fragmentId
tagId
createdAt
```

---

## AiOperation

```text
id
userId
workspaceId
operationType
status
inputScope
inputFragmentIds
inputSummary
result
errorCode
errorMessage
createdAt
startedAt
updatedAt
completedAt
failedAt
cancelledAt
```

---

## UsageQuota

Controla límites aplicables por usuario, plan, funcionalidad y periodo.

```text
id
userId
feature
period
limit
used
resetAt
createdAt
updatedAt
```

## AiUsage

Registra consumo real de IA para auditoría y análisis de costes.

```text
id
userId
aiOperationId
operationType
inputUnits
outputUnits
provider
model
createdAt
```

---

# 19. Tipos de operación IA

```text
WORKSPACE_SUMMARY
GLOSSARY_GENERATION
MAIN_IDEAS_DETECTION
TAG_SUGGESTION
RESEARCH_QUERY_SUGGESTION
```

## 19.1 Mapeo entre operación IA y feature limitable

```text
WORKSPACE_SUMMARY -> AI_SUMMARY
GLOSSARY_GENERATION -> AI_GLOSSARY
MAIN_IDEAS_DETECTION -> AI_MAIN_IDEAS
TAG_SUGGESTION -> AI_TAG_SUGGESTION
RESEARCH_QUERY_SUGGESTION -> AI_RESEARCH_QUERY_SUGGESTION
```

---

# 20. Decisiones de producto y alcance

Estas decisiones definen el comportamiento esperado de la primera versión.

---

## 20.1 Eliminación de cuenta

La eliminación de cuenta se gestionará inicialmente como una **eliminación lógica**.

Cuando un usuario solicite eliminar su cuenta:

* La cuenta pasará al estado `DELETED`.
* El usuario no podrá iniciar sesión de forma normal.
* Sus datos quedarán marcados como pendientes de eliminación definitiva.
* Se conservará la posibilidad de reactivar la cuenta durante un periodo determinado.
* Pasado ese periodo, la cuenta y sus datos asociados podrán ser eliminados físicamente del sistema.

### Reactivación

Un usuario con cuenta eliminada lógicamente podrá solicitar la reactivación de su cuenta siempre que todavía no se haya ejecutado la eliminación física.

### Reutilización del email

Mientras la cuenta esté en estado `DELETED` pero no haya sido eliminada físicamente, el email seguirá asociado a esa cuenta y no podrá reutilizarse para crear una nueva cuenta.

Una vez ejecutada la eliminación física, el email podrá volver a utilizarse, salvo que por motivos legales, de auditoría o seguridad se decida conservar algún registro mínimo no reutilizable.

### Reglas de negocio

* Un usuario `DELETED` no puede iniciar sesión.
* Un usuario `DELETED` puede solicitar reactivación si la eliminación física aún no se ha ejecutado.
* La eliminación física se realizará tras un periodo de retención definido.
* La duración exacta del periodo de retención queda pendiente de decisión técnica/legal.

---

## 20.2 Verificación de email

Para la primera versión de la aplicación no será obligatorio verificar el email.

El usuario podrá registrarse e iniciar sesión directamente con email y contraseña, siempre que el email no esté ya registrado y la cuenta esté en estado válido.

### Reglas de negocio

* Un usuario recién registrado quedará en estado `ACTIVE`.
* No se enviará email de verificación en la primera versión.
* No existirá inicialmente el estado `PENDING_VERIFICATION` como requisito funcional obligatorio.
* El campo `emailVerified` existirá como dato informativo, pero no bloqueará el uso de la aplicación en V1.
* En V1, `emailVerified = false` y `emailVerifiedAt = null` al registrar usuario.

---

## 20.3 Procesamiento de IA

Las operaciones de IA se implementarán desde el inicio de forma **asíncrona**.

Se decide utilizar un enfoque asíncrono desde la primera versión para soportar operaciones largas, controlar estados y registrar consumo de forma fiable.

### Comportamiento esperado

Cuando el usuario solicite una operación de IA:

* El sistema creará una tarea de procesamiento.
* La operación quedará en estado pendiente.
* El backend procesará la tarea de forma asíncrona.
* El usuario podrá consultar el estado de la operación.
* Cuando finalice, el resultado quedará disponible en la aplicación.

### Estados posibles de una operación IA

```text
PENDING
PROCESSING
COMPLETED
FAILED
CANCELLED
```

### Operaciones IA de V1

```text
WORKSPACE_SUMMARY
GLOSSARY_GENERATION
MAIN_IDEAS_DETECTION
TAG_SUGGESTION
RESEARCH_QUERY_SUGGESTION
```

### Reglas de negocio

* Un usuario solo puede solicitar operaciones IA si su plan y cuota lo permiten.
* Cada operación IA debe quedar registrada.
* Una operación IA puede fallar sin afectar al workspace ni a los fragmentos originales.
* El usuario debe poder seguir usando la aplicación aunque una operación IA esté pendiente.
* El resultado de IA debe asociarse al workspace, fragmento o conjunto de fragmentos correspondiente.
* Si una operación falla, debe guardarse el motivo del fallo de forma controlada.

### Modelo conceptual sugerido

```text
AiOperation
- id
- userId
- workspaceId
- operationType
- status
- inputScope
- inputFragmentIds
- inputSummary
- result
- errorCode
- errorMessage
- createdAt
- startedAt
- updatedAt
- completedAt
- failedAt
- cancelledAt
```

---

## 20.4 Recomendación de búsquedas

La aplicación permitirá que la IA recomiende nuevas búsquedas, conceptos o líneas de investigación relacionadas con el contenido activo del workspace.

La funcionalidad no recomendará enlaces externos reales en V1.

### Flujo esperado

1. El usuario solicita recomendaciones para continuar investigando.
2. El sistema analiza el contenido activo del workspace.
3. La IA genera posibles búsquedas, conceptos o líneas de investigación.
4. El sistema muestra las sugerencias al usuario.

### Reglas de negocio

* Las sugerencias deben basarse en el contenido guardado por el usuario.
* La IA puede sugerir términos de búsqueda, conceptos o líneas de investigación.
* La IA no debe inventar enlaces reales ni presentar fuentes externas no verificadas como si hubieran sido consultadas.
* El usuario decide si utiliza o descarta las sugerencias.

---

## 20.5 Límites de IA y planes de usuario

La aplicación está pensada como un producto de pago, pero incluirá un plan gratuito limitado para que nuevos usuarios puedan probar la aplicación antes de contratar un plan superior.

### Plan gratuito

El plan gratuito permitirá utilizar la aplicación con ciertas limitaciones.

El usuario gratuito podrá:

* registrarse,
* iniciar sesión,
* crear workspaces,
* guardar fragmentos,
* consultar sus contenidos,
* probar funcionalidades de IA dentro de un límite definido.

Cuando alcance el límite gratuito de IA:

* seguirá teniendo acceso a su cuenta,
* seguirá pudiendo ver sus workspaces,
* seguirá pudiendo ver sus fragmentos,
* no podrá ejecutar nuevas operaciones de IA hasta que se reinicie la cuota o cambie de plan.

El usuario seguirá en estado:

```text
ACTIVE
```

No se usará `DISABLED` para representar que ha agotado su límite gratuito.

### Gestión de límites

Los límites de IA se gestionarán mediante cuotas asociadas al plan del usuario, no mediante el estado principal de la cuenta.

### Modelo conceptual

```text
Plan
- id
- name
- type
- price
- billingPeriod
- isActive

UserPlan
- id
- userId
- planId
- status
- startedAt
- expiresAt

UsageQuota
- id
- userId
- feature
- period
- limit
- used
- resetAt
```

### Tipos de plan iniciales

```text
FREE
PAID
```

### Funcionalidades limitables

```text
AI_SUMMARY
AI_GLOSSARY
AI_TAG_SUGGESTION
AI_MAIN_IDEAS
AI_RESEARCH_QUERY_SUGGESTION
WORKSPACE_CREATION
FRAGMENT_STORAGE
```

`PDF_EXPORT` queda reservado para una versión futura y no tendrá endpoints activos en V1.

### Decisión pendiente

Queda pendiente definir el número exacto de operaciones incluidas en el plan gratuito.

Esta decisión dependerá del coste real de uso de la API de IA, incluyendo:

* coste por modelo utilizado,
* coste por tokens de entrada,
* coste por tokens de salida,
* frecuencia esperada de uso,
* margen comercial deseado,
* coste de infraestructura adicional.

### Reglas de negocio

* Un usuario gratuito tendrá una cuota limitada de operaciones IA.
* El número exacto de operaciones gratuitas se definirá tras analizar costes.
* El sistema debe registrar el consumo de operaciones IA por usuario.
* El sistema debe impedir nuevas operaciones IA cuando la cuota esté agotada.
* Agotar la cuota de IA no cambia el estado del usuario.
* El usuario podrá seguir accediendo a sus contenidos aunque haya agotado su cuota.
* La aplicación debe separar el estado de la cuenta del estado de las cuotas de uso.

---


# 21. Conclusión

Esta especificación define una primera versión funcional de Fragmind centrada en:

* capturar fragmentos relevantes desde la web,
* organizarlos en workspaces,
* conservar sus fuentes,
* estructurarlos mediante etiquetas,
* y usar IA para comprender, organizar y ampliar la investigación.

La aplicación debe mantenerse simple para el usuario y coherente con el alcance definido para la primera versión.
