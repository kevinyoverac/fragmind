# Documentación — Fragmind

Este directorio contiene la documentación del proyecto Fragmind, organizada por dominio.

## Estructura

```
docs/
├── product/                        Visión, alcance y comportamiento funcional
│   ├── product-brief.md            Visión, problema, solución, MVP y diferenciación
│   ├── functional-specification.md Casos de uso, reglas de negocio, estados y errores
│   └── future-evolution-notes.md   Backlog estratégico de versiones futuras
│
├── technical/                      Diseño técnico del sistema
│   ├── architecture.md             Arquitectura del sistema, componentes y stack
│   ├── non-functional-requirements.md  Rendimiento, límites, accesibilidad, seguridad
│   ├── domain-models.md            Modelos de dominio, agregados y modelo relacional
│   └── api-spec.md                 Endpoints, convenciones y formatos de respuesta
│
├── operations/                     Seguridad, despliegue y operación
│   ├── deployment.md                Proveedores, entornos, CI/CD, backups, monitorización
│   └── security-and-privacy.md     GDPR, datos al proveedor IA, autenticación, incidentes
│
└── decisions/                      Registros de decisiones técnicas (ADRs)
    ├── README.md                    Convenciones e índice de ADRs
    ├── adr-template.md              Plantilla reutilizable para nuevos ADRs
    └── adr-NNN-*.md                 ADRs numerados secuencialmente
```

## Documentos pendientes de generar

- Wireframes o mockups de las pantallas principales (trabajo de diseño visual, mejor en Figma o Excalidraw que en markdown)

## Convenciones

- Cada documento es autocontenido pero puede referenciar a otros.
- El detalle canónico de cada concepto vive en un único documento. Por ejemplo, los metadatos de un fragmento se definen en `domain-models.md`, no en `product-brief.md`.
- Los cambios de alcance o decisiones que afecten al MVP se reflejan en `product-brief.md` (sección 10).
