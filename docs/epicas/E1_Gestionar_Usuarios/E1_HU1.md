# E1-HU1 Registro de nuevo usuario

**Como** visitante del sitio web,

**Necesito** crear una cuenta proporcionando mis datos personales,

**Para** acceder a los servicios ofrecidos por el centro clinico Piedra Azul

---

#### Reglas de negocio

1. El numero de identidad debe ser unico en el sistema. No se permiten dos cuentas de usuario con el mismo numero de documento de identidad.
2. El usuario debe ser mayor de 18 años.
3. Datos obligatorios: Nombre, Apellido, Numero telefonico, Contraseña, Fecha de nacimiento.

---

#### Criterios de Aceptacion

**Escenario 1:** Registro exitoso con datos validos

```gherkin
Dado que estoy en la pagina de "Registro"

Cuando ingreso un nombre, apellido, fecha de nacimiento valida

Y un numero de identidad no registrado previamente

Y me intento registrar

Entonces el sistema debe mostrar un mensaje que indique que el registro fue correcto

Y redirigir al usuario a la pagina de inicio de sesion
```

**Escenario 2:** Intento de registro con numero de identidad ya existente

```gherkin
Dado que ya existe una cuenta activa con el numero de identidad "1234"

Cuando intento registrarme con ese mismo numero de identidad

Y completo el registro de los campos obligatorios

Y me intento registrar

Entonces el sistema debe informar que ya existe un usuario activo con el mismo numero de identidad, y no crear el nuevo registro.
```

**Escenario 3:** Usuario menor de edad

```gherkin
Dado que estoy en el formulario de registro

Cuando ingrese una fecha de nacimiento que resulta en una edad menor a 18 años

Y completo el registro de los campos obligatorios

Y me intento registrar

Entonces el sistema impide el envio del formulario e indicar que el usuario debe ser mayor de 18 años para poder registrarse.
```

**Escenario 4:** Campos obligatorios faltantes

```gherkin
Dado que estoy en el formulario de registro

Cuando dejo vacio uno o mas campos obligatorios

Y me intento registrar

Entonces el sistema debe mostar indicadores visuales en los campos que faltan por completar y el boton de registro debe permanecer deshabilitado
```

---

####  Notas Tecnicas

---
