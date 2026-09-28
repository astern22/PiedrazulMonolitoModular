# Agendamiento de citas

Esta rama desarrolla el agendamiento de citas médicas en Piedrazul, incluida la definición de franjas de disponibilidad para profesionales y la consulta de horarios disponibles.

## Tecnologías

- Backend: Java 21, Spring Boot y PostgreSQL.
- Frontend: Vue 3 y Vite.

## Ejecución

Configura PostgreSQL según `src/main/resources/application.properties`. Inicia el backend desde la raíz con `.\mvnw.cmd spring-boot:run`; en otra terminal ejecuta `cd frontend`, `npm install` y `npm run dev`.

La API se sirve en `http://localhost:8080` y el frontend de desarrollo en `http://localhost:5173`.
