# ADR-010: Polling con backoff para notificación de operaciones IA

- **Estado**: Aceptado
- **Fecha**: 2026-05-28
- **Decisores**: Equipo Fragmind
- **Etiquetas**: backend, frontend, ia, rendimiento

## Contexto

ADR-002 estableció que las operaciones IA son asíncronas. El cliente lanza una operación, recibe un ID y necesita conocer cuándo ha terminado. Hay que elegir el mecanismo concreto de notificación.

Restricciones:

- Volumen estimado bajo: el usuario lanza pocas operaciones IA simultáneas (típicamente una a la vez).
- Duración típica de una operación IA: 5-30 segundos.
- El equipo es pequeño y prefiere infraestructura mínima en V1.

## Alternativas consideradas

### Opción A: Polling con backoff

El cliente consulta periódicamente el endpoint `GET /ai-operations/{id}` con intervalos crecientes (ej: 2s, 3s, 5s, 8s, hasta 10s).

- Ventajas:
    - Sin infraestructura adicional: solo HTTP.
    - Implementación trivial en cualquier cliente.
    - Sin estado de conexión que mantener en el backend.
    - Compatible con cualquier proxy, firewall o configuración de red.
- Desventajas:
    - Carga adicional en el backend por las peticiones de polling.
    - Latencia perceptible entre que termina la operación y el cliente la detecta (hasta 10s en el peor caso).

### Opción B: WebSocket (STOMP sobre SockJS)

Conexión persistente entre cliente y servidor para empujar eventos.

- Ventajas:
    - Latencia mínima: el cliente se entera al instante.
    - Eficiente en uso de red para operaciones largas.
    - Habilita en el futuro otras notificaciones en tiempo real.
- Desventajas:
    - Más infraestructura: gestión de sesiones, reconexiones, balanceo con sticky sessions o adaptador.
    - Mayor complejidad en cliente (gestión de reconexión, mensajes perdidos).
    - El ALB necesita configuración específica para WebSocket.

### Opción C: Server-Sent Events (SSE)

Conexión HTTP de larga duración del servidor al cliente.

- Ventajas:
    - Más simple que WebSocket.
    - Soporte nativo en navegadores.
- Desventajas:
    - Aún más infraestructura que polling.
    - Problemas en algunos proxies corporativos.
    - No soporta enviar datos del cliente al servidor por la misma conexión.

## Decisión

Se elige **polling con backoff** para V1.

Estrategia de polling sugerida en cliente:

- Primer poll a los 2 segundos.
- Intervalos crecientes: 2s, 3s, 5s, 8s, 10s, 10s, 10s...
- Máximo de polls antes de mostrar fallback al usuario: configurable, sugerido 30 polls (~5 minutos).
- Si la pestaña no está visible, espaciar más el polling para no consumir batería ni cuota innecesaria.

La migración a WebSocket o SSE está contemplada como evolución futura cuando:

- El producto incorpore otras notificaciones en tiempo real (colaboración, comentarios en tiempo real...).
- La latencia percibida en operaciones IA se vuelva problemática.
- La carga de polling deje de ser despreciable.

## Consecuencias

### Positivas

- Implementación inmediata sin infraestructura extra.
- Cero riesgo de complicaciones por proxies, firewalls o configuración de ALB.
- Más fácil de depurar (cada poll es una petición HTTP normal con su log).

### Negativas

- Latencia adicional de hasta 10 segundos entre fin de operación y detección.
- Carga incremental en el backend (mitigada porque el endpoint de estado es muy ligero: consulta una fila por ID).

### Riesgos y mitigaciones

- **Polling agresivo desde muchos clientes**: rate limit por usuario en el endpoint de estado (ya contemplado en `non-functional-requirements.md`, sección 4.2).
- **Caché**: la respuesta del endpoint de estado puede llevar `Cache-Control: no-cache` para evitar respuestas obsoletas de intermediarios.

## Referencias

- ADR-002 (asincronía de operaciones IA).
- `technical/architecture.md`, sección 3.7.
- `technical/api-spec.md`, sección 14.3 (endpoint de consulta de estado).
- `product/future-evolution-notes.md` (migración futura a tiempo real).
