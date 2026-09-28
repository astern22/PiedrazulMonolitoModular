# Usuarios y roles

Esta rama desarrolla el registro y acceso de usuarios de Piedrazul. El registro puede vincular una cuenta con un paciente existente mediante su número de documento; el acceso se controla mediante roles.

## Tecnologías

- Backend: Java 21, Spring Boot, Spring Security y PostgreSQL.
- Frontend: Vue 3 y Vite.

## Ejecución

Configura PostgreSQL según `src/main/resources/application.properties`. Inicia el backend desde la raíz con `.\mvnw.cmd spring-boot:run`; en otra terminal ejecuta `cd frontend`, `npm install` y `npm run dev`.

La API se sirve en `http://localhost:8080` y el frontend de desarrollo en `http://localhost:5173`.
