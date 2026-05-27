# Fragmind — Información para versiones futuras

## 1. Objetivo del documento

Este documento recopila ideas, funcionalidades, modelos y decisiones que han aparecido durante la definición inicial de Fragmind, pero que no forman parte de la definición cerrada de la primera versión.

La intención es no perder información útil, manteniéndola separada de los documentos principales del producto, la especificación funcional, los modelos de dominio y la API.

Este documento no representa alcance de V1. Es una referencia para revisar cuando se quiera planificar nuevas versiones.

---

## 2. Principio de uso

Antes de incorporar cualquiera de estas ideas al producto, debería revisarse:

- Si aporta valor claro al usuario.
- Si encaja con la propuesta principal de Fragmind.
- Si aumenta demasiado la complejidad del producto.
- Si requiere cambios relevantes en modelo de datos, API, frontend, extensión o infraestructura.
- Si afecta al coste de IA o al modelo de negocio.

---

## 3. Funcionalidades de investigación avanzada

### 3.1 Chat contextual sobre un workspace

Permitir que el usuario converse con la IA usando como contexto los fragmentos guardados en un workspace.

Posibles capacidades:

- Preguntar sobre el contenido guardado.
- Pedir explicaciones sobre fragmentos concretos.
- Solicitar comparaciones entre ideas.
- Pedir que la IA cite o referencie fragmentos concretos.

Consideraciones:

- Requiere controlar bien qué fragmentos se envían como contexto.
- Puede necesitar búsqueda semántica o recuperación por relevancia.
- Puede incrementar mucho el consumo de tokens.
- Conviene diferenciar entre conversación temporal e historial persistente.

---

### 3.2 Búsqueda semántica

Permitir encontrar fragmentos no solo por coincidencia textual, sino por significado.

Ejemplos:

- Buscar “ventajas de microservicios” y encontrar fragmentos que hablen de escalabilidad, despliegue independiente o resiliencia aunque no usen esas palabras exactas.
- Buscar conceptos relacionados dentro de un workspace.

Posibles cambios técnicos:

```text
fragment_embeddings
- id
- fragmentId
- embedding
- provider
- model
- createdAt
```

Índices futuros posibles:

```text
fragments.selected_text full-text index
fragments.user_note full-text index
fragment_embeddings vector index
```

Consideraciones:

- Requiere generar embeddings.
- Añade coste por fragmento capturado o procesado.
- Puede requerir una base de datos o extensión compatible con búsqueda vectorial.

---

### 3.3 Detección de contradicciones

Permitir que la IA detecte posibles contradicciones entre fragmentos de distintas fuentes.

Ejemplos:

- Una fuente afirma que una tecnología es adecuada para cierto caso y otra afirma lo contrario.
- Dos fragmentos dan datos incompatibles.
- Hay diferencias entre recomendaciones de distintas fuentes.

Operación IA posible:

```text
CONTRADICTION_DETECTION
```

Consideraciones:

- No debe presentarse como verdad absoluta.
- La IA debería mostrar qué fragmentos generan la posible contradicción.
- Es importante conservar la fuente original para que el usuario pueda revisar.

---

### 3.4 Comparación de puntos de vista

Permitir comparar opiniones, enfoques o argumentos encontrados durante la investigación.

Operación IA posible:

```text
VIEWPOINT_COMPARISON
```

Ejemplos:

- Comparar argumentos a favor y en contra.
- Comparar enfoques técnicos.
- Comparar diferentes autores o fuentes.

---

### 3.5 Detección de ideas repetidas o patrones

Permitir detectar temas recurrentes dentro de un workspace.

Posibles usos:

- Saber qué ideas aparecen en varias fuentes.
- Identificar conceptos dominantes.
- Reducir duplicidad de información.

Operaciones relacionadas:

```text
TOPIC_CLASSIFICATION
RELATED_CONCEPTS_SUGGESTION
```

---

### 3.6 Relación automática entre fragmentos

Permitir que el sistema relacione fragmentos que hablan de conceptos similares o complementarios.

Posibles usos:

- Mostrar fragmentos relacionados.
- Crear agrupaciones automáticas.
- Construir una vista de conexiones dentro del workspace.

Consideraciones:

- Puede apoyarse en embeddings.
- Puede confundirse con una estructura demasiado compleja si se introduce pronto.
- Conviene mantenerlo como asistencia, no como organización obligatoria.

---

## 4. Recomendación de fuentes externas reales

### 4.1 Descripción

La recomendación de fuentes externas reales consiste en sugerir artículos, documentación, libros, vídeos u otros recursos concretos relacionados con el contenido del workspace.

Esta funcionalidad no debe basarse únicamente en la generación de texto por IA. Para recomendar enlaces reales de forma fiable, el sistema debería consultar una fuente externa verificable.

---

### 4.2 Integraciones posibles

Opciones técnicas:

```text
- API de búsqueda web.
- Motor de búsqueda propio.
- Servicio externo de recuperación de documentos.
- Integración con fuentes académicas o documentales.
```

---

### 4.3 Flujo posible

```text
1. El usuario solicita fuentes para ampliar su investigación.
2. El sistema analiza el contenido activo del workspace.
3. La IA genera consultas de búsqueda relevantes.
4. El sistema ejecuta búsquedas reales mediante una integración externa.
5. La IA analiza y ordena los resultados obtenidos.
6. El sistema muestra fuentes recomendadas con justificación.
7. El usuario decide si guarda alguna fuente o fragmento relacionado.
```

---

### 4.4 Modelo futuro posible

```text
ExternalRecommendedSource
- id
- aiOperationId
- workspaceId
- title
- url
- description
- reason
- provider
- createdAt
```

---

### 4.5 Datos mínimos de una fuente recomendada

```text
title
url
description
reason
provider
```

Ejemplo:

```json
{
  "title": "Artificial intelligence in education: challenges and opportunities",
  "url": "https://example.com/article",
  "description": "Artículo sobre el impacto de la IA en contextos educativos.",
  "reason": "Complementa los fragmentos guardados sobre aprendizaje asistido por IA.",
  "provider": "web-search-provider"
}
```

---

## 5. Operaciones IA adicionales

Estas operaciones aparecieron durante la definición, pero no forman parte del conjunto cerrado de operaciones iniciales.

```text
SELECTED_FRAGMENTS_SUMMARY
FAQ_GENERATION
TOPIC_CLASSIFICATION
RELATED_CONCEPTS_SUGGESTION
SOURCE_RECOMMENDATION
CONTRADICTION_DETECTION
VIEWPOINT_COMPARISON
QUESTION_GENERATION
COMPARISON
```

### 5.1 SELECTED_FRAGMENTS_SUMMARY

Resumen basado solo en fragmentos seleccionados manualmente por el usuario.

Valor:

- Permite resumir una parte concreta del workspace.
- Reduce coste frente a procesar todo el workspace.
- Da más control al usuario.

---

### 5.2 FAQ_GENERATION

Generación de preguntas frecuentes a partir de los fragmentos guardados.

Valor:

- Útil para estudiar.
- Útil para preparar documentación.
- Útil para transformar investigación en contenido explicativo.

---

### 5.3 TOPIC_CLASSIFICATION

Clasificación de fragmentos por temas detectados automáticamente.

Valor:

- Ayuda a organizar workspaces grandes.
- Puede sugerir etiquetas o agrupaciones.

---

### 5.4 RELATED_CONCEPTS_SUGGESTION

Sugerencia de conceptos relacionados con lo investigado.

Valor:

- Ayuda al usuario a descubrir líneas de aprendizaje.
- Encaja con investigación asistida sin necesidad de recomendar enlaces reales.

---

### 5.5 SOURCE_RECOMMENDATION

Recomendación de fuentes externas reales.

Condición:

- Solo debería implementarse cuando exista integración con búsqueda externa fiable.
- La IA no debería inventar enlaces ni presentar fuentes no verificadas como reales.

---

### 5.6 QUESTION_GENERATION

Generación de preguntas de estudio, reflexión o investigación a partir de los fragmentos.

Valor:

- Útil para aprendizaje.
- Puede ayudar a detectar huecos en la investigación.

---

### 5.7 COMPARISON / VIEWPOINT_COMPARISON

Comparación de fragmentos, fuentes, argumentos o enfoques.

Valor:

- Útil en investigaciones donde hay varias opiniones.
- Puede ayudar en toma de decisiones.

---

## 6. Resultados IA más estructurados

En la primera versión puede ser suficiente guardar resultados IA como contenido flexible, por ejemplo Markdown.

Más adelante puede ser útil definir respuestas JSON específicas por tipo de operación.

### 6.1 Ejemplo para glosario

```json
{
  "terms": [
    {
      "term": "Gateway",
      "definition": "Componente que enruta peticiones hacia servicios internos.",
      "relatedFragmentIds": []
    }
  ]
}
```

### 6.2 Ejemplo para ideas principales

```json
{
  "mainIdeas": [
    {
      "title": "Escalabilidad independiente",
      "description": "Los microservicios permiten escalar partes concretas del sistema.",
      "relatedFragmentIds": []
    }
  ]
}
```

### 6.3 Ejemplo para sugerencias de búsqueda

```json
{
  "queries": [
    {
      "query": "Patrones de comunicación entre microservicios",
      "reason": "El workspace contiene varios fragmentos sobre arquitectura distribuida."
    }
  ]
}
```

Ventajas:

- Mejora la presentación en UI.
- Facilita guardar resultados parciales.
- Permite aplicar acciones concretas sobre resultados.

Coste:

- Requiere validación de estructura.
- La IA puede devolver JSON inválido si no se controla bien.
- Cada operación necesita contrato propio.

---

## 7. Historial avanzado de operaciones IA

### 7.1 Descripción

Registrar no solo la operación IA y su resultado, sino también qué hizo el usuario con ese resultado.

Modelo posible:

```text
AiResultDecision
- id
- aiOperationId
- userId
- decision
- createdAt
```

Valores posibles:

```text
SAVED
COPIED
DISCARDED
APPLIED
```

Valor:

- Permite saber qué resultados fueron útiles.
- Facilita auditoría.
- Puede ayudar a mejorar prompts y operaciones.

---

## 8. Organización avanzada

### 8.1 Carpetas o subcarpetas

Modelo posible:

```text
Folder
- id
- workspaceId
- parentFolderId
- name
- createdAt
- updatedAt
```

Valor:

- Organización jerárquica para workspaces grandes.

Riesgo:

- Puede convertir Fragmind en una app de notas genérica.
- Añade fricción a la captura rápida.
- Puede duplicar parcialmente la función de etiquetas.

---

### 8.2 Mapas conceptuales

Permitir visualizar conceptos, fragmentos y relaciones en forma de grafo o mapa mental.

Valor:

- Útil para aprendizaje.
- Útil para ver relaciones entre conceptos.

Riesgo:

- Mucha complejidad visual.
- Puede ser difícil de hacer bien en una primera etapa.

---

### 8.3 Modo lectura o revisión

Vista enfocada para repasar fragmentos guardados.

Posibles funciones:

- Leer fragmentos uno tras otro.
- Ocultar distracciones.
- Marcar como revisado.
- Mostrar notas personales.
- Mostrar fuente original.

---

## 9. Exportaciones

Exportar contenido del workspace o de fragmentos seleccionados. Esta línea queda fuera de V1 y no debe aparecer como endpoint activo ni como límite activo hasta que se defina una funcionalidad de exportación concreta.

Formatos mencionados:

```text
Markdown
PDF
TXT
Documentos
```

Posibles casos de uso:

- Preparar apuntes.
- Exportar investigación para un trabajo.
- Compartir recopilación de fragmentos.
- Generar documentación inicial.

Consideraciones:

- Puede ser una funcionalidad limitable por plan.
- PDF puede requerir más trabajo de maquetación.
- Markdown puede ser el formato inicial más sencillo.

---

## 10. Extracción automática de páginas completas

### 10.1 Descripción

Permitir guardar o procesar una página completa automáticamente, no solo fragmentos seleccionados.

Valor:

- Reduce trabajo manual.
- Útil para artículos largos.
- Puede ayudar a crear resúmenes completos.

Riesgos:

- Se aleja del principio de que el usuario selecciona conocimiento relevante.
- Puede introducir mucho ruido.
- Requiere extracción robusta por sitio web.
- Puede aumentar costes de IA.
- Puede tener implicaciones legales o de términos de uso según la fuente.

---

## 11. Integraciones externas

Posibles integraciones:

```text
- Herramientas de notas.
- Gestores de conocimiento.
- Exportación a repositorios o documentación.
- Integraciones académicas o documentales.
- APIs de búsqueda web.
```

Criterio recomendado:

- No incorporar integraciones hasta que el flujo principal de captura, organización e IA esté validado.

---

## 12. Colaboración y workspaces compartidos

### 12.1 Descripción

Permitir que varios usuarios trabajen sobre un mismo workspace.

Modelos posibles:

```text
SharedWorkspace
CollaborationMember
WorkspaceMember
```

Campos posibles:

```text
workspaceId
userId
role
status
createdAt
```

Roles posibles:

```text
OWNER
EDITOR
VIEWER
```

Valor:

- Útil para equipos, estudiantes o investigación compartida.
- Puede habilitar planes Team.

Complejidad:

- Permisos por recurso.
- Invitaciones.
- Auditoría de acciones.
- Conflictos de edición.
- Facturación por equipo.

---

## 13. Roles y permisos avanzados

En la definición inicial, la autorización se basa en propiedad del recurso. En versiones futuras podrían aparecer roles si hay:

- Panel administrativo.
- Colaboración.
- Moderación.
- Soporte.
- Planes de equipo o empresa.

Modelos posibles:

```text
Role
Permission
UserRole
```

Roles mencionados durante la ideación:

```text
USER
ADMIN
MODERATOR
SUPPORT
MANAGER
OWNER
```

Nota:

- Un usuario PRO no debería modelarse como rol. Debería representarse mediante plan o suscripción.

---

## 14. Planes de pago avanzados

La primera definición contempla planes simples. Para versiones futuras pueden definirse planes más específicos:

```text
FREE
PRO
TEAM
ENTERPRISE
```

Aspectos a definir:

- Precio de cada plan.
- Límite de fragmentos.
- Límite de workspaces.
- Límite de operaciones IA.
- Límite de tokens o coste real por IA.
- Exportaciones incluidas.
- Funciones avanzadas incluidas.
- Uso individual o por equipo.

Funcionalidades limitables mencionadas:

```text
AI_SUMMARY
AI_GLOSSARY
AI_TAG_SUGGESTION
AI_MAIN_IDEAS
AI_RESEARCH_QUERY_SUGGESTION
AI_SOURCE_RECOMMENDATION
AI_QUESTION_GENERATION
AI_COMPARISON
PDF_EXPORT
WORKSPACE_CREATION
FRAGMENT_STORAGE
```

---

## 15. Verificación de email

En V1 no se requiere verificación de email, pero podría incorporarse más adelante.

Campos ya contemplables:

```text
emailVerified
emailVerifiedAt
```

Comportamiento futuro posible:

```text
1. Usuario se registra.
2. La cuenta queda pendiente de verificación.
3. Se envía email de confirmación.
4. El usuario confirma el email.
5. La cuenta queda habilitada para uso completo.
```

Estado futuro posible:

```text
PENDING_VERIFICATION
```

Consideraciones:

- Puede reducir abuso.
- Añade fricción al registro.
- Puede ser necesario para recuperación de cuenta o comunicaciones importantes.

---

## 16. Papelera completa y restauración avanzada

Aunque la eliminación lógica ya permite conservar datos, una papelera completa podría incluir:

- Listar workspaces eliminados.
- Listar fragmentos eliminados.
- Restaurar elementos eliminados.
- Borrado físico manual.
- Borrado físico automático tras periodo de retención.

Consideraciones:

- Hay que decidir cuánto tiempo se conservan los datos.
- Puede tener implicaciones legales y de privacidad.
- Conviene diferenciar entre `ARCHIVED` y `DELETED`.

---

## 17. Autenticación alternativa con cookie HTTP-only

La decisión actual es usar JWT con refresh token. Aun así, se evaluó como alternativa usar cookies HTTP-only de sesión.

Ventajas potenciales:

- Mejor protección frente a acceso directo desde JavaScript.
- Modelo clásico de sesión web.
- Puede simplificar la app web si todo vive bajo el mismo dominio.

Dificultades:

- Integración con extensión de navegador.
- Configuración CORS.
- SameSite, dominios y subdominios.
- Manejo de sesión en distintos clientes.

Esta alternativa queda descartada para la definición actual, pero documentada por si en el futuro cambia la arquitectura.

---

## 18. Agentes autónomos de investigación

Funcionalidad avanzada donde el sistema no solo sugiere búsquedas, sino que ejecuta pasos de investigación de forma autónoma.

Ejemplos:

- Buscar fuentes.
- Leer resultados.
- Extraer puntos clave.
- Proponer nuevos fragmentos.
- Construir un informe.

Riesgos:

- Mucha complejidad.
- Coste elevado.
- Riesgo de resultados incorrectos.
- Necesidad de trazabilidad fuerte.
- Se aleja del principio de que el usuario decide qué guardar.

---

## 19. Modelos futuros recopilados

```text
ExternalRecommendedSource
AiResultDecision
Folder
SharedWorkspace
WorkspaceMember
Role
Permission
FragmentEmbedding
Source
```

### 19.1 Source como entidad independiente

En V1, la fuente puede ser metadata embebida dentro de `Fragment`. Más adelante podría convertirse en entidad propia si se necesita:

- Deduplicar fuentes.
- Agrupar fragmentos por página.
- Guardar estadísticas por fuente.
- Gestionar fuentes recomendadas.
- Detectar varias capturas desde una misma página.

Modelo posible:

```text
Source
- id
- workspaceId
- url
- domain
- pageTitle
- faviconUrl
- createdAt
- updatedAt
```

---

## 20. Ideas que deben tratarse con cuidado

Estas ideas pueden aportar valor, pero también pueden diluir el producto:

```text
- Notas independientes.
- Extracción automática de páginas completas.
- Carpetas profundas.
- Agentes autónomos.
- Chat genérico sin contexto.
- Recomendación de enlaces sin búsqueda real.
```

Criterio recomendado:

Fragmind debe seguir centrado en capturar fragmentos relevantes, organizarlos por workspace y analizarlos con IA usando el conocimiento recopilado por el usuario.

---

## 21. Resumen

Este documento conserva información útil para futuras versiones sin mezclarla con el alcance actual del producto.

Las líneas de evolución más relevantes son:

1. Chat contextual.
2. Búsqueda semántica.
3. Recomendación de fuentes externas reales.
4. Operaciones IA avanzadas.
5. Exportaciones.
6. Colaboración.
7. Planes avanzados.
8. Organización avanzada.
9. Historial avanzado de IA.
10. Integraciones externas.

Cualquier incorporación futura debería evaluarse como una decisión explícita de producto, no como parte implícita del MVP.
