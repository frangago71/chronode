# Chronode

Chronode es una aplicación web full-stack de seguimiento y gestión de tiempo de trabajo (*time tracking*) inspirada en herramientas como Clockify. Su propósito es ofrecer un registro ágil y categorizado mediante **espacios de trabajo (Workspaces)** independientes, con jerarquías de niveles dinámicos y algoritmos de búsqueda por similitud léxica (*fuzzy matching*).

---

## Cómo ejecutar la aplicación

### 1. Requisitos previos

* **Docker y Docker Compose:** Para la base de datos PostgreSQL 16.
* **Java Development Kit (JDK):** Versión 21 (LTS).
* **Apache Maven:** Versión 3.9 o superior.
* **Node.js:** Versión 20 o superior junto con el gestor **npm**.

---

### 2. Configuración de variables de entorno (.env)

Antes de levantar los contenedores o servicios, copia la plantilla de variables de entorno en la raíz del proyecto:

```bash
cp .env.example .env
```

> Para consultar la descripción de cada variable y cómo cargarlas según el entorno, consulta [docs/variables_de_entorno.md](docs/variables_de_entorno.md).

---

### 3. Base de datos (PostgreSQL 16)

Desde la raíz del proyecto, inicia el contenedor de base de datos con Docker:

```bash
docker compose up -d
```

* El contenedor `chronode_db` se ejecutará accesible en el host en `localhost:5433` (mapeado al puerto interno 5432).
* La base de datos `chronode_db` cuenta con persistencia y la extensión `pg_trgm` activada.

---

### 4. Backend (Spring Boot 3)

1. Accede al directorio del backend:
   ```bash
   cd backend
   ```

2. Asegúrate de tener Java 21 en el PATH antes de continuar.

3. Inicia el servidor:
   ```bash
   mvn spring-boot:run
   ```

* La API estará disponible en [http://localhost:8080](http://localhost:8080).
* Documentación interactiva Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (consulta la [guía de pruebas con Swagger](docs/guia_swagger.md)).
* Endpoint de verificación de estado: [http://localhost:8080/api/v1/health](http://localhost:8080/api/v1/health).

#### Datos de prueba opcionales

El backend puede ejecutarse como antes, sin activar ningún perfil:

```bash
mvn spring-boot:run
```

En este modo la aplicación arranca normalmente, pero no crea datos de prueba automáticamente.

Para poblar PostgreSQL con datos locales para desarrollo y pruebas de la API, activa explícitamente el perfil `dev`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

El inicializador del perfil `dev` crea de forma idempotente usuarios, workspaces, membresías, definiciones de nivel, nodos, tags y entradas de tiempo. Las credenciales disponibles son:

| Email | Contraseña | Rol inicial |
|---|---|---|
| `admin@chronode.com` | `password123` | `OWNER` |
| `user@chronode.com` | `password123` | `MEMBER` |

El inicializador no se ejecuta en producción ni en los tests por defecto.

---

### 5. Tests del backend

Desde el directorio `backend/`:

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar solo los tests del HealthCheck
mvn test -Dtest=HealthCheckServiceTest,HealthCheckControllerTest
```

> Para más detalle sobre qué testea cada clase, consulta [docs/tests.md](docs/tests.md).

---

### 6. Frontend (React + TypeScript + Vite)

1. En una nueva terminal, accede a la carpeta del frontend:
   ```bash
   cd frontend
   ```

2. Instala las dependencias necesarias:
   ```bash
   npm install
   ```

3. Inicia el servidor de desarrollo de Vite:
   ```bash
   npm run dev
   ```

* La interfaz de usuario estará disponible en [http://localhost:5173](http://localhost:5173).

---

## Documentación detallada

Para consultar la arquitectura, estilos y reglas de ingeniería, revisa los documentos en la carpeta `docs/`:

* [Estructura del proyecto](docs/estructura_del_proyecto.md): Organización y responsabilidades de carpetas en el monorepo.
* [Estándares y seguridad](docs/estandares_y_seguridad.md): Modelo de autorización stateless (JWT) y directrices de código limpio.
* [Guía de Swagger](docs/guia_swagger.md): Instrucciones para probar autenticación y endpoints con Swagger UI.
* [Variables de entorno](docs/variables_de_entorno.md): Gestión de credenciales, secretos JWT y configuración por entorno.
* [Tests](docs/tests.md): Estrategia de testing, clases disponibles y comandos de ejecución por módulo.
* [Base de datos, Docker y DBeaver](docs/base_de_datos_docker_dbeaver.md): Configuración de PostgreSQL, contenedor, conexión visual y consultas útiles.
