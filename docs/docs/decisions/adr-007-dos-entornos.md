# ADR-007: Dos entornos (dev + prod) en V1

- **Estado**: Aceptado
- **Fecha**: 2026-05-28
- **Decisores**: Equipo Fragmind
- **Etiquetas**: infraestructura, despliegue, operaciones

## Contexto

Hay que decidir cuántos entornos desplegados mantiene Fragmind en V1. La elección afecta a complejidad operativa, coste y velocidad de iteración.

Opciones habituales:

- 2 entornos: `dev` + `prod`.
- 3 entornos: `dev` + `staging` + `prod`.
- 1 entorno: solo `prod` (todo local antes).

## Alternativas consideradas

### Opción A: dev + prod

- Ventajas:
    - Sencillo: menos infraestructura, menos coste, menos configuración duplicada.
    - Adecuado para una escala V1 (decenas-cientos de usuarios) sin testers ni proceso QA formal.
- Desventajas:
    - No hay entorno seguro para probar cambios cerca de producción antes de promocionarlos.

### Opción B: dev + staging + prod

- Ventajas:
    - Permite validar cambios en un entorno idéntico a producción antes de desplegar.
    - Útil si hay testers externos o demos previas.
- Desventajas:
    - Triplica la configuración y aproximadamente duplica el coste.
    - Mantenimiento adicional: tres bases de datos, tres conjuntos de variables, tres pipelines.
    - Sobredimensionado para un equipo pequeño en V1.

### Opción C: Solo prod

- Ventajas: coste mínimo.
- Desventajas: cualquier prueba real afecta a usuarios. No es viable para un producto público.

## Decisión

Se eligen **dos entornos: `dev` + `prod`**.

`dev` actúa como entorno integrado de pruebas: cualquier cambio mergeado a `main` se despliega ahí automáticamente. Es el entorno donde el equipo valida que un cambio funciona en infraestructura real antes de crear un tag de release y promocionarlo a `prod`.

Cuando el producto crezca y se incorporen testers regulares o usuarios beta, se reevaluará añadir `staging` como entorno intermedio entre `dev` y `prod`.

## Consecuencias

### Positivas

- Operación sencilla y barata.
- Pipeline CI/CD lineal y comprensible.
- Bajo coste de infraestructura en los primeros meses.

### Negativas

- Cambios que requieren validación cerca de producción se prueban directamente en `dev`, que no es idéntico (puede tener datos sintéticos, cuotas mayores, etc.).
- Si hay un cambio arriesgado, hay que asumir que la primera prueba "real" ocurre en `prod`.

### Riesgos y mitigaciones

- **Cambios destructivos no detectados antes de producción**: mitigado con tests automáticos exhaustivos en CI, despliegues progresivos con health checks y posibilidad de rollback inmediato (ver `operations/deployment.md`, sección 10).
- **Migraciones de BBDD**: regla del compatible hacia atrás (`deployment.md`, sección 5.5) para que el rollback sea siempre posible.

## Referencias

- `operations/deployment.md`, sección 4 (entornos).
