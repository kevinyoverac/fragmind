# ADR-001: Stack tecnológico principal

- **Estado**: Aceptado
- **Fecha**: 2026-05-28
- **Decisores**: Equipo Fragmind
- **Etiquetas**: arquitectura, backend, frontend, base-de-datos, ia

## Contexto

Fragmind necesita definir un stack tecnológico para su V1: backend que aplique reglas de negocio y exponga API, frontend web para la interfaz principal, base de datos para persistir el dominio (usuarios, workspaces, fragmentos, etiquetas, operaciones IA) y un proveedor de IA para las funcionalidades de resumen, glosario, ideas principales y sugerencias.

Restricciones relevantes:

- Equipo pequeño, MVP orientado a validación con decenas-cientos de usuarios.
- Dominio relacional con varios agregados, reglas de negocio, estados y transacciones (ver `domain-models.md`).
- Operaciones de IA contra un proveedor externo, con costes que deben controlarse.
- Operación desde la UE, con obligaciones GDPR.

## Alternativas consideradas

### Backend

- **Java + Spring Boot**: framework maduro, excelente soporte para JPA, seguridad, validación y testing. Java 21 ofrece virtual threads, útiles para muchas conexiones concurrentes.
- **Node.js + NestJS / Express**: ecosistema unificado con el frontend, despliegue ligero, pero menos solidez para dominios ricos.
- **Python + FastAPI / Django**: rápido para iterar; menos cómodo para tipado estricto y transacciones complejas en V1.

### Frontend

- **Next.js (React)**: combina SSR (útil para landing y SEO) con cliente React. API routes permiten una capa fina de BFF sin reescribir.
- **Vue / Nuxt**: alternativa válida, menos popular en el mercado laboral local.
- **Angular**: más opinado y pesado; encaja peor en una SPA ligera.

### Base de datos

- **PostgreSQL**: relacional, ACID, integridad referencial, búsqueda full-text nativa suficiente para V1.
- **MySQL / MariaDB**: válido, pero PostgreSQL tiene mejor soporte de tipos y full-text.
- **MongoDB**: el dominio no es naturalmente documental; las relaciones entre fragmentos, workspaces y etiquetas se modelan mejor relacionalmente.

### Proveedor de IA

- **OpenAI API**: madurez de API, modelos con buena relación coste/calidad, documentación amplia.
- **Anthropic (Claude)**: alternativa fuerte, válida también.
- **Modelo europeo (Mistral)**: ventaja en cumplimiento GDPR (no requiere SCCs), pero menos opciones de modelos para V1.

## Decisión

Stack elegido:

- **Backend**: Java 21 + Spring Boot 3.x.
- **Frontend**: Next.js 15+ con React y TypeScript.
- **Base de datos**: PostgreSQL 16+.
- **Proveedor IA**: OpenAI API.

## Consecuencias

### Positivas

- Stack maduro con comunidad amplia y abundante documentación.
- Java + Spring encajan bien con el dominio relacional rico que tiene Fragmind.
- Next.js permite renderizado del lado servidor cuando se necesite (landing, futuras páginas SEO).
- PostgreSQL cubre persistencia, búsqueda full-text y integridad referencial sin añadir más piezas.
- OpenAI permite arrancar rápido sin construir infraestructura propia de IA.

### Negativas

- Java es más verboso y lento de iterar que Node o Python.
- Mezcla de lenguajes en el equipo (Java en backend, TypeScript en frontend y extensión) implica conocer dos ecosistemas.
- Dependencia de un proveedor IA externo no europeo (mitigada por ADR-004).

### Riesgos y mitigaciones

- **Cambio de proveedor IA**: mitigado encapsulando el acceso a OpenAI en un adaptador `AiProvider` (ver `architecture.md`, sección 3.6).
- **Coste de OpenAI imprevisible**: mitigado con cuotas estrictas (`non-functional-requirements.md`, sección 4.2) y alertas de coste (`deployment.md`, sección 7.3).

## Referencias

- `technical/architecture.md` (visión general del sistema).
- `technical/domain-models.md` (modelo de dominio relacional).
- ADR-004 (transferencia de datos a EEUU con OpenAI).
