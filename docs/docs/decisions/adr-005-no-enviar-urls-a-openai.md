# ADR-005: No enviar URLs ni dominios a OpenAI

- **Estado**: Aceptado
- **Fecha**: 2026-05-28
- **Decisores**: Equipo Fragmind
- **Etiquetas**: gdpr, privacidad, ia

## Contexto

Cada fragmento guardado en Fragmind incluye, entre otros campos, la URL de origen y el dominio de la página. Cuando el usuario lanza una operación IA (resumen, glosario, ideas principales, sugerencias de etiquetas o búsqueda), hay que decidir qué campos exactos se envían al proveedor IA (OpenAI).

La pregunta concreta: ¿se incluyen las URLs y los dominios en el payload enviado a OpenAI, o solo el contenido textual?

## Alternativas consideradas

### Opción A: Enviar URLs y dominios junto al contenido

- Ventajas:
    - La IA podría detectar patrones por dominio (fragmentos de la misma fuente).
    - Las sugerencias de búsqueda podrían inspirarse en los dominios consultados.
    - Sugerencias de etiquetas potencialmente más precisas (un dominio puede aportar contexto).
- Desventajas:
    - El proveedor IA pasa a conocer los patrones de navegación del usuario.
    - Los dominios son metadatos sensibles: revelar que alguien lee `clinica-xxx.com` o foros específicos puede ser indicativo de información personal.
    - Aumenta la superficie de datos transferidos a EEUU bajo ADR-004.

### Opción B: Enviar solo el contenido textual y nombres de workspace/notas

- Ventajas:
    - Minimización de datos: solo se envía lo estrictamente necesario para la operación.
    - El proveedor IA no infiere patrones de navegación.
    - Alineado con el principio GDPR de minimización (artículo 5.1.c).
    - Si el DPF se invalida, menor exposición histórica.
- Desventajas:
    - Calidad ligeramente menor en operaciones donde la fuente podría aportar contexto (poco probable en V1).

## Decisión

Se elige la **Opción B**: las URLs y los dominios de los fragmentos **nunca se envían a OpenAI**. Permanecen exclusivamente en la base de datos de Fragmind.

El detalle por operación está documentado en `operations/security-and-privacy.md`, sección 5.2.

## Consecuencias

### Positivas

- Cumple el principio GDPR de minimización.
- Reduce el riesgo reputacional y legal asociado a transferir patrones de navegación.
- Simplifica la política de privacidad de cara al usuario: "no enviamos a OpenAI información sobre qué páginas visitas, solo el texto que tú decides guardar".
- Si en el futuro se invalida el DPF, la exposición histórica es menor.

### Negativas

- Posible pérdida marginal de calidad en sugerencias donde el dominio podría aportar contexto. Se considera asumible dado que las operaciones IA de V1 (resumen, glosario, ideas, etiquetas, búsqueda) funcionan principalmente sobre el contenido textual.

### Riesgos y mitigaciones

- Si en evaluación posterior se detecta que la calidad de las sugerencias mejora significativamente al incluir dominios, se reevalúa la decisión con un nuevo ADR. La reevaluación debe valorar el coste de privacidad frente al beneficio funcional.

## Referencias

- `operations/security-and-privacy.md`, sección 5 (política de envío de datos al proveedor IA).
- ADR-004 (transferencia de datos a EEUU).
- GDPR artículo 5.1.c (principio de minimización).
