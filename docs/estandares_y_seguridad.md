# Estándares de desarrollo y seguridad

Este documento resume los principios de seguridad, arquitectura y buenas prácticas obligatorias para el desarrollo en Chronode.

---

## Seguridad y autorización

* **Autenticación sin estado (Stateless JWT):** La aplicación no almacena sesiones en el servidor Tomcat. Las solicitudes viajan con la cabecera `Authorization: Bearer <token>`.
* **Cifrado de contraseñas:** Uso obligatorio del algoritmo `BCryptPasswordEncoder`.
* **Aislamiento por espacio de trabajo (*Tenant isolation*):**
  * Todas las consultas de lectura y operaciones de escritura en niveles dinámicos, etiquetas y registros de tiempo deben estar acotadas al `workspaceId`.
  * La capa de servicio debe verificar la titularidad del espacio de trabajo respecto al usuario autenticado para evitar accesos cruzados (*BOLA*).
* **Cabeceras HTTP autorizadas en CORS:** `Authorization`, `Content-Type`, `X-Workspace-Id`.

---

## Manejo de errores y respuestas API

* **Formato estándar RFC 7807 (`ProblemDetail`):** Todas las respuestas de error utilizan una estructura uniforme sin exponer trazas internas de la pila ni detalles de la base de datos al cliente.
* **Controlador global:** Gestionado a través de la clase `GlobalExceptionHandler` con anotación `@RestControllerAdvice`.

---

## Estándares de calidad y redacción

* **Desarrollo paso a paso:** Implementación incremental guiada por casos de uso concretos (DTO -> Entidad/Repositorio -> Servicio/Tests -> Controlador -> Frontend).
* **Estilo de títulos (Sentence case):** Todos los documentos y encabezados se escriben utilizando mayúscula únicamente en la primera palabra del título y en nombres propios o tecnologías.
