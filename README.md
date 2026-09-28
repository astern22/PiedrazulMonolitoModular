# Frontend de Piedrazul

Esta rama contiene la aplicación web de Piedrazul, desarrollada con Vue 3 y Vite. Incluye gestión de citas, búsqueda de profesionales, edición de duración de citas, validación de cruces de horario y registro de profesionales.

## Requisitos

Node.js 22.18 o superior y npm. Para utilizar las funciones conectadas a datos, inicia también el backend Spring Boot en `http://localhost:8080`.

## Ejecución

```sh
cd frontend
npm install
npm run dev
```

Vite inicia el sitio, por defecto, en `http://localhost:5173` y redirige las solicitudes `/api` y `/auth` al backend local. Para generar una compilación de producción, ejecuta `npm run build` desde `frontend`.
