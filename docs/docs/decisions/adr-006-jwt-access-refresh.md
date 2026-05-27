# ADR-006: JWT con access + refresh token para autenticación

- **Estado**: Aceptado
- **Fecha**: 2026-05-28
- **Decisores**: Equipo Fragmind
- **Etiquetas**: backend, seguridad, autenticacion

## Contexto

Fragmind tiene tres clientes que necesitan autenticarse contra el backend: aplicación web (Next.js), extensión de navegador (Chrome y Firefox) y, en el futuro, posibles integraciones externas.

Hay que elegir un mecanismo de sesión que:

- Funcione en los tres tipos de cliente.
- Sea seguro frente a robo de credenciales.
- Permita expiración corta sin obligar al usuario a reautenticarse constantemente.
- Sea operacionalmente sencillo en V1.

## Alternativas consideradas

### Opción A: JWT con access + refresh token

Access token de corta vida (15-30 min) enviado en `Authorization: Bearer`. Refresh token de larga vida (7-30 días) usado para renovar el access token.

- Ventajas:
    - Funciona idéntico en web, extensión y futuras integraciones.
    - Stateless en el backend: no requiere consultar base de datos en cada petición.
    - Access tokens cortos limitan el daño si se filtran.
    - Refresh tokens revocables al hacer logout o cambio de contraseña.
- Desventajas:
    - El cliente debe gestionar el almacenamiento y la renovación de tokens.
    - Si los tokens se almacenan en `localStorage`, son vulnerables a XSS.

### Opción B: Cookie HTTP-only de sesión

Sesión gestionada por el servidor, almacenada en una cookie `HttpOnly` y `Secure`.

- Ventajas:
    - Inmune a XSS (el JavaScript no puede leer la cookie).
    - Más simple para el cliente: el navegador la gestiona automáticamente.
    - Revocación trivial (basta con invalidar la sesión en el servidor).
- Desventajas:
    - Requiere protección CSRF explícita.
    - Más complicado para la extensión, que opera en un contexto distinto al de la aplicación web.
    - Requiere session store en el backend (Redis o BBDD).
    - Más complicado para integraciones externas futuras.

### Opción C: OAuth / OIDC contra proveedor externo

Delegar autenticación a un proveedor (Auth0, Clerk, Cognito).

- Ventajas: menos código propio de autenticación, MFA, social login, recuperación gestionada.
- Desventajas: coste adicional, dependencia de proveedor, lock-in, complicación en V1.

## Decisión

Se elige la **Opción A**: JWT con access token de corta vida y refresh token de larga vida.

Detalles:

- **Access token**: JWT firmado, vida útil 15-30 minutos.
- **Refresh token**: token opaco con registro en BBDD (no JWT), vida útil 7-30 días, revocable.
- Almacenamiento en cliente web: por decidir entre `localStorage` y memoria + cookie de refresh. Se evaluará en la implementación.
- En la extensión: storage propio de la extensión, no compartido con la web.
- Invalidación: logout, cambio de contraseña y eliminación de cuenta invalidan todos los refresh tokens activos del usuario.

La migración a cookie HTTP-only está contemplada como evolución futura (ver `product/future-evolution-notes.md`, sección 17) si el modelo de uso evoluciona y la web pasa a dominar frente a la extensión.

## Consecuencias

### Positivas

- Mismo mecanismo en web y extensión.
- Sin session store en V1 (los refresh tokens en BBDD son una tabla pequeña).
- Operacionalmente simple.
- Fácil de implementar con Spring Security.

### Negativas

- Riesgo de XSS si el access token se almacena en `localStorage`. Mitigación: CSP estricta, no usar `dangerouslySetInnerHTML` con contenido de usuario, vida corta del access token.
- La revocación inmediata del access token es imposible sin lista negra (asumible dado su vida corta).

### Riesgos y mitigaciones

- **Robo de access token vía XSS**: vida útil corta (15-30 min) limita la ventana. CSP estricta y sanitización rigurosa (ver `operations/security-and-privacy.md`, sección 10).
- **Robo de refresh token**: rotación obligatoria en cada uso (un refresh token solo se usa una vez; al usarse, se emite uno nuevo y el anterior queda invalidado). Detección de uso doble del mismo refresh token = todas las sesiones revocadas.

## Referencias

- `operations/security-and-privacy.md`, sección 6 (autenticación y sesiones).
- `technical/api-spec.md`, sección 8 (endpoints de autenticación).
- `product/future-evolution-notes.md`, sección 17 (cookie HTTP-only como evolución futura).
