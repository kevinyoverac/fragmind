# ADR-012: AWS CDK con TypeScript como infraestructura como código

- **Estado**: Aceptado
- **Fecha**: 2026-05-28
- **Decisores**: Equipo Fragmind
- **Etiquetas**: infraestructura, despliegue

## Contexto

Toda la infraestructura de Fragmind en AWS se gestiona como código (`deployment.md`, sección 11). Falta decidir la herramienta concreta.

Restricciones:

- AWS como único proveedor cloud por ahora (ADR-003).
- Equipo pequeño que ya usa TypeScript en frontend (Next.js) y extensión.
- Necesidad de reproducibilidad y revisión vía pull request.

## Alternativas consideradas

### Opción A: AWS CDK con TypeScript

Framework oficial de AWS para definir infraestructura usando lenguajes de programación. Compila a CloudFormation.

- Ventajas:
    - Consistencia con el resto del stack (TypeScript ya está en el equipo).
    - Constructs de alto nivel: definir un servicio Fargate con ALB es cuestión de líneas.
    - Tipado fuerte: errores detectados en compilación, autocompletado completo.
    - Soportado y mantenido directamente por AWS.
- Desventajas:
    - Lock-in a AWS. Si se cambia de proveedor cloud, hay que reescribir.
    - Estado de CloudFormation puede ser tedioso si hay errores.

### Opción B: Terraform

Herramienta open source de HashiCorp. Lenguaje propio (HCL).

- Ventajas:
    - Portable entre clouds (AWS, GCP, Azure, etc.).
    - Comunidad enorme, ecosistema de módulos enorme.
    - Estado independiente de CloudFormation.
- Desventajas:
    - Lenguaje HCL adicional para el equipo.
    - Sin tipado fuerte.
    - Los módulos de alto nivel comparables a los constructs de CDK suelen ser de terceros, no oficiales.

### Opción C: AWS SAM o Serverless Framework

Orientados a serverless (Lambda + API Gateway).

- Desventajas: el stack de Fragmind no es serverless en sentido estricto (Fargate + RDS), así que no encajan bien.

## Decisión

Se elige **AWS CDK con TypeScript**.

Convenciones:

- Repositorio separado `fragmind-infrastructure` o módulo dentro del repo de backend (por decidir al arrancar).
- Stacks separados por entorno: `FragmindDevStack`, `FragmindProdStack`.
- Constructs reutilizables para los servicios comunes (Fargate service con ALB, RDS con backup, etc.).
- Despliegue manual desde el equipo en V1; automatización vía GitHub Actions si crece la frecuencia.

## Consecuencias

### Positivas

- Una sola lengua (TypeScript) para frontend, extensión e infraestructura.
- Tipado fuerte previene muchos errores antes de aplicar cambios.
- Curva de aprendizaje suave si ya se sabe TypeScript.
- Constructs de AWS oficiales y actualizados rápido cuando hay servicios nuevos.

### Negativas

- Vendor lock-in a AWS asumido como aceptable (consistente con ADR-003).
- Si en el futuro se migra de cloud, la infraestructura se reescribe. Asumible.

### Riesgos y mitigaciones

- **Drift entre infraestructura real y código**: CDK detecta drift al hacer `cdk diff`. Norma: ningún cambio manual en consola de AWS en producción; los cambios pasan siempre por código y PR.
- **Coste accidental por errores en código**: revisión obligatoria de `cdk diff` antes de cada `cdk deploy` en producción.

## Referencias

- ADR-003 (AWS como proveedor cloud).
- `operations/deployment.md`, sección 11.
