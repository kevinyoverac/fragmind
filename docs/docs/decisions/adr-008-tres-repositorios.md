# ADR-008: Tres repositorios separados en lugar de monorepo

- **Estado**: Aceptado
- **Fecha**: 2026-05-28
- **Decisores**: Equipo Fragmind
- **Etiquetas**: organizacion, ci-cd

## Contexto

Fragmind tiene tres componentes desplegables independientes:

1. Backend (Java + Spring Boot).
2. Frontend web (Next.js + TypeScript).
3. Extensión de navegador (TypeScript, Manifest V3).

Hay que decidir si todo el código vive en un único repositorio (monorepo) o en repositorios separados.

## Alternativas consideradas

### Opción A: Monorepo

Un único repositorio con tres carpetas (`/backend`, `/web`, `/extension`).

- Ventajas:
    - Un solo lugar para buscar, clonar, configurar.
    - Cambios coordinados (ej. cambio de API que afecta a backend y frontend a la vez) viven en un mismo commit.
    - Configuración compartida más fácil (tipos comunes, prettier, etc.).
- Desventajas:
    - Las ramas y PRs se vuelven más confusas (cambios entremezclados).
    - El CI necesita detectar qué carpetas cambiaron para no ejecutar todo siempre, o asumir que se ejecuta todo siempre (lento).
    - La extensión tiene un ciclo de versionado y publicación distinto (Chrome Web Store, AMO) que se gestiona peor mezclado.
    - Stacks muy diferentes (Java vs TypeScript) conviven mal en un mismo repo en términos de tooling.

### Opción B: Tres repositorios separados

Repositorios independientes: `fragmind-backend`, `fragmind-web`, `fragmind-extension`.

- Ventajas:
    - Cada repo tiene su CI, su tooling, su `README.md` y su ciclo de despliegue independiente.
    - La extensión, sujeta a revisión por las stores, no mezcla su tag con releases de backend.
    - Pull requests centrados en un componente, más fáciles de revisar.
    - Permite repos privados/públicos selectivos si en el futuro se decide abrir alguno.
- Desventajas:
    - Cambios coordinados (cambio de contrato API) requieren PRs en varios repos.
    - Más configuración inicial (un README por repo, un set de actions por repo).

## Decisión

Se eligen **tres repositorios separados**: `fragmind-backend`, `fragmind-web` y `fragmind-extension`.

Los contratos compartidos (formato de respuestas de API, códigos de error) viven canónicamente en `api-spec.md` en el repo de backend, y se referencian desde los demás.

## Consecuencias

### Positivas

- Cada componente tiene un ciclo de vida claro y autónomo.
- Despliegues independientes: el backend puede desplegar 5 veces al día sin afectar a frontend o extensión.
- Tooling especializado en cada repo (Maven/Gradle en backend, npm/pnpm en frontend, bundlers de extensión).
- La revisión de PRs es más enfocada.

### Negativas

- Cambios que cruzan repos requieren coordinación manual: hacer PR en backend con la nueva API, mergear, después hacer PR en frontend que la consuma.
- Riesgo de desincronización temporal entre versiones de backend y clientes durante un despliegue.

### Riesgos y mitigaciones

- **API rompedora desplegada sin que los clientes estén listos**: mitigado con la regla de versionado de la API y compatibilidad hacia atrás durante al menos un despliegue. La regla equivalente para BBDD está documentada en `deployment.md`, sección 5.5.
- **Configuración duplicada (linting, prettier, etc.)**: aceptable. Si crece, se evalúa un repo `fragmind-shared-config`.

## Referencias

- `operations/deployment.md`, sección 5 (pipelines CI/CD por componente).
- `technical/api-spec.md` (contrato canónico de la API).
