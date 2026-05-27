# Despliegue y operaciones — Fragmind

## 1. Objetivo del documento

Este documento describe cómo se despliega y opera Fragmind: proveedores cloud usados, entornos, pipeline CI/CD, backups, monitorización y procedimientos operativos.

Asume que el lector ya conoce la arquitectura (`technical/architecture.md`) y los requisitos no funcionales (`technical/non-functional-requirements.md`). Las decisiones de seguridad operativa están en `operations/security-and-privacy.md`.

---

## 2. Proveedores

| Servicio | Proveedor | Justificación breve |
|---|---|---|
| Cloud principal | **AWS** (región `eu-central-1`, Frankfurt) | Madurez, región UE para GDPR, free tier amplio |
| Hosting del frontend | **Vercel** | Despliegue trivial para Next.js, CDN global incluido |
| Repositorio y CI | **GitHub + GitHub Actions** | Integración nativa con el resto del stack |
| Proveedor IA | **OpenAI API** (definido en arquitectura) | — |
| Email transaccional | **Resend** | Ver ADR-013 |
| Registro de dominio y DNS | **Cloudflare** | Gratis, CDN, DDoS protection, gestión de DNS sencilla |
| Cola de trabajos IA | **AWS SQS** | Ver ADR-009 |
| Migraciones de BBDD | **Flyway** | Ver ADR-011 |
| Infraestructura como código | **AWS CDK (TypeScript)** | Ver ADR-012 |

Los proveedores con datos personales (AWS, Vercel, OpenAI, email) requieren DPA firmado antes de producción (ver `operations/security-and-privacy.md`, sección 3).

---

## 3. Arquitectura de despliegue en AWS

```text
                  ┌─────────────────────────┐
                  │   Usuario final         │
                  └───────────┬─────────────┘
                              │ HTTPS
              ┌───────────────┼──────────────────┐
              │               │                  │
        ┌─────▼─────┐   ┌─────▼──────┐    ┌──────▼──────────┐
        │  Vercel   │   │  AWS ALB   │    │ Chrome/Firefox  │
        │ (Next.js) │   │            │    │ Web Store       │
        └───────────┘   └─────┬──────┘    │ (extensión)     │
                              │           └─────────────────┘
                    ┌─────────┴─────────┐
                    │                   │
            ┌───────▼────────┐  ┌───────▼────────┐
            │ Backend API    │  │ Workers IA     │
            │ (Fargate)      │  │ (Fargate)      │
            └───────┬────────┘  └───────┬────────┘
                    │                   │
                    │           ┌───────▼────────┐
                    │           │ SQS (cola)     │
                    │           └────────────────┘
                    │
            ┌───────▼────────┐  ┌────────────────┐
            │ RDS PostgreSQL │  │ Secrets Manager│
            │ (Multi-AZ opc.)│  │                │
            └────────────────┘  └────────────────┘
```

### 3.1 Servicios AWS usados

| Servicio | Uso |
|---|---|
| **ECS Fargate** | Ejecutar contenedores del backend y de los workers IA |
| **Application Load Balancer (ALB)** | Punto de entrada HTTPS al backend |
| **RDS PostgreSQL** | Base de datos principal |
| **SQS** | Cola de trabajos IA |
| **Secrets Manager** | Claves de API, credenciales de BBDD, secretos JWT |
| **CloudWatch** | Logs centralizados y métricas |
| **S3** | Almacenamiento de exports de datos (portabilidad GDPR) |
| **Route 53** (opcional) | DNS si se decide centralizar en AWS |
| **ACM** | Certificados TLS gestionados |

Para V1 se evita Kubernetes (EKS) por complejidad operativa innecesaria a esta escala. Fargate ofrece contenedores serverless sin gestionar nodos.

### 3.2 Frontend en Vercel

- Despliegue automático desde el repositorio GitHub.
- CDN global incluido.
- Variables de entorno gestionadas en el panel de Vercel.
- Previews por pull request, útiles para revisión de cambios visuales.

### 3.3 Extensión de navegador

- Publicada en **Chrome Web Store** y **Firefox Add-ons (AMO)**.
- La publicación en stores requiere proceso de revisión propio de cada plataforma. Hay que prever días de margen antes del lanzamiento.
- Versiones de la extensión gestionadas con tags semánticos (`extension-v1.0.0`).

---

## 4. Entornos

V1 contempla dos entornos: `dev` y `prod`. No hay `staging` separado para simplificar operaciones al inicio. Cuando el producto crezca y se incorporen testers o usuarios reales en pruebas, se evaluará añadir `staging`.

| Entorno | Propósito | URL frontend | URL API |
|---|---|---|---|
| `dev` | Desarrollo y pruebas internas | `dev.fragmind.app` (orientativo) | `api.dev.fragmind.app` |
| `prod` | Producción real | `fragmind.app` | `api.fragmind.app` |

### 4.1 Configuración por entorno

Diferencias principales entre `dev` y `prod`:

- **Base de datos**: instancias separadas. La de `dev` puede ser una `db.t4g.micro`; la de `prod` un tamaño adecuado a la carga con Multi-AZ activado cuando el coste sea asumible.
- **Cuotas de IA**: las cuotas de `dev` pueden ser mayores o ilimitadas para facilitar pruebas; las de `prod` siguen lo definido en `non-functional-requirements.md`.
- **Logs**: en `dev` se aceptan logs más verbosos; en `prod` se respeta lo definido en `security-and-privacy.md`.
- **OpenAI**: claves de API distintas con cuota separada para que el desarrollo no consuma presupuesto de producción.
- **Datos**: nunca se replican datos reales de `prod` a `dev`. Si hace falta poblar `dev`, se usan datos sintéticos.

### 4.2 Acceso al entorno `dev`

- Accesible solo desde la red del equipo (puede ser una VPN sencilla, o restricción por IP en `dev`).
- Login deshabilitado para usuarios externos.

---

## 5. Pipeline CI/CD

### 5.1 Repositorios y ramas

- Un repositorio monorepo o tres repositorios separados (backend, frontend, extensión). Recomendación inicial: **tres repositorios separados** porque cada uno tiene ciclo de despliegue independiente (especialmente la extensión, sujeta a aprobación de stores).
- Rama principal: `main`.
- Trabajo en ramas de feature: `feat/...`, `fix/...`. Pull request a `main`.
- Tags semánticos para releases: `v0.1.0`, `v0.2.0`...

### 5.2 GitHub Actions — Backend (Spring Boot)

Pipeline al hacer push a una rama:

1. **Lint y formato**: comprobación de estilo con Checkstyle / Spotless.
2. **Build**: `mvn` o `gradle` build.
3. **Tests unitarios**.
4. **Tests de integración** con Testcontainers (PostgreSQL real en contenedor).
5. **Análisis de dependencias** (Dependabot, OWASP dependency check).
6. **Construcción de imagen Docker**.

Al hacer merge a `main`:

7. **Push de la imagen** a Amazon ECR.
8. **Despliegue automático a `dev`**: actualizar la tarea de Fargate con la nueva imagen.

Al crear un tag `vX.Y.Z`:

9. **Despliegue a `prod`** con aprobación manual del responsable.

### 5.3 GitHub Actions — Frontend (Next.js)

Vercel se encarga del CI/CD del frontend directamente al estar integrado con GitHub:

- Push a rama: preview deploy automático.
- Merge a `main`: deploy automático a `dev`.
- Promoción a `prod` desde el panel de Vercel o vía tag.

### 5.4 GitHub Actions — Extensión

Pipeline:

1. Lint, build, tests.
2. Empaquetado del `.zip` o `.xpi`.
3. Subida automática a Chrome Web Store y AMO **bajo aprobación manual** (no se automatiza la publicación porque cada cambio requiere revisión humana de la store, no tiene sentido encadenarla).

### 5.5 Migraciones de base de datos

- Las migraciones se aplican con **Flyway** (ver ADR-011) como parte del arranque del backend. Esto implica que el contenedor recién desplegado aplica las migraciones antes de aceptar tráfico.
- **Regla**: las migraciones deben ser compatibles hacia atrás durante al menos un despliegue, para permitir despliegues progresivos sin downtime.
- Para cambios destructivos (eliminar columnas, renombrar tablas) se usa el patrón en dos pasos: primero migración aditiva + despliegue, luego migración destructiva en un despliegue posterior.

---

## 6. Backups y recuperación

### 6.1 Base de datos

- **Snapshots automáticos** de RDS: diarios, retención 7 días en `dev`, 30 días en `prod`.
- **Point-in-time recovery** activado en `prod`: permite restaurar la base de datos a cualquier momento dentro de la ventana de retención.
- **Backups manuales** antes de cambios mayores (migraciones grandes, cambios estructurales).

### 6.2 Prueba de restauración

- **Trimestralmente** se realiza una prueba de restauración: levantar una nueva instancia desde un snapshot en un entorno aislado, verificar integridad de los datos, documentar el resultado.
- Un backup que nunca se ha restaurado no es un backup fiable.

### 6.3 Otros datos

- **Secretos**: AWS Secrets Manager mantiene versionado. Antes de rotar una clave se valida que la versión anterior sigue disponible.
- **Configuración de infraestructura**: gestionada como código con AWS CDK (ver ADR-012). El propio repositorio es el backup.
- **Logs**: CloudWatch retiene logs según lo definido en `security-and-privacy.md` (30 días aplicación, 90 días seguridad).

### 6.4 Borrado físico de usuarios

Como se indicó en `security-and-privacy.md` sección 11.4, los backups que contengan datos de usuarios eliminados se purgan según el ciclo natural de rotación. Con 30 días de retención de snapshots en `prod`, el borrado físico completo se alcanza ~30 días después del borrado físico en la base de datos principal. Esto debe documentarse en la política de privacidad.

---

## 7. Observabilidad

### 7.1 Logs

- Todos los servicios envían logs a **CloudWatch Logs** con formato JSON estructurado.
- Cada log incluye: timestamp, nivel, servicio, `requestId`, `userId` (si aplica), mensaje, contexto adicional.
- Búsqueda y filtrado mediante CloudWatch Logs Insights.

### 7.2 Métricas

Métricas mínimas que se monitorizan en V1:

- **Backend**: tiempo de respuesta p50/p95/p99 por endpoint, tasa de errores 4xx y 5xx, número de peticiones por minuto.
- **Workers IA**: profundidad de la cola SQS, tiempo medio de procesamiento por tipo de operación, tasa de fallos, coste estimado por hora.
- **Base de datos**: conexiones activas, CPU, almacenamiento usado, queries lentas.
- **Negocio**: usuarios activos diarios, fragmentos capturados por día, operaciones IA completadas.

### 7.3 Alertas

Alertas críticas que despiertan al responsable:

- Tasa de errores 5xx > 5% durante 5 minutos.
- Backend no responde durante > 2 minutos.
- Profundidad de la cola IA crece sin procesarse durante > 10 minutos.
- Almacenamiento de la base de datos > 80% lleno.
- Fallo en backup automático.
- Coste diario de OpenAI > umbral configurado (alerta temprana de uso anómalo).

Alertas informativas (no despiertan, se revisan en horario laboral):

- Picos de uso inusuales.
- Dependencias con vulnerabilidades nuevas.
- Tasa de errores 4xx alta (puede indicar problema en cliente).

### 7.4 Health checks

- Endpoint `/health` en el backend que verifica conexión a BBDD y a SQS. El ALB lo usa para decidir si una tarea de Fargate es saludable.
- Endpoint `/health/deep` (autenticado) que verifica adicionalmente conectividad con OpenAI y devuelve métricas básicas.

---

## 8. Escalado

Para la escala V1 (decenas a cientos de usuarios) la configuración inicial es:

- **Backend API**: 1-2 tareas Fargate. Auto-scaling configurado pero con umbrales generosos.
- **Workers IA**: 1-2 tareas Fargate, auto-scaling según profundidad de la cola SQS.
- **PostgreSQL**: instancia pequeña (`db.t4g.small` o similar). Sin réplica de lectura en V1.

El sistema está diseñado para escalar horizontalmente (más tareas Fargate) sin cambios en código. La base de datos es el cuello de botella natural; cuando se acerque a sus límites se evaluará migración a un tamaño mayor o réplicas de lectura.

---

## 9. Costes estimados

Estimación orientativa de coste mensual en AWS para V1 (decenas-cientos de usuarios):

| Concepto | Coste mensual aprox. |
|---|---|
| RDS PostgreSQL (`db.t4g.small`) | 25-35 € |
| Fargate (2-4 tareas) | 30-60 € |
| ALB | 20 € |
| SQS, Secrets Manager, CloudWatch | 5-15 € |
| Transferencia de datos | 5-10 € |
| Backups (snapshots) | 5-10 € |
| **Total AWS** | **~90-150 €** |
| Vercel (plan Hobby si encaja, o Pro) | 0-20 € |
| OpenAI API | Variable según uso real |

Los costes de OpenAI son los más imprevisibles y los que más se vigilan (ver alertas en sección 7.3). Las cuotas estrictas definidas en `non-functional-requirements.md` limitan el riesgo.

---

## 10. Procedimientos operativos

### 10.1 Despliegue a producción

1. Pull request aprobado y mergeado a `main`.
2. Despliegue automático a `dev`. Validación manual del cambio en `dev`.
3. Creación de tag `vX.Y.Z` en GitHub.
4. Aprobación manual del despliegue a `prod` en GitHub Actions.
5. Despliegue progresivo: la nueva tarea Fargate arranca, pasa health checks, recibe tráfico; las tareas antiguas se retiran.
6. Verificación post-despliegue: revisar dashboards de errores y latencias durante los primeros 15 minutos.

### 10.2 Rollback

- Si un despliegue introduce un fallo, se hace rollback re-desplegando la imagen Docker de la versión anterior (todas las imágenes quedan en ECR).
- Para cambios de base de datos destructivos: el rollback requiere también desplegar la versión anterior del esquema, lo cual es más delicado. Por eso la regla del compatible hacia atrás (sección 5.5).

### 10.3 Rotación de secretos

- **Anual** por defecto, **inmediata** si hay sospecha de compromiso.
- Procedimiento: generar la nueva clave en el proveedor, actualizar Secrets Manager, redesplegar los servicios afectados, invalidar la clave antigua tras verificar que todo funciona.

### 10.4 Gestión de incidentes

Detallada en `operations/security-and-privacy.md` sección 14. Resumen:

1. Detección y contención.
2. Evaluación de impacto.
3. Notificación a AEPD en 72h si aplica.
4. Notificación a usuarios afectados si aplica.
5. Post mortem documentado.

---

## 11. Infraestructura como código

Toda la infraestructura de AWS se gestiona **como código** con **AWS CDK en TypeScript** (ver ADR-012). Nunca a mano por consola.

- **Beneficios**: reproducibilidad, versionado, revisión por pares vía pull request, recuperación rápida ante desastres, consistencia de lenguaje con frontend y extensión.
- **Excepciones aceptables**: experimentos puntuales en `dev` que luego se trasladan a código antes de aplicar a `prod`.
- **Norma estricta**: ningún cambio manual en consola de AWS en `prod`. Todo pasa por código, pull request y `cdk diff` antes de `cdk deploy`.

---

## 12. Decisiones pendientes

Estas decisiones quedan abiertas y se concretarán cuando corresponda operativamente:

- **Activar Multi-AZ en RDS** desde el inicio o esperar. Decisión actual: V1 sin Multi-AZ para reducir coste (la disponibilidad objetivo del 99% lo permite). Se activará cuando el coste sea asumible o la disponibilidad lo exija.
- **Cuándo introducir un entorno `staging`** separado. Se evaluará cuando el producto incorpore testers regulares o usuarios beta.
- **Repositorio donde vive el código CDK**: dentro del repo de backend o en repo separado `fragmind-infrastructure`. Se decide al arrancar el desarrollo.

---

## 13. Aspectos no cubiertos en este documento

- Detalle de implementación del backend → código del repositorio.
- Decisiones de seguridad operativa → `operations/security-and-privacy.md`.
- Requisitos de rendimiento y disponibilidad → `technical/non-functional-requirements.md`.
- Decisiones técnicas con contexto → `decisions/` (ADRs, pendiente).
- Manual de operación detallado (runbooks por incidente) → pendiente de redactar conforme aparezcan incidentes reales.
