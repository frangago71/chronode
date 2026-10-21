# Estructura del proyecto

Este documento describe la organización de carpetas y responsabilidades de los módulos que componen el monorepo de Chronode.

---

## Organización general del monorepo

```text
chronode/
├── .guide/                 # Guías de arquitectura, flujos y estándares de desarrollo
├── docs/                   # Documentación técnica complementaria del proyecto
├── backend/                # API REST desarrollada con Spring Boot 3 y Java 21
├── frontend/               # Single Page Application con React, TypeScript y Vite
├── docker/                 # Scripts de aprovisionamiento de la base de datos
├── docker-compose.yml      # Definición del contenedor PostgreSQL 16
├── .gitignore              # Reglas de exclusión para Git
└── README.md               # Resumen de la app e instrucciones de ejecución
```

---

## Módulo backend (`/backend`)

El backend sigue una arquitectura desacoplada por capas:

* `src/main/java/com/chronode/`:
  * `config/`: Configuraciones de seguridad con Spring Security (`SecurityConfig`), documentación OpenAPI (`OpenApiConfig`) y auditoría automática (`JpaAuditingConfig`).
  * `controller/`: Controladores REST documentados para recibir peticiones y retornar respuestas HTTP estándar.
  * `dto/`: Objetos de transferencia de datos inmutables definidos mediante Java Records.
  * `entity/`: Entidades JPA preparadas para el mapeo relacional de base de datos.
  * `exception/`: Manejo centralizado de errores (`GlobalExceptionHandler`) mediante el estándar RFC 7807 (`ProblemDetail`).
  * `mapper/`: Interfaces de MapStruct para transformación desacoplada entre entidades y DTOs.
  * `repository/`: Interfaces de Spring Data JPA con consultas acotadas por espacio de trabajo.
  * `service/`: Lógica de negocio y orquestación transaccional.
* `src/main/resources/`:
  * `application.yml`: Configuración de base de datos, Hibernate, dialecto PostgreSQL y Virtual Threads.

---

## Módulo frontend (`/frontend`)

El frontend está estructurado para escalar por características (*features*):

* `src/assets/`: Archivos estáticos como iconos, imágenes y logotipos.
* `src/components/ui/`: Componentes atómicos reutilizables (Button, Card, Badge, Input).
* `src/components/layout/`: Barra lateral de navegación unificada (`Sidebar`) y barra superior (`Navbar`).
* `src/features/`: Módulos de funcionalidad de negocio que se incorporarán progresivamente.
* `src/hooks/`: Hooks de React personalizados y consultas con TanStack Query.
* `src/lib/`: Utilidades generales como el asistente de combinación de clases CSS (`cn`).
* `src/services/`: Configuración del cliente HTTP Axios e interceptores para autenticación JWT y cabecera de espacio de trabajo.
* `src/types/`: Definiciones de tipos e interfaces de TypeScript compartidas.
* `tailwind.config.js`: Tokens de diseño con nomenclatura descriptiva (`brand-blue`, `soft-blue`, `navy-dark`, `sand-gold`, `golden-ochre`, `deep-bronze`, `pure-white`).
