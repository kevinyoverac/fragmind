# ADR-013: Resend como proveedor de email transaccional

- **Estado**: Aceptado
- **Fecha**: 2026-05-28
- **Decisores**: Equipo Fragmind
- **Etiquetas**: infraestructura, email

## Contexto

Fragmind necesita enviar correos transaccionales: recuperación de contraseña, notificaciones del sistema y, en el futuro, verificación de email u otros mensajes operativos. Es necesario elegir un proveedor.

Restricciones:

- Volumen estimado bajo en V1: decenas a cientos de emails al mes.
- Necesidad de buena entregabilidad (los emails de recuperación no pueden caer en spam).
- Preferencia por proveedores con DX cuidada y configuración rápida.

## Alternativas consideradas

### Opción A: Resend

Proveedor moderno de email transaccional con API limpia.

- Ventajas:
    - **Free tier de 3.000 emails/mes**, más que suficiente para V1.
    - API HTTP simple, SDK para Java disponible.
    - Configuración de dominio guiada y rápida.
    - DX muy cuidada (dashboard, logs, webhooks).
    - Buena entregabilidad por defecto.
- Desventajas:
    - Proveedor relativamente reciente (lanzado en 2023).
    - Catálogo de features menor que SendGrid.

### Opción B: AWS SES

Servicio de email de AWS.

- Ventajas:
    - Muy barato (0,10 $ por cada 1000 emails).
    - Mismo proveedor que el resto de infraestructura.
    - Escala sin problemas.
- Desventajas:
    - Configuración tediosa: verificación de dominio, salir de sandbox manualmente, configurar DKIM/SPF.
    - Dashboard menos amigable que Resend.
    - Requiere más trabajo inicial para garantizar entregabilidad correcta.

### Opción C: SendGrid

Proveedor histórico, muy maduro.

- Ventajas: catálogo amplio de features, robusto.
- Desventajas: UX y DX se han degradado en los últimos años, free tier limitado (100 emails/día).

## Decisión

Se elige **Resend** para V1.

Configuración inicial:

- Dominio verificado: `fragmind.app` o subdominio dedicado para email transaccional.
- DKIM, SPF y DMARC configurados según la guía de Resend.
- Plantillas de email gestionadas en el código del backend (no en el panel de Resend) para tener versionado.
- API key gestionada en AWS Secrets Manager.

La migración a AWS SES se evaluará si:

- El volumen crece y el coste justifica el cambio.
- Aparecen necesidades de configuración avanzada no soportadas por Resend.
- Se quiere reducir el número de proveedores externos.

## Consecuencias

### Positivas

- Coste cero en V1.
- Setup rápido (horas en lugar de días).
- DX moderna acorde al resto del stack.

### Negativas

- Proveedor adicional fuera de AWS: un panel y una factura más.
- Dependencia de un proveedor relativamente joven.

### Riesgos y mitigaciones

- **Caída de Resend**: los emails transaccionales no son críticos en tiempo real (recuperación de contraseña tolera reintentos). Mitigación: cola interna con reintentos si Resend devuelve error.
- **Cambio de política de Resend**: la abstracción en el código (`EmailProvider`) permite cambiar de proveedor con esfuerzo limitado, similar al `AiProvider` del ADR-001.
- **DPA**: firmado con Resend antes de producción (ver `security-and-privacy.md`, sección 3).

## Referencias

- `operations/deployment.md`, sección 2 (proveedores).
- `operations/security-and-privacy.md`, sección 3 (encargados del tratamiento).
