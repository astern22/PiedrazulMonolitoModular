# Piedrazul

Sistema web para gestionar pacientes, profesionales, especialidades, disponibilidad y citas médicas. El proyecto está organizado como un monolito modular, con API REST y control de acceso por roles.

## Tecnologías

- Backend: Java 21, Spring Boot y PostgreSQL.
- Frontend: Vue 3, Vite y Node.js 22.18 o superior.

## Requisitos

Instala Java 21, Node.js, PostgreSQL y npm. Antes de iniciar la aplicación, configura PostgreSQL con la base de datos y el esquema que espera el backend (`piedrazuldb` y `piedrazul`) y revisa las credenciales en `src/main/resources/application.properties`.

> Los scripts de `DB/` pueden borrar y recrear bases de datos o esquemas. Revísalos y respalda los datos antes de ejecutarlos.

## Ejecución local

Desde la raíz del proyecto, inicia el backend:

```powershell
.\mvnw.cmd spring-boot:run
```

En otra terminal, inicia el frontend:

```powershell
cd frontend
npm install
npm run dev
```

Abre la URL que indique Vite (por defecto `http://localhost:5173`). La API corre en `http://localhost:8080`; en desarrollo, Vite redirige las solicitudes `/api` y `/auth` al backend.

Para ejecutar las pruebas del backend desde la raíz:

```powershell
.\mvnw.cmd test
```

Para generar el frontend de producción:

```powershell
cd frontend
npm run build
```

## Documentación

- [Documento de requisitos IEEE 830](docs/ERS_PiedraAzul_IEEE830_2026-II_FINAL.docx)
- [Documento de arquitectura](docs/Documento-Piedra-azul-documento-arquitectura_corte1.docx)
- [Diagramas de arquitectura C4](docs/c4/README.md)
- [Historias de usuario](docs/epicas/)
