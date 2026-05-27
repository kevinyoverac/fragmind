# Fragmind — Product Brief

## 1. Resumen

**Fragmind** es una aplicación de investigación asistida por inteligencia artificial que permite al usuario seleccionar fragmentos relevantes de páginas web, guardarlos en un workspace y analizarlos con IA.

El objetivo del producto no es guardar páginas completas ni convertirse en una aplicación de notas genérica, sino ayudar al usuario a recopilar conocimiento relevante durante una investigación y convertirlo en información organizada, comprensible y accionable.

---

## 2. Problema

Cuando una persona investiga sobre un tema en internet, suele abrir múltiples páginas, leer contenido de distintas fuentes y guardar información de forma dispersa mediante favoritos, notas manuales, documentos o capturas.

Con el tiempo, esta información suele quedar desorganizada, es difícil volver a encontrarla y todavía más difícil reutilizarla para entender un tema, comparar ideas, generar resúmenes o continuar investigando.

El usuario necesita una forma sencilla de guardar únicamente los fragmentos que considera importantes, conservar su fuente original y analizarlos dentro de un contexto organizado.

---

## 3. Solución

Fragmind permite al usuario seleccionar texto directamente desde una página web mediante una extensión de navegador y enviarlo a una aplicación web.

Cada fragmento seleccionado se guarda dentro de un **workspace** junto con información básica de la fuente original, como la URL, el dominio y la fecha de extracción. Opcionalmente, también se puede guardar el título de la página y el favicon.

Una vez recopilada la información, el usuario puede utilizar inteligencia artificial para comprender, organizar y ampliar su investigación.

La IA trabaja principalmente sobre el contenido guardado por el usuario y puede asistir recomendando conceptos relacionados, búsquedas o líneas de investigación basadas en el conocimiento ya recopilado. La recomendación de enlaces externos reales no forma parte de la definición inicial del producto.

---

## 4. Usuario objetivo

Fragmind está dirigido a personas que necesitan investigar en internet como parte de su trabajo, estudios o proyectos personales.

Ejemplos de usuarios:

- Estudiantes que recopilan información para trabajos, exámenes o proyectos.
    
- Desarrolladores que investigan documentación técnica, herramientas o arquitecturas.
    
- Creadores de contenido que recopilan referencias para artículos, vídeos o publicaciones.
    
- Profesionales que investigan temas concretos para tomar decisiones.
    
- Personas que aprenden sobre un tema y quieren organizar lo que van descubriendo.
    

Aunque el producto puede ser utilizado por cualquier persona, su enfoque principal es ayudar a usuarios que realizan procesos de investigación recurrentes.

---

## 5. Propuesta de valor

Fragmind ayuda al usuario a transformar información dispersa de internet en conocimiento organizado.

La propuesta de valor principal es:

> Guardar fragmentos relevantes de distintas fuentes y analizarlos con inteligencia artificial dentro de un workspace de investigación.

A diferencia de un gestor de favoritos, Fragmind no se centra en guardar enlaces completos.

A diferencia de una app de notas genérica, Fragmind se centra en capturar conocimiento desde la web.

A diferencia de un chat con IA genérico, Fragmind parte del contenido seleccionado por el usuario y utiliza ese contexto para ayudarle a comprender, organizar y ampliar su investigación.

---

## 6. Principios del producto

- El usuario decide qué información guardar.
    
- La captura de información debe ser rápida y sencilla.
    
- La fuente original siempre debe conservarse.
    
- La IA debe trabajar sobre conocimiento relevante y contextualizado.
    
- La aplicación debe ser potente internamente, pero simple visualmente.
    
- La organización debe ayudar a investigar, no complicarla.
    
- Las funcionalidades avanzadas deben estar disponibles sin saturar la experiencia principal.
    
- La IA debe asistir al usuario, no sustituir su criterio.
    

---

## 7. Concepto principal: Workspace

El concepto central de Fragmind es el **workspace**.

Un workspace representa un espacio de investigación sobre un tema concreto.

Ejemplos:

- “Aprender sobre inteligencia artificial”
    
- “Arquitectura de microservicios”
    
- “Preparación entrevista backend”
    
- “Trabajo final de grado”
    
- “Investigación sobre productividad”
    

Dentro de cada workspace, el usuario puede guardar fragmentos, consultar sus fuentes, organizar contenido y utilizar herramientas de IA.

### Organización inicial dentro de un workspace

En una primera versión, un workspace puede contener:

- Fragmentos guardados.
    
- Fuentes asociadas.
    
- Etiquetas.
    
- Resúmenes generados por IA.
    
- Glosarios.
    
- Sugerencias de búsquedas o líneas de investigación.
    

Quedan fuera de la primera versión, aunque pueden evaluarse más adelante: preguntas frecuentes, clasificación temática avanzada, agrupación automática de fragmentos relacionados, comparación de puntos de vista, detección de contradicciones y recomendación de fuentes externas reales.

Para evitar complejidad inicial, no se incluirán subcarpetas ni jerarquías profundas en la primera versión.

---

## 8. Funcionalidades principales

Esta sección describe a alto nivel las grandes áreas funcionales del producto. El detalle de casos de uso, reglas de negocio, estados y errores se mantiene en `functional-specification.md`.

### 8.1 Captura de fragmentos

El usuario selecciona texto en una página web y lo guarda en Fragmind mediante una extensión de navegador, conservando la fuente original. Opcionalmente puede añadir notas personales y etiquetas. Las notas manuales no son entidades independientes en la primera versión: solo existen asociadas a un fragmento concreto.

### 8.2 Gestión de workspaces

El usuario puede crear, editar, archivar y eliminar workspaces para organizar sus investigaciones por tema, y consultar los fragmentos guardados en cada uno.

### 8.3 Organización de fragmentos

El usuario organiza los fragmentos mediante etiquetas (manuales o sugeridas por IA), filtros por fuente y etiqueta, y búsqueda de texto dentro de un workspace.

### 8.4 Comprensión con IA

La IA ayuda a entender el contenido recopilado generando resúmenes del workspace, glosarios de términos e ideas principales.

### 8.5 Organización con IA

La IA ayuda a estructurar la información sugiriendo etiquetas a partir del contenido del workspace.

### 8.6 Investigación asistida con IA

La IA recomienda búsquedas, líneas de investigación y conceptos relacionados, siempre guiada por el contenido del workspace, no por búsquedas genéricas sin contexto. La recomendación de enlaces externos reales no forma parte de la primera versión.

---

## 9. Qué NO hará por ahora

En la primera versión, Fragmind no hará lo siguiente:

- Extraer automáticamente todo el contenido de una página web con un solo clic.
    
- Actuar como una app de notas genérica tipo Notion.
    
- Funcionará como un simple gestor de favoritos.
    
- Realizar scraping avanzado de cualquier web.
    
- Crear subcarpetas o estructuras jerárquicas complejas.
    
- Generar mapas mentales avanzados.
    
- Crear relaciones automáticas complejas entre notas.
    
- Ejecutar agentes autónomos de investigación.
    
- Buscar información externa sin estar guiado por el contenido guardado por el usuario.
    
- Soportar colaboración entre múltiples usuarios.
    

---

## 10. MVP

La primera versión usable de Fragmind debe permitir al usuario realizar el flujo principal del producto de principio a fin.

### Funcionalidades mínimas del MVP

#### Extensión de navegador

- Seleccionar texto de una página web.
    
- Enviar el texto seleccionado a Fragmind.
    
- Asociar el fragmento a un workspace.
    
- Capturar URL, dominio y fecha de extracción.
    
- Capturar opcionalmente título de la página y favicon.
    

#### Aplicación web

- Registro e inicio de sesión de usuario.
    
- Creación automática de un workspace inicial por defecto.
    
- Crear y gestionar workspaces.
    
- Visualizar fragmentos guardados.
    
- Ver la fuente original de cada fragmento.
    
- Buscar fragmentos dentro de un workspace.
    
- Añadir etiquetas básicas.
    
- Añadir notas personales asociadas a fragmentos concretos, sin permitir notas independientes en la V1.
    
- Archivar y restaurar fragmentos archivados.
    
- Generar un resumen con IA a partir de los fragmentos de un workspace.
    
- Generar un glosario básico con IA.
    
- Sugerir etiquetas con IA.
    
- Sugerir búsquedas o líneas de investigación relacionadas con el workspace.
    

---

## 11. Flujo principal del usuario

```text
Usuario investiga en internet
→ encuentra información relevante
→ selecciona un fragmento de texto
→ lo guarda mediante la extensión
→ confirma o elige el workspace destino
→ el fragmento aparece en la aplicación web
→ el usuario organiza los fragmentos
→ la IA ayuda a resumir, extraer ideas principales, sugerir etiquetas o proponer nuevas búsquedas
```

---

## 12. Diferenciación

Fragmind se diferencia por combinar tres elementos:

### 1. Selección manual de conocimiento

El usuario guarda únicamente lo que considera importante, reduciendo ruido y evitando almacenar páginas completas innecesarias.

### 2. Contexto de investigación

Los fragmentos se organizan dentro de workspaces, lo que permite entender cada información dentro de un tema concreto.

### 3. IA basada en conocimiento recopilado

La inteligencia artificial no parte de cero ni actúa como un chat genérico. Trabaja sobre el contenido seleccionado por el usuario y lo utiliza para ayudarle a comprender, organizar y ampliar su investigación.

---

## 13. Definición resumida del producto

> Fragmind es un workspace de investigación asistido por IA que permite guardar fragmentos relevantes de la web, organizarlos por tema y convertirlos en conocimiento útil mediante resúmenes, glosarios, ideas principales, etiquetas sugeridas y sugerencias para continuar investigando.

---

## 14. Frase corta del producto

> Guarda lo importante. Organiza tu investigación. Deja que la IA te ayude a entender y profundizar.

---