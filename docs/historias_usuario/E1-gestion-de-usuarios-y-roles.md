# Épica E1 — Gestión de usuarios y roles

Registro de usuarios en la plataforma y asignación de roles por parte del administrador.

## Historias de usuario

### E1-HU01 — Registro de usuarios

| Código | Rol | Quiero | Para |
|---|---|---|---|
| `E1-HU01` | Como USUARIO | registrarme en la aplicacion web | hacer uso de las funcionalidades segun mi rol |

#### Criterios de aceptación

| Código | Título | Contexto | Evento | Resultado esperado |
|---|---|---|---|---|
| `E1-HU01-CA01` | Registro exitoso | En caso que diligencie todos los campos obligatorios en el formulario de registro | cuando intento completar el registro enviando el formulario | el sistema muestra un mensaje de informacion indicando que el registro fue exitoso |
| `E1-HU01-CA02` | Registro invalido por obligatoriedad | En caso que NO diligencie todos los campos obligatorios en el formulario de registro | cuando intento completar el registro enviando el formulario | el sistema impide el envio de informacion, y mostrará un mensaje indicando que se deben llenar todos campos obligatorios |
| `E1-HU01-CA03` | Registro fallido por datos inválidos | En caso de que el usuario ingrese datos en un formato incorrecto | cuando intento completar el registro enviando el formulario | el sistema muestra un mensaje indicando los errores de los campos. |
| `E1-HU01-CA04` | Registro fallido por usuario existente | En caso de que el correo ingresado ya esté registrado en el sistema | cuando intento completar el registro enviando el formulario | el sistema muestra un mensaje indicando que el correo nombre de usuario ya está en uso. |

### E1-HU02 — Asignación de roles

| Código | Rol | Quiero | Para |
|---|---|---|---|
| `E1-HU02` | Como ADMINISTRADOR | asignar un rol definido a un USUARIO | otorgar diferentes funcionalidades a cada tipo de USUARIO |

#### Criterios de aceptación

| Código | Título | Contexto | Evento | Resultado esperado |
|---|---|---|---|---|
| `E1-HU02-CA01` | Asignacion de rol valida | En caso que se asigne un rol valido a un usuario existente | cuando intento asignar un rol a un usuario especifico | el sistema debe mostrar un mensaje indicando que se ha asignado el rol correspondiente a un usuario especifico |
| `E1-HU02-CA02` | Asignacion de rol incorrecta por inexistencia de usuario | En caso que se asigne un rol valido a un usuario existente | cuando intento asignar un rol a un usuario inexistente | el sistema debe mostrar un mensaje indicando que NO se ha asignado el rol ya que el usuario especifico no existe |
| `E1-HU02-CA03` | Asignacion de rol incorrecta por invalidez de rol | En caso que se asigne un rol invalido a un usuario existente | cuando intento asignar un rol a un usuario especifico | el sistema debe mostrar un mensaje indicando que NO se ha asignado el rol correspondiente a un usuario especifico |
