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

| Módulo | Clase | Tipo | Tests | Descripción |
|---|---|---|:---:|---|
| **HealthCheck** | `HealthCheckServiceTest` | Unitario | 1 | Verifica que el servicio devuelve `status: UP` y los metadatos de la aplicación sin levantar contexto Spring. |
| **HealthCheck** | `HealthCheckControllerTest` | Capa web (`@WebMvcTest`) | 1 | Verifica que `GET /api/v1/health` responde `200 OK` con el JSON estructurado esperado. |
| **Auth** | `AuthServiceTest` | Unitario (`MockitoExtension`) | 4 | Valida la lógica de negocio de login y registro, emisión de tokens JWT y manejo de excepciones por credenciales erróneas o email duplicado. |
| **Auth** | `AuthControllerTest` | Capa web (`@WebMvcTest`) | 5 | Verifica endpoints `/api/v1/auth/login` y `/api/v1/auth/register`, códigos HTTP (200, 201), validación de campos (`@Valid`) y respuestas de error RFC 7807 (`ProblemDetail`). |
| **Security** | `JwtTokenProviderTest` | Unitario | 6 | Comprueba la generación de tokens HMAC-SHA256, extracción de claims (`userId`, `email`), detección de firmas manipuladas, expiración, cadenas malformadas y nulos/vacíos. |
| **Security** | `JwtAuthenticationFilterTest` | Unitario (`MockitoExtension`) | 4 | Comprueba la extracción del Bearer token en cabeceras HTTP, inyección de autenticación en el `SecurityContext` y omisión ante tokens ausentes o inválidos. |
| **Config** | `SecurityConfigTest` | Integración web (`@WebMvcTest`) | 2 | Verifica la cadena de filtros de Spring Security: rechazo de acceso no autenticado a endpoints protegidos (`401 Unauthorized`) y autorización con Bearer token válido. |

---

### Detalle por módulo

#### 1. HealthCheck (`com.chronode.healthcheck`)

- **`HealthCheckServiceTest`**: Comprueba la generación de la respuesta con `status: UP`, nombre de app, versión y timestamp.
- **`HealthCheckControllerTest`**: Simula peticiones HTTP a `GET /api/v1/health` usando `MockMvc`.

Respuesta de ejemplo (`GET /api/v1/health`):

```json
{
  "status": "UP",
  "application": "Chronode Backend",
  "version": "1.0.0",
  "environment": "development",
  "timestamp": "2026-10-20T16:15:00Z"
}
```

#### 2. Autenticación (`com.chronode.auth`)

- **`AuthServiceTest`**:
  - `deberiaRetornarAuthResponse_cuandoLoginEsExitoso`: Autentica credenciales vía `AuthenticationManager` y genera `AuthResponse` con token JWT válido.
  - `deberiaLanzarExcepcion_cuandoCredencialesSonInvalidas`: Comprueba lanzamiento de `BadCredentialsException` ante contraseñas incorrectas.
  - `deberiaRegistrarUsuarioYRetornarAuthResponse_cuandoDatosSonValidos`: Encripta contraseña con BCrypt, persiste usuario y genera token de sesión.
  - `deberiaLanzarExcepcion_cuandoEmailYaEstaRegistrado`: Rechaza con `IllegalArgumentException` si el correo ya está registrado en base de datos.

- **`AuthControllerTest`**:
  - `deberiaRetornar200Ok_cuandoLoginEsValido`: `POST /api/v1/auth/login` devuelve `200 OK` con JSON `AuthResponse`.
  - `deberiaRetornar400BadRequest_cuandoLoginTieneCamposInvalidos`: `POST /api/v1/auth/login` devuelve `400 Bad Request` con `ProblemDetail` ante campos requeridos vacíos.
  - `deberiaRetornar201Created_cuandoRegistroEsValido`: `POST /api/v1/auth/register` crea usuario y responde `201 Created` con token.
  - `deberiaRetornar400BadRequest_cuandoPasswordEsCorta`: Comprueba validación `@Size(min = 6)` con `400 Bad Request`.
  - `deberiaRetornar400BadRequest_cuandoEmailYaEstaRegistrado`: Captura excepción de negocio y la transforma en error RFC 7807 (`Parámetro inválido`).

Respuesta de ejemplo exitosa de autenticación (`AuthResponse`):

```json
{
  "accessToken": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "email": "usuario@chronode.com",
  "fullName": "Usuario Chronode"
}
```

#### 3. Seguridad y JWT (`com.chronode.security.jwt`)

- **`JwtTokenProviderTest`**:
  - Generación de token firmado con clave secreta HMAC-SHA256.
  - Extracción de claims: `userId` (como `UUID`) y custom claim `email`.
  - Validación de firma: retorna `false` si el token fue firmado con una clave distinta.
  - Validación de expiración: retorna `false` ante tokens expirados.
  - Validación de formato: retorna `false` si el string está malformado.
  - Validación de nulos/espacios: retorna `false` ante cadenas vacías o `null`.

- **`JwtAuthenticationFilterTest`**:
  - `deberiaEstablecerAutenticacion_cuandoTokenEsValido`: Extrae el token del header `Authorization`, valida y coloca un `UsernamePasswordAuthenticationToken` en el `SecurityContextHolder`.
  - `deberiaNoEstablecerAutenticacion_cuandoTokenEsInvalido`: No autentica si el token no supera la validación.
  - `deberiaNoEstablecerAutenticacion_cuandoNoHayEncabezadoAuthorization`: Permite continuar la cadena sin alterar el contexto cuando no se envía la cabecera.
  - `deberiaNoEstablecerAutenticacion_cuandoEncabezadoNoEsBearer`: Ignora cabeceras que no comienzan con el prefijo `Bearer ` (ej. `Basic`).

#### 4. Configuración de seguridad (`com.chronode.config`)

- **`SecurityConfigTest`**:
  - `deberiaRetornar401_cuandoSeAccedeAEndpointProtegidoSinToken`: Acceso a rutas protegidas sin cabecera devuelve `401 Unauthorized` en formato `ProblemDetail` (manejado por `JwtAuthenticationEntryPoint`).
  - `deberiaPermitirAcceso_cuandoSeProporcionaTokenValido`: Acceso con token Bearer válido responde `200 OK` al endpoint protegido.

Respuesta de ejemplo `401 Unauthorized` (`ProblemDetail`):

```json
{
  "type": "https://chronode.com/errors/unauthorized",
  "title": "No autenticado",
  "status": 401,
  "detail": "Acceso denegado: debe proporcionar un token JWT válido",
  "instance": "/api/v1/secure/data"
}
```

---

## Frontend (React + Vite)

> Pendiente de añadir tests (Vitest / React Testing Library).
