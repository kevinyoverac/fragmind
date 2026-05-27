# ADR-011: Flyway como herramienta de migraciones de base de datos

- **Estado**: Aceptado
- **Fecha**: 2026-05-28
- **Decisores**: Equipo Fragmind
- **Etiquetas**: backend, base-de-datos

## Contexto

El backend de Fragmind necesita una herramienta para versionar y aplicar cambios al esquema de la base de datos PostgreSQL de forma controlada y reproducible. Las migraciones se aplican automáticamente al arrancar el contenedor del backend (`deployment.md`, sección 5.5).

## Alternativas consideradas

### Opción A: Flyway

- Ventajas:
    - Migraciones en SQL plano: legibles, revisables en PRs, fáciles de auditar.
    - Estándar de facto en el ecosistema Spring Boot.
    - Configuración mínima en Spring Boot (autoconfiguración).
    - Comunidad amplia, abundante documentación.
- Desventajas:
    - Menos potente que Liquibase para rollbacks complejos.
    - Las migraciones son SQL específico de cada motor (no portable entre PostgreSQL y otros), aunque no es problema dado que ADR-001 fija PostgreSQL.

### Opción B: Liquibase

- Ventajas:
    - Soporte para múltiples formatos (XML, YAML, JSON, SQL).
    - Rollbacks declarativos más sofisticados.
    - Mejor abstracción para entornos con múltiples motores de BBDD.
- Desventajas:
    - Más verboso (cambios típicamente en XML o YAML).
    - Curva de aprendizaje algo mayor.
    - Para SQL plano, Flyway es igual de capaz con menos ceremonia.

## Decisión

Se elige **Flyway**.

Convenciones:

- Migraciones ubicadas en `src/main/resources/db/migration/`.
- Nomenclatura: `V{version}__{descripcion}.sql` (ej: `V001__create_users_table.sql`).
- Las migraciones se aplican al arrancar el backend, antes de aceptar tráfico.
- Toda migración debe ser **compatible hacia atrás** durante al menos un despliegue para permitir rollback (ver `deployment.md`, sección 5.5).
- Cambios destructivos (drop column, rename) se hacen en dos pasos: migración aditiva + despliegue, luego migración destructiva en despliegue posterior.

## Consecuencias

### Positivas

- SQL plano: cualquiera con conocimiento de SQL revisa una migración sin aprender una DSL.
- Configuración trivial con Spring Boot.
- Buena trazabilidad: cada migración aplicada queda registrada en la tabla `flyway_schema_history`.

### Negativas

- Los rollbacks no se ejecutan automáticamente; hay que escribir la migración inversa manualmente si se necesita.
- Las migraciones SQL aquí no son portables a otros motores (no es problema mientras ADR-001 siga vigente).

### Riesgos y mitigaciones

- **Migración rota en producción**: la migración se aplica antes de aceptar tráfico; si falla, el contenedor no arranca y el despliegue queda bloqueado. El despliegue progresivo deja la versión anterior atendiendo tráfico mientras tanto.
- **Modificar migraciones ya aplicadas**: prohibido. Si una migración tiene un error, se crea una nueva migración correctiva con número posterior.

## Referencias

- ADR-001 (PostgreSQL como base de datos).
- `operations/deployment.md`, sección 5.5 (migraciones en el pipeline).
- `technical/architecture.md`, sección 5 (tabla resumen del stack).
