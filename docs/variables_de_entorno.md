# Variables de entorno y configuración

Este documento describe la gestión centralizada de variables de entorno, credenciales y secretos de configuración en Chronode.

---

## 1. Modelo de configuración (.env y .env.example)

El proyecto utiliza un archivo `.env` en la raíz del repositorio para centralizar la configuración de base de datos, puertos, credenciales de servicios y secretos criptográficos de tokens JWT.

* **`.env.example`:** Plantilla de referencia versionada en Git con variables documentadas y valores seguros para entornos de desarrollo local.
* **`.env`:** Archivo local donde residen los valores reales de ejecución. **Este archivo está estrictamente ignorado en `.gitignore`** para evitar la filtración accidental de credenciales y secretos en el repositorio.

---

## 2. Variables disponibles y descripción

| Variable | Descripción | Valor por defecto (Local) |
| :--- | :--- | :--- |
| `POSTGRES_DB` | Nombre de la base de datos PostgreSQL | `chronode_db` |
| `POSTGRES_USER` | Usuario administrador de la base de datos | `postgres` |
| `POSTGRES_PASSWORD` | Contraseña del usuario de la base de datos | `postgrespassword` |
| `POSTGRES_PORT` | Puerto expuesto en el host para PostgreSQL | `5433` |
| `SERVER_PORT` | Puerto del servidor Tomcat en Spring Boot | `8080` |
| `SPRING_DATASOURCE_URL` | URL de conexión JDBC hacia PostgreSQL | `jdbc:postgresql://localhost:5433/chronode_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de conexión JDBC | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de conexión JDBC | `postgrespassword` |
| `JWT_SECRET` | Clave secreta simétrica HMAC-SHA256 (mínimo 256 bits) | *Clave autogenerada para desarrollo* |
| `JWT_EXPIRATION_MS` | Tiempo de vida del token de acceso (en ms) | `3600000` (1 hora) |
| `VITE_API_URL` | URL base de la API REST consumida por el cliente Vite | `http://localhost:8080/api/v1` |

---

## 3. Instrucciones de uso según el entorno

### A. Primer inicio en desarrollo local
1. Copia la plantilla `.env.example` para generar tu archivo local `.env`:
   ```bash
   cp .env.example .env
   ```
2. Modifica cualquier valor en `.env` si requieres puertos o credenciales diferentes.
3. Inicia la base de datos con Docker Compose (Docker detecta y carga automáticamente el archivo `.env` de la raíz):
   ```bash
   docker compose up -d
   ```

### B. Ejecución del backend con variables cargadas
Spring Boot lee directamente las variables de entorno del sistema operativo. Puedes ejecutarlas de las siguientes maneras:

* **Desde terminal PowerShell (Windows):**
  ```powershell
  # Cargar variables del .env temporalmente en la sesión
  Get-Content .env | Where-Object { $_ -notmatch '^#' -and $_ -match '=' } | ForEach-Object {
      $name, $value = $_.Split('=', 2)
      [System.Environment]::SetEnvironmentVariable($name, $value, "Process")
  }
  cd backend
  mvn spring-boot:run
  ```

* **Desde terminal Bash / Linux / macOS:**
  ```bash
  export $(grep -v '^#' .env | xargs)
  cd backend
  mvn spring-boot:run
  ```

* **Desde IntelliJ IDEA / Eclipse / VS Code:**
  Configurar la ruta del archivo `.env` en el *Run/Debug Configuration* (mediante plugins como *EnvFile* en IntelliJ o definiendo el archivo `envFile` en `launch.json` de VS Code).

* **En entornos de producción o CI/CD:**
  Inyectar las variables como secretos del orquestador (GitHub Actions Secrets, variables de Kubernetes ConfigMaps/Secrets o variables de entorno en contenedores Docker).

---

## 4. Buenas prácticas de seguridad para tokens JWT

* **Longitud mínima de clave:** Para algoritmos `HMAC-SHA256`, la clave secreta `JWT_SECRET` debe contener al menos 256 bits (32 bytes).
* **Generación segura en producción:** Queda prohibido reutilizar la clave de desarrollo en producción. Para generar una clave aleatoria segura de 256 bits codificada en hexadecimal:
  ```bash
  openssl rand -hex 32
  ```
* **Rotación y expiración:** Mantener una expiración corta (`15m` a `60m`) en el token de acceso para limitar la ventana de exposición en caso de compromiso de credenciales.
