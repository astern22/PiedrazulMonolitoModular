# E2-HU1 Inicio de sesion como usuario

**Como** un usuario registrado,

**Necesito** iniciar sesion en la aplicacion web,

**Para** acceder a las funcionalidades que se me permiten segun mi rol en la entidad medica.

---

#### Reglas de negocio

1. Se debe verificar que el usuario registrado tenga su cuenta activa.
2. Datos obligatorios: numero de identidad, contraseña.

---

#### Criterios de Aceptacion

**Escenario 1:** Inicio de sesion exitoso

```gherkin
Dado que estoy en la pagina de "Iniciar Sesion" y ya he realizado un registro de usuario previo

Cuando ingreso mi numero de identidad y mi contraseña

Y trato de iniciar sesion

Entonces el sistema debe redirigirme a la pagina principal de la aplicacion y mostrarme opciones definidas por mi rol
```

**Escenario 2:** Intento de inicio de sesion con credenciales incorrectas

```gherkin
Dado que estoy en la pagina de "Iniciar Sesion" y ya he realizado un registro de usuario previo

Cuando ingreso un numero de identidad o una contraseña incorrectas

Y trato de iniciar sesion

Entonces el sistema debe informar que las credenciales ingresadas no coinciden con un usuario activo registrado en la aplicacion, y debe impedir el acceso a la pagina principal.
```

---

#### Notas Tecnicas

Notas tecnicas

---