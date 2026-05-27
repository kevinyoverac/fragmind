# ADR-002: Operaciones IA asíncronas con cola de trabajos

- **Estado**: Aceptado
- **Fecha**: 2026-05-28
- **Decisores**: Equipo Fragmind
- **Etiquetas**: arquitectura, ia, rendimiento

## Contexto

Las operaciones de IA en Fragmind (resúmenes de workspace, glosarios, ideas principales, sugerencias de etiquetas, sugerencias de búsqueda) implican llamadas al proveedor IA externo (OpenAI). Estas llamadas tienen latencias variables que pueden ir de segundos a decenas de segundos, dependiendo del modelo, el tamaño del prompt y la carga del proveedor.

Hay dos enfoques posibles para gestionar estas operaciones:

1. **Síncrono**: el cliente lanza la operación y espera la respuesta HTTP completa.
2. **Asíncrono**: el cliente lanza la operación, recibe un ID, y consulta el estado posteriormente.

## Alternativas consideradas

### Opción A: Operaciones síncronas

El cliente hace `POST /ai-operations` y espera la respuesta con el resultado completo.

- Ventajas:
    - Implementación más simple en backend y cliente.
    - No requiere cola ni workers.
- Desventajas:
    - Conexiones HTTP abiertas durante decenas de segundos, mala UX en redes inestables.
    - Difícil manejar timeouts del proveedor IA sin perder trabajo.
    - Reintentos complicados: si el cliente reintenta, se duplica la operación y el coste.
    - Bloquea recursos del backend mientras espera.
    - Mal encaje con limitaciones de rate limit del proveedor IA.

### Opción B: Operaciones asíncronas con cola

El cliente hace `POST /ai-operations`, recibe `{ id, status: PENDING }`, y consulta el estado por polling o recibe notificación vía WebSocket. Un worker independiente procesa la cola.

- Ventajas:
    - Buena UX: el cliente puede mostrar progreso o seguir navegando.
    - Reintentos seguros desde el worker, con backoff.
    - Control centralizado del rate limit hacia OpenAI.
    - El backend síncrono no se acopla a la latencia del proveedor IA.
    - Permite cancelar operaciones en curso.
- Desventajas:
    - Más complejidad: cola, workers, estados, notificaciones.
    - Requiere infraestructura adicional (SQS, RabbitMQ o similar).
    - El cliente necesita gestionar estados intermedios.

## Decisión

Se elige la **Opción B: operaciones asíncronas con cola**.

Flujo:

1. Cliente: `POST /ai-operations` → backend crea operación en estado `PENDING`, encola, devuelve ID.
2. Worker consume el mensaje, marca como `RUNNING`, invoca OpenAI, persiste resultado, marca como `COMPLETED` o `FAILED`.
3. Cliente consulta estado por polling o recibe notificación.

## Consecuencias

### Positivas

- Resiliencia frente a fallos del proveedor IA (reintentos automatizados).
- Control unificado de cuotas y rate limits.
- Mejor experiencia de usuario con feedback de progreso.
- El backend síncrono permanece rápido y predecible.

### Negativas

- Mayor complejidad inicial: encoladores, consumidores, gestión de estados.
- Se necesita una infraestructura adicional (cola).
- El cliente necesita implementar polling o WebSocket.

### Riesgos y mitigaciones

- **Cola se bloquea**: alerta de profundidad de cola en monitorización (`deployment.md`, sección 7.3).
- **Operaciones huérfanas en `RUNNING` indefinido**: timeout configurable por tipo de operación; si se supera, se marca como `FAILED`.

## Referencias

- `technical/architecture.md`, sección 3.5 (cola y workers IA).
- `technical/architecture.md`, sección 4.2 (flujo de operación IA asíncrona).
- `technical/api-spec.md`, sección 14 (endpoints de operaciones IA).
- ADR-009 (tecnología concreta de cola: AWS SQS).
- ADR-010 (mecanismo concreto de notificación: polling con backoff).
