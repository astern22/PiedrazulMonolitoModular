# Pruebas de Piedrazul

Esta rama contiene pruebas automatizadas para validar los módulos y flujos del sistema Piedrazul, incluyendo lógica de negocio, controladores y seguridad.

## Tecnologías

- Java 21, Spring Boot y Maven Wrapper.
- PostgreSQL para las pruebas que requieren base de datos.

## Ejecutar las pruebas

Desde la raíz del repositorio:

```powershell
.\mvnw.cmd test
```

La configuración de pruebas está en `src/test/resources/application.properties`. Consulta también `docs/` para los requisitos e información de arquitectura.
