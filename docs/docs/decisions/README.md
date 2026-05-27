# Decisiones técnicas (ADRs) — Fragmind

Esta carpeta contiene los **Architecture Decision Records (ADRs)** del proyecto.

## ¿Qué es un ADR?

Un ADR es un documento corto que captura una decisión técnica importante, su contexto y sus consecuencias. Sirve para que cualquier persona que llegue al proyecto entienda **por qué** las cosas son como son, no solo cómo son.

## ¿Cuándo crear un ADR?

Crea un ADR cuando una decisión cumpla alguna de estas condiciones:

- Tiene alternativas reales que merecen ser comparadas.
- Es difícil o costosa de revertir.
- Sorprendería a alguien que llegue nuevo al proyecto.
- Afecta a varios componentes del sistema.

**No** crees un ADR para decisiones triviales o reversibles sin coste (nombres de variables, librerías intercambiables, estilo de código).

## Convenciones

- **Numeración**: los ADRs se numeran secuencialmente con tres dígitos (`adr-001`, `adr-002`...). El número no se reutiliza ni se renumera aunque el ADR se marque como obsoleto.
- **Nombre de archivo**: `adr-NNN-titulo-corto-en-kebab-case.md`.
- **Estado**: cada ADR tiene un estado (`Propuesto`, `Aceptado`, `Obsoleto`, `Reemplazado por ADR-XXX`).
- **Inmutabilidad**: una vez `Aceptado`, un ADR no se modifica. Si la decisión cambia, se crea un nuevo ADR que lo reemplace y se actualiza el estado del antiguo.
- **Idioma**: español.

Usa `adr-template.md` como punto de partida para nuevos ADRs.

## Índice de ADRs

| Nº | Título | Estado |
|---|---|---|
| 001 | Stack tecnológico principal | Aceptado |
| 002 | Operaciones IA asíncronas con cola de trabajos | Aceptado |
| 003 | AWS como proveedor cloud y Fargate como modo de despliegue | Aceptado |
| 004 | Transferencia de datos a EEUU con OpenAI bajo SCCs | Aceptado |
| 005 | No enviar URLs ni dominios a OpenAI | Aceptado |
| 006 | JWT con access + refresh token para autenticación | Aceptado |
| 007 | Dos entornos (dev + prod) en V1 | Aceptado |
| 008 | Tres repositorios separados en lugar de monorepo | Aceptado |
| 009 | AWS SQS como cola de trabajos IA | Aceptado |
| 010 | Polling con backoff para notificación de operaciones IA | Aceptado |
| 011 | Flyway como herramienta de migraciones | Aceptado |
| 012 | AWS CDK con TypeScript como infraestructura como código | Aceptado |
| 013 | Resend como proveedor de email transaccional | Aceptado |
