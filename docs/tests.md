# Tests

Este documento centraliza la estrategia de testing del proyecto y las instrucciones para ejecutar los tests de cada módulo.

---

## Backend 

### Ejecutar los tests

Desde el directorio `backend/`:

```bash
# Todos los tests
mvn test

# Una clase concreta
mvn test -Dtest=NombreDeLaClaseTest

# Varios tests a la vez
mvn test -Dtest=ClaseATest,ClaseBTest
```

### Informe de resultados

Maven genera el informe tras cada ejecución en:

```
backend/target/surefire-reports/
```

### Tests disponibles

| Clase | Tipo | Descripción |
|---|---|---|
| `HealthCheckServiceTest` | Unitario | Verifica que el servicio devuelve `status: UP` y los datos de la aplicación. No levanta Spring Context. |
| `HealthCheckControllerTest` | Capa web (MockMvc) | Verifica que `GET /api/v1/health` responde `200 OK` con el JSON esperado. |

### HealthCheck — respuesta de ejemplo

```json
{
  "status": "UP",
  "application": "Chronode Backend",
  "version": "1.0.0",
  "environment": "development",
  "timestamp": "2026-10-20T16:15:00Z"
}
```

---

## Frontend (React + Vite)

> Pendiente de añadir tests (Vitest / React Testing Library).
