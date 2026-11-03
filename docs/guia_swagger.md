# Guía de pruebas con Swagger UI

Esta guía describe cómo interactuar con la documentación interactiva OpenAPI y probar el flujo de autenticación JWT y endpoints protegidos en Chronode.

---

## 1. Acceso a Swagger UI

Con el backend en ejecución (`mvn spring-boot:run` en `backend/`), abre tu navegador web en:

* **URL interactiva:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **Definición OpenAPI (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 2. Flujo de prueba paso a paso

### Paso 1: Registrar un nuevo usuario
1. Despliega la sección **Autenticación** y selecciona `POST /api/v1/auth/register`.
2. Haz clic en el botón **Try it out**.
3. El campo *Request body* se autocompleta con valores de prueba válidos por defecto:
   ```json
   {
     "email": "usuario@chronode.com",
     "password": "Password123!",
     "fullName": "Carlos Gómez"
   }
   ```
4. Haz clic en **Execute**.
5. Deberás recibir un código HTTP `201 Created` con el siguiente formato de respuesta:
   ```json
   {
     "accessToken": "eyJhbGciOiJIUzI1NiIsIn...",
     "tokenType": "Bearer",
     "expiresInSeconds": 3600,
     "userId": "b4e2870f-8c31-4e78-...",
     "email": "usuario@chronode.com",
     "fullName": "Carlos Gómez"
   }
   ```
6. **Copia el valor del `accessToken`** generado (únicamente la cadena sin comillas).

---

### Paso 2: Iniciar sesión (Login)
1. En la misma sección **Autenticación**, selecciona `POST /api/v1/auth/login`.
2. Haz clic en **Try it out**.
3. El cuerpo ya incluye las credenciales registradas:
   ```json
   {
     "email": "usuario@chronode.com",
     "password": "Password123!"
   }
   ```
4. Pulsa **Execute** y valida la respuesta `200 OK` con un nuevo token JWT.

---

### Paso 3: Autorizar peticiones protegidas (Botón Authorize)
1. Ve a la parte superior derecha de la página de Swagger UI y haz clic en el botón verde **Authorize** (con icono de candado).
2. En la ventana modal de **BearerAuth**, pega el token en el campo **Value**.
3. Haz clic en **Authorize** y luego en **Close**.
4. A partir de este momento, Swagger UI adjuntará automáticamente la cabecera HTTP `Authorization: Bearer <tu_token>` en todas las peticiones a endpoints protegidos.

---

### Paso 4: Comprobación de errores RFC 7807 (ProblemDetail)
Puedes probar respuestas de error controladas:
* **Credenciales erróneas:** En `POST /api/v1/auth/login`, introduce una contraseña incorrecta para observar la respuesta `401 Unauthorized`.
* **Validación de campos:** En `POST /api/v1/auth/register`, envía un correo no válido o una contraseña de menos de 8 caracteres para observar el detalle de validación `400 Bad Request`.
* **Email duplicado:** Intenta registrar dos veces consecutivas el mismo email para verificar la captura del conflicto con mensaje amigable.
