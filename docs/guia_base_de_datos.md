# Guía base de datos.

Este documento describe cómo se ejecuta, inspecciona y visualiza la base de datos local de Chronode.

## 1. Tecnología y configuración

Chronode utiliza PostgreSQL 16 ejecutándose dentro de Docker Compose.

| Parámetro | Valor local |
|---|---|
| Motor | PostgreSQL 16 Alpine |
| Contenedor | `chronode_db` |
| Base de datos | `chronode_db` |
| Usuario | `postgres` |
| Contraseña | La definida en `POSTGRES_PASSWORD` en `.env` |
| Host desde el equipo local | `127.0.0.1` |
| Puerto desde el equipo local | `5433` |
| Puerto dentro del contenedor | `5432` |
| URL JDBC | `jdbc:postgresql://127.0.0.1:5433/chronode_db` |

Los valores anteriores son los valores por defecto definidos en [`docker-compose.yml`](../docker-compose.yml). Se pueden sobrescribir mediante las variables de entorno documentadas en [`variables_de_entorno.md`](./variables_de_entorno.md).

La aplicación Spring Boot utiliza la misma conexión por defecto en [`application.yml`](../backend/src/main/resources/application.yml).

## 2. Contenerización de PostgreSQL

El servicio `chronode_db` se define en [`docker-compose.yml`](../docker-compose.yml) y proporciona:

- PostgreSQL 16 sobre Alpine Linux.
- Reinicio automático salvo que se detenga explícitamente.
- Publicación del puerto `5433` del equipo hacia el puerto `5432` del contenedor.
- Persistencia mediante el volumen Docker `postgres_data`.
- Healthcheck con `pg_isready`.
- Carga del script [`docker/init.sql`](../docker/init.sql) al crear una base de datos nueva.

El script de inicialización habilita la extensión `pg_trgm`, utilizada para búsquedas por similitud.

### Arrancar la base de datos

Desde la raíz del proyecto:

```powershell
docker compose up -d
```

Comprobar el estado:

```powershell
docker compose ps
```

El contenedor debe aparecer como `running` y, después del healthcheck, como `healthy`.

Detener el contenedor conservando los datos:

```powershell
docker compose down
```

Detenerlo y eliminar también el volumen local:

```powershell
docker compose down -v
```

> `docker compose down -v` elimina la base de datos local persistida. Úsalo solo cuando quieras empezar desde cero.

## 3. Creación del esquema

Docker crea PostgreSQL y la extensión `pg_trgm`, pero no contiene directamente las tablas de la aplicación.

Las tablas se crean o actualizan mediante Hibernate cuando se inicia el backend, porque la configuración actual utiliza:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

Por tanto, el orden recomendado es:

1. Arrancar PostgreSQL con Docker.
2. Arrancar el backend.
3. Conectarse desde DBeaver.

Arranque normal, sin datos de prueba:

```powershell
Set-Location C:\WS-frangago71\chronode\backend
mvn spring-boot:run
```

Arranque con datos de prueba:

```powershell
Set-Location C:\WS-frangago71\chronode\backend
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

El perfil `dev` ejecuta `DataInitializer` y crea de forma idempotente usuarios, workspaces, membresías, niveles, nodos, tags y entradas de tiempo. El arranque normal no ejecuta ese inicializador.

## 4. Modelo de datos

Las entidades principales son:

- `users`: usuarios autenticables.
- `workspaces`: espacios de trabajo.
- `workspace_members`: relación entre usuarios y workspaces, con rol.
- `level_definitions`: niveles jerárquicos definidos por workspace.
- `level_nodes`: nodos de cada nivel y sus relaciones padre-hijo.
- `tags`: etiquetas asociadas a un workspace.
- `time_entries`: registros de tiempo.
- `time_entry_levels`: relación muchos a muchos entre entradas y nodos.
- `time_entry_tags`: relación muchos a muchos entre entradas y tags.

Todas las entidades principales heredan identificadores UUID y marcas de auditoría `created_at` y `updated_at` desde `BaseEntity`.

El diagrama visual del modelo está en [`diagrama_entidades.mmd`](./diagrama_entidades.mmd).

## 5. Conexión desde DBeaver

### Crear la conexión

1. Abrir DBeaver.
2. Seleccionar **Database > New Database Connection**.
3. Elegir **PostgreSQL**.
4. Introducir:

```text
Host: 127.0.0.1
Port: 5433
Database: chronode_db
Username: postgres
Password: el valor local de `POSTGRES_PASSWORD`
```

5. Mantener SSL desactivado para esta conexión local.
6. Pulsar **Test Connection**.
7. Si DBeaver solicita descargar el driver JDBC de PostgreSQL, aceptar.
8. Pulsar **Finish**.

### Navegar por las tablas

En el panel **Database Navigator**, expandir:

```text
Chronode Local
└── Databases
    └── chronode_db
        └── Schemas
            └── public
                └── Tables
```

Si las tablas no aparecen:

1. Confirmar que el backend se ha iniciado al menos una vez.
2. Hacer clic derecho sobre la conexión.
3. Seleccionar **Refresh**.
4. Expandir de nuevo `Schemas > public > Tables`.

## 6. Consultas útiles en DBeaver

Para abrir un editor SQL, hacer clic derecho sobre la conexión y seleccionar **SQL Editor > New SQL Script**.

### Usuarios

```sql
SELECT id, email, full_name, enabled, created_at
FROM users
ORDER BY email;
```

### Workspaces

```sql
SELECT id, name, code, description
FROM workspaces
ORDER BY code;
```

### Miembros y roles

```sql
SELECT
    u.email,
    w.code AS workspace_code,
    w.name AS workspace_name,
    wm.role
FROM workspace_members wm
JOIN users u ON u.id = wm.user_id
JOIN workspaces w ON w.id = wm.workspace_id
ORDER BY w.code, u.email;
```

### Niveles y nodos

```sql
SELECT
    w.code AS workspace_code,
    ld.depth_order,
    ld.name AS level_name,
    ln.name AS node_name,
    ln.color
FROM level_nodes ln
JOIN level_definitions ld ON ld.id = ln.level_definition_id
JOIN workspaces w ON w.id = ln.workspace_id
ORDER BY w.code, ld.depth_order, ln.name;
```

### Tags

```sql
SELECT
    w.code AS workspace_code,
    t.name,
    t.color
FROM tags t
JOIN workspaces w ON w.id = t.workspace_id
ORDER BY w.code, t.name;
```

### Entradas de tiempo

```sql
SELECT
    w.code AS workspace_code,
    te.description,
    te.start_time,
    te.end_time,
    te.duration_seconds
FROM time_entries te
JOIN workspaces w ON w.id = te.workspace_id
ORDER BY te.start_time DESC;
```

## 7. Diagnóstico rápido

### El contenedor no está activo

```powershell
docker compose up -d
docker compose ps
```

### DBeaver no conecta

Comprobar estos valores:

- Host: `127.0.0.1`
- Puerto: `5433`
- Base de datos: `chronode_db`
- Usuario: `postgres`
- Contraseña: el valor local de `POSTGRES_PASSWORD`
- SSL: desactivado

También se puede verificar PostgreSQL directamente:

```powershell
docker exec chronode_db psql -U postgres -d chronode_db -c "SELECT current_user, current_database();"
```

### La conexión funciona pero no hay tablas

El backend todavía no ha creado el esquema. Arrancarlo desde `backend/`:

```powershell
mvn spring-boot:run
```

### No aparecen los datos de prueba

Arrancar expresamente con el perfil `dev`:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

El perfil normal no ejecuta el seeder.
