# ADR-009: AWS SQS como cola de trabajos IA

- **Estado**: Aceptado
- **Fecha**: 2026-05-28
- **Decisores**: Equipo Fragmind
- **Etiquetas**: infraestructura, ia, cola

## Contexto

ADR-002 estableció que las operaciones de IA se procesan de forma asíncrona mediante una cola de trabajos. Queda por decidir qué tecnología concreta se usa para esa cola.

Restricciones:

- AWS ya está elegido como proveedor cloud (ADR-003).
- Volumen estimado bajo en V1: decenas de operaciones IA por día.
- No se necesitan features avanzadas como routing complejo, prioridades estrictas u orden FIFO global.
- El equipo es pequeño y no quiere gestionar infraestructura adicional si puede evitarlo.

## Alternativas consideradas

### Opción A: AWS SQS

Servicio de colas gestionado por AWS.

- Ventajas:
    - Totalmente gestionado: sin servidores que mantener.
    - Integración nativa con IAM, CloudWatch y Fargate.
    - Pago por uso, free tier de 1 millón de peticiones/mes.
    - Garantías de entrega "at least once" suficientes para esta carga.
    - Dead letter queues nativas para mensajes fallidos.
- Desventajas:
    - Solo disponible en AWS (vendor lock-in parcial).
    - No soporta patrones avanzados de routing (publish/subscribe complejo, exchanges).

### Opción B: RabbitMQ

Broker de mensajes open source con más features.

- Ventajas:
    - Routing avanzado, exchanges, prioridades.
    - Portable entre clouds.
- Desventajas:
    - Hay que gestionar la instancia (o pagar CloudAMQP, ~25-50€/mes).
    - Más complejidad operativa para features que no se necesitan en V1.

### Opción C: Redis Streams

Cola sobre Redis.

- Ventajas: muy rápido, simple.
- Desventajas:
    - Requeriría desplegar Redis solo para esto.
    - Menos garantías de durabilidad que SQS o RabbitMQ.

## Decisión

Se elige **AWS SQS** como tecnología de cola.

Configuración inicial:

- Una cola standard `fragmind-ai-operations` para los trabajos IA.
- Una dead letter queue asociada para mensajes que fallan tras N reintentos (N = 3 por defecto).
- Visibility timeout configurado en función del timeout máximo de las operaciones IA (orientativo: 120 segundos).
- Retención de mensajes: 4 días (suficiente para detectar y reaccionar a backlogs).

## Consecuencias

### Positivas

- Cero gestión operativa de la cola.
- Coste prácticamente nulo a la escala V1 (dentro del free tier).
- Integración trivial con Spring Boot vía AWS SDK o Spring Cloud AWS.
- Dead letter queue facilita la depuración de mensajes problemáticos.

### Negativas

- Acoplamiento adicional a AWS (mitigado encapsulando la cola tras una abstracción en el backend, igual que con el adaptador `AiProvider`).
- No soporta prioridades nativas. Si en el futuro se necesitan operaciones IA prioritarias para usuarios premium, habrá que rediseñar o crear colas separadas.

### Riesgos y mitigaciones

- **Cambio de proveedor cloud**: aceptado como coste asumible si llega el caso, dado que la lógica de encolar/desencolar está aislada.
- **Mensajes huérfanos en la cola**: dead letter queue + alerta sobre profundidad de la DLQ.

## Referencias

- ADR-002 (decisión de asincronía de operaciones IA).
- ADR-003 (AWS como proveedor cloud).
- `operations/deployment.md`, sección 3.1.
