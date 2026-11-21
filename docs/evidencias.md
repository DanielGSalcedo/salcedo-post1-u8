# Evidencias de ejecución

Fecha de ejecución: **2026-11-20**  
Entorno: Windows 11, Java 25.0.4.1 (proyecto compilado con `--release 17`),
Spring Boot 3.5.6, H2 2.3.232, Maven 3.9.16.

La aplicación se ejecutó con:

```bash
mvn clean package
java -jar target/auditoria-hallazgos-0.0.1-SNAPSHOT.jar
```

## 1. Pruebas automatizadas

```text
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

- 8 pruebas unitarias de dominio (`HallazgoAuditoriaTest`).
- 1 prueba de integración del flujo REST completo (`HallazgoApiIntegrationTest`).

## 2. Registro de un hallazgo — `201 Created`

```http
POST /api/hallazgos
Content-Type: application/json

{
  "titulo": "Contrasenas por defecto en servidor de pruebas",
  "descripcion": "Hallazgo de prueba del laboratorio",
  "areaResponsable": "Infraestructura",
  "severidad": "ALTA",
  "fechaDeteccion": "2026-11-10"
}
```

```http
HTTP/1.1 201 Created

{"hallazgoId":"d30c9413-25d7-49f2-843e-aec83740ea12"}
```

## 3. Transiciones exitosas — `200 OK`

```http
PATCH /api/hallazgos/d30c9413-25d7-49f2-843e-aec83740ea12/iniciar-remediacion
Content-Type: application/json

{"responsable":"Equipo de Infraestructura","fechaLimite":"2026-12-05","notas":"Rotar credenciales"}
```

```json
{"estado":"EN_REMEDIACION"}
```

```http
PATCH /api/hallazgos/d30c9413-25d7-49f2-843e-aec83740ea12/cerrar
```

```json
{"estado":"CERRADO"}
```

```http
PATCH /api/hallazgos/e5050994-7234-4f93-9cb1-bedce6b23a00/reabrir
Content-Type: application/json

{"motivo":"El hallazgo reaparecio en la auditoria externa"}
```

```json
{"estado":"REABIERTO"}
```

## 4. Historial cronológico — `200 OK`

### Hallazgo cerrado sin reapertura

```http
GET /api/hallazgos/d30c9413-25d7-49f2-843e-aec83740ea12/historial
```

```json
[
  {
    "estadoAnterior": "ABIERTO",
    "estadoNuevo": "EN_REMEDIACION",
    "motivo": "Inicio de remediacion",
    "fecha": "2026-11-20T23:04:31.1104"
  },
  {
    "estadoAnterior": "EN_REMEDIACION",
    "estadoNuevo": "CERRADO",
    "motivo": "Cierre de remediacion",
    "fecha": "2026-11-20T23:04:31.190253"
  }
]
```

### Hallazgo reabierto (tres cambios, en orden)

```http
GET /api/hallazgos/e5050994-7234-4f93-9cb1-bedce6b23a00/historial
```

```json
[
  {
    "estadoAnterior": "ABIERTO",
    "estadoNuevo": "EN_REMEDIACION",
    "motivo": "Inicio de remediacion",
    "fecha": "2026-11-20T23:04:31.415042"
  },
  {
    "estadoAnterior": "EN_REMEDIACION",
    "estadoNuevo": "CERRADO",
    "motivo": "Cierre de remediacion",
    "fecha": "2026-11-20T23:04:31.447443"
  },
  {
    "estadoAnterior": "CERRADO",
    "estadoNuevo": "REABIERTO",
    "motivo": "El hallazgo reaparecio en la auditoria externa",
    "fecha": "2026-11-20T23:04:31.489346"
  }
]
```

## 5. Dashboard consolidado — `200 OK`

```http
GET /api/hallazgos/dashboard
```

```json
{
  "porSeveridad": [
    {"categoria": "ALTA", "total": 1},
    {"categoria": "BAJA", "total": 1},
    {"categoria": "CRITICA", "total": 1},
    {"categoria": "MEDIA", "total": 1}
  ],
  "porEstado": [
    {"categoria": "ABIERTO", "total": 1},
    {"categoria": "CERRADO", "total": 2},
    {"categoria": "REABIERTO", "total": 1}
  ],
  "promedioDiasCierrePorArea": [
    {"categoria": "Infraestructura", "promedioDias": 10.0},
    {"categoria": "Sistemas", "promedioDias": 4.0}
  ]
}
```

Los promedios corresponden a hallazgos cerrados detectados 10 y 4 días antes de la fecha de
cierre. El hallazgo reabierto no se cuenta en el promedio porque su estado actual ya no es
`CERRADO`.

## 6. Validaciones y errores

### Cerrar un hallazgo abierto sin plan — `400 Bad Request`

```http
PATCH /api/hallazgos/d57148b5-a702-48d3-8b62-a4eb89578415/cerrar
```

```json
{"error":"No se puede cerrar un hallazgo sin plan de remediacion"}
```

### Reabrir un hallazgo abierto — `400 Bad Request`

```http
PATCH /api/hallazgos/d57148b5-a702-48d3-8b62-a4eb89578415/reabrir
Content-Type: application/json

{"motivo":"Intento invalido de reapertura"}
```

```json
{"error":"No se puede transicionar de ABIERTO a REABIERTO"}
```

### Cerrar un hallazgo reabierto sin iniciar remediación — `400 Bad Request`

```http
PATCH /api/hallazgos/e5050994-7234-4f93-9cb1-bedce6b23a00/cerrar
```

```json
{"error":"No se puede transicionar de REABIERTO a CERRADO"}
```

### Hallazgo inexistente — `404 Not Found`

```http
GET /api/hallazgos/00000000-0000-0000-0000-000000000000
```

```json
{"error":"No existe un hallazgo con id 00000000-0000-0000-0000-000000000000"}
```

### UUID con formato inválido — `400 Bad Request`

```http
GET /api/hallazgos/no-es-uuid
```

```json
{"error":"Invalid UUID string: no-es-uuid"}
```

### Validación de campos obligatorios — `400 Bad Request`

```http
POST /api/hallazgos

{"titulo":"","areaResponsable":"","severidad":"ALTA","fechaDeteccion":"2026-08-01"}
```

```json
{"error":"areaResponsable: El area responsable es obligatoria; titulo: El titulo es obligatorio"}
```

## 7. Persistencia H2

La aplicación usa `jdbc:h2:mem:auditoria` con `spring.jpa.hibernate.ddl-auto=update`.
Las tablas creadas son:

- `hallazgos` — estado actual del agregado (fuente de verdad).
- `historial_cambios_estado` — bitácora append-only de transiciones.

La consola H2 queda disponible en `http://localhost:8080/h2-console` durante la ejecución.
