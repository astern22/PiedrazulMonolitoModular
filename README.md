# Profesionales y especialidades

Esta rama reúne funcionalidades de gestión de profesionales de salud y sus especialidades dentro de Piedrazul, incluyendo la creación y consulta de estos registros.

## Tecnologías

- Backend: Java 21, Spring Boot y PostgreSQL.
- Frontend: Vue 3 y Vite.

## Ejecución

Configura PostgreSQL según `src/main/resources/application.properties`. Inicia el backend desde la raíz con `.\mvnw.cmd spring-boot:run`; en otra terminal ejecuta `cd frontend`, `npm install` y `npm run dev`.

La API se sirve en `http://localhost:8080` y el frontend de desarrollo en `http://localhost:5173`.
