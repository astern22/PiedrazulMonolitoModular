# Planeación semanal

Esta rama incorpora la planeación semanal de Piedrazul para organizar la disponibilidad de los profesionales y apoyar la programación de citas.

## Tecnologías

- Backend: Java 21, Spring Boot y PostgreSQL.
- Frontend: Vue 3 y Vite.

## Ejecución

Configura PostgreSQL según `src/main/resources/application.properties`. Inicia el backend desde la raíz con `.\mvnw.cmd spring-boot:run`; en otra terminal ejecuta `cd frontend`, `npm install` y `npm run dev`.

La API se sirve en `http://localhost:8080` y el frontend de desarrollo en `http://localhost:5173`.
