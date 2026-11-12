# Épica E2 — Autenticación

Acceso de los usuarios a la aplicación mediante el inicio de sesión.

## Historias de usuario

### E2-HU01 — Inicio de sesión

| Código | Rol | Quiero | Para |
|---|---|---|---|
| `E2-HU01` | Como USUARIO | iniciar sesion | ingresar a la aplicacion web |

#### Criterios de aceptación

| Código | Título | Contexto | Evento | Resultado esperado |
|---|---|---|---|---|
| `E2-HU01-CA01` | Inicio de sesion valido por obligatoriedad | En caso que diligencie todos los campos obligatorios en el formulario de inicio de sesion | cuando intento iniciar sesion | el sistema lleva al usuario al menú principal |
| `E2-HU01-CA02` | Inicio de sesion incorrecto por invalidez de datos | En caso que diligencie todos los campos obligatorios en el formulario de inicio de sesion | cuando intento iniciar sesion | el sistema muestra un mensaje indicando que las credenciales que se usaron son invalidas |
| `E2-HU01-CA03` | Inicio de sesion incorrecto por obligatoriedad | En caso que NO diligencie todos los campos obligatorios en el formulario de inicio de sesion | cuando intento iniciar sesion | el sistema impide la solicitud de inicio de sesion y muestra un mensaje indicando que se deben llenar todos los campos obligatorios |
