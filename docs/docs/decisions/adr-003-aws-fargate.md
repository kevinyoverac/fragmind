# ADR-003: AWS como proveedor cloud y Fargate como modo de despliegue

- **Estado**: Aceptado
- **Fecha**: 2026-05-28
- **Decisores**: Equipo Fragmind
- **Etiquetas**: infraestructura, despliegue, cloud

## Contexto

Fragmind necesita un proveedor cloud para alojar el backend, la base de datos, la cola de trabajos IA y la infraestructura auxiliar. Hay que decidir también el modo concreto de despliegue del backend (contenedores gestionados, Kubernetes o VMs).

Restricciones:

- Operación desde la UE (necesidad de región europea para GDPR).
- Equipo pequeño sin dedicación a operaciones a tiempo completo.
- Escala V1 modesta (decenas-cientos de usuarios).
- Coste contenido en los primeros meses.

## Alternativas consideradas

### Proveedor cloud

- **AWS**: el más maduro, mejor documentación, mayor comunidad. Tiene regiones UE (Frankfurt, Madrid). Free tier amplio durante 12 meses.
- **GCP**: muy bueno para serverless (Cloud Run encaja muy bien con Spring Boot). Menos cuota de mercado local.
- **Azure**: fuerte si el cliente o equipo ya está en el ecosistema Microsoft, no es el caso aquí.

### Modo de despliegue del backend

- **Contenedores gestionados (Fargate sobre ECS)**: contenedores serverless. No se gestionan nodos. Escalado y parches del host gestionados por AWS.
- **Kubernetes (EKS)**: mucho control y portabilidad, pero gran complejidad operativa.
- **Máquinas virtuales (EC2)**: barato en papel, pero exige gestionar actualizaciones, parches y despliegues zero-downtime manualmente.

## Decisión

- Proveedor cloud: **AWS**, región **`eu-central-1` (Frankfurt)**.
- Modo de despliegue del backend y workers IA: **ECS Fargate** (contenedores serverless).
- Frontend (Next.js): **Vercel**, no AWS, por la integración nativa con Next.js y la simplicidad operativa.

## Consecuencias

### Positivas

- AWS aporta madurez, documentación y comunidad amplias; las dudas operativas suelen estar resueltas en StackOverflow o documentación oficial.
- Frankfurt cumple requisitos GDPR sin esfuerzo adicional.
- Fargate elimina toda la gestión de nodos: AWS se encarga de parches del host, escalado y disponibilidad.
- Vercel gestiona el frontend con esfuerzo operativo mínimo y aporta CDN global incluido.
- El stack es suficientemente estándar como para que sea fácil encontrar perfiles que lo entiendan.

### Negativas

- Dispersión de proveedores: AWS para backend + Vercel para frontend obliga a gestionar dos paneles, dos facturas y dos contratos.
- Fargate es algo más caro que EC2 si el uso es muy estable (no es el caso en V1).
- AWS tiene cierta complejidad incluso para casos sencillos (IAM, VPC, etc.).

### Riesgos y mitigaciones

- **Coste creciente**: alertas de presupuesto configuradas en AWS y Vercel desde el inicio.
- **Vendor lock-in**: mitigado parcialmente porque la aplicación corre en contenedores estándar (no usa servicios propietarios de AWS más allá de RDS, SQS y Secrets Manager, todos sustituibles).
- **Madrid (`eu-south-2`) puede ser interesante a futuro** por latencia y por encaje cultural, pero en V1 tiene menos servicios disponibles. Se reevalúa cuando madure.

## Referencias

- `operations/deployment.md` (configuración detallada de la infraestructura).
- `operations/security-and-privacy.md`, sección 3 (DPA con AWS y Vercel como encargados del tratamiento).
