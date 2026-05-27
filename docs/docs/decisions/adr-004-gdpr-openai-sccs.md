# ADR-004: Transferencia de datos a EEUU con OpenAI bajo SCCs

- **Estado**: Aceptado
- **Fecha**: 2026-05-28
- **Decisores**: Equipo Fragmind
- **Etiquetas**: gdpr, privacidad, ia, legal

## Contexto

Fragmind opera desde la UE y trata datos personales de usuarios europeos, por lo que está sujeto a GDPR. La aplicación utiliza OpenAI (proveedor estadounidense) para procesar el contenido de fragmentos en operaciones de IA. Esto implica transferencia internacional de datos personales fuera del Espacio Económico Europeo.

El GDPR (artículos 44-49) exige garantías específicas para transferencias internacionales. Las opciones legales son:

1. País con decisión de adecuación (EEUU lo tiene desde julio 2023 vía el EU-US Data Privacy Framework).
2. Garantías adecuadas: cláusulas contractuales tipo (SCCs), reglas corporativas vinculantes (BCRs).
3. Excepciones específicas del artículo 49.

Tras la sentencia Schrems II (2020) y la entrada en vigor del Data Privacy Framework (2023), el marco legal ha sido cambiante. Las startups europeas que usan IA mayoritariamente operan bajo SCCs + DPF.

## Alternativas consideradas

### Opción A: Postura pragmática — OpenAI bajo SCCs + DPF

Usar OpenAI con DPA firmado, opt-out de entrenamiento activado y amparo en SCCs y el Data Privacy Framework.

- Ventajas:
    - Acceso inmediato a modelos maduros con buena relación coste/calidad.
    - Marco legal aceptado por la mayoría de empresas europeas.
    - Postura defendible ante AEPD si se documenta correctamente.
- Desventajas:
    - Dependencia de la estabilidad del DPF (puede ser invalidado en el futuro como pasó con Privacy Shield).
    - Algunos usuarios sensibles a privacidad pueden rechazar usar la herramienta.

### Opción B: Postura estricta — proveedor europeo

Usar Mistral (Francia) o Azure OpenAI con despliegue garantizado en región UE.

- Ventajas:
    - Sin transferencia internacional, marco legal mucho más simple.
    - Ventaja competitiva frente a usuarios sensibles a privacidad.
- Desventajas:
    - Menos modelos disponibles, modelos generalmente menos potentes.
    - Coste similar o mayor.
    - Mayor complejidad de integración.
    - Limita la velocidad de iteración en V1.

### Opción C: Sin IA externa, modelos locales

Desplegar modelos open source en infraestructura propia.

- Ventajas: control total, sin transferencia.
- Desventajas: coste de infraestructura GPU, complejidad operativa fuera del alcance de un equipo pequeño en V1.

## Decisión

Se elige la **Opción A: postura pragmática** con las siguientes garantías obligatorias:

- **DPA firmado** con OpenAI antes de poner el sistema en producción.
- Uso de la **API empresarial** de OpenAI (no ChatGPT consumer).
- **Opt-out de entrenamiento** activado.
- **Retención en OpenAI configurada al mínimo** permitido por el plan contratado.
- **Amparo legal** explícito en las SCCs y el EU-US Data Privacy Framework, documentado en la política de privacidad.
- **No se envían a OpenAI datos personales identificables del usuario** (email, nombre, ID): solo contenido que el usuario ha guardado conscientemente (ver ADR-005).
- **Información transparente al usuario** sobre qué se envía y por qué, antes de la primera operación IA.

## Consecuencias

### Positivas

- Permite arrancar V1 sin barreras técnicas de integración.
- Marco legal estándar y defendible.
- No condiciona la calidad del producto desde el primer día.

### Negativas

- Si el DPF es invalidado por una futura Schrems III, hay que migrar a otra solución con cierta urgencia.
- Algunos potenciales usuarios sensibles a privacidad pueden no usar el producto.

### Riesgos y mitigaciones

- **Invalidación del DPF**: mitigado encapsulando el acceso a OpenAI en un adaptador `AiProvider` (ver ADR-001 y `architecture.md` sección 3.6), lo que permite cambiar de proveedor sin tocar la lógica de negocio. Se contempla Azure OpenAI región UE o Mistral como alternativas viables.
- **Cambios de política de OpenAI**: revisión anual de DPA y condiciones.
- **Tipo de cliente futuro**: si el producto evoluciona hacia clientes empresariales o sectores regulados (sanidad, legal), esta decisión se revisa.

## Referencias

- `operations/security-and-privacy.md`, sección 5 (política de envío de datos al proveedor IA).
- `technical/non-functional-requirements.md`, sección 9.2 (transferencia internacional).
- ADR-001 (stack tecnológico, elección de OpenAI).
- ADR-005 (qué datos exactamente se envían a OpenAI).
- GDPR artículos 44-49.
- [EU-US Data Privacy Framework](https://commission.europa.eu/document/fa09cbad-dd7d-4684-ae60-be03fcb0fddf_en).
