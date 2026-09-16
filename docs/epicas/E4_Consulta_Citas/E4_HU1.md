# E4-HU1 Listado de citas medicas asignadas a un profesional en fecha determinada

**Como** agendador de citas,

**Necesito** listar las citas médicas de un determinado médico/terapista
en una fecha determinada,

**Para** visualizar el listado y la cantidad de citas.

---

#### Reglas de negocio

1. No pueden existir dos o mas citas agendadas a un mismo Medico/Especialista en el mismo horario.

---

#### Criterios de Aceptacion

**Escenario 1:** Listado de citas agendadas cuando existen registros

```gherkin
Dado que inicie sesion en la aplicacion como un usuario con rol de AGENDADOR y me encuentro en la pagina "Listar citas medicas"

Cuando selecciono al medico/terapista

Y una fecha determinada

Entonces el sistema debe mostrarme la lista de citas agendadas para el profesional medico, que se encuentren dentro del rango de la fecha determinada, y tambien mostrar la cantidad total de registros encontrados.
```

**Escenario 2:** Listado de citas cuando no existen registros

```gherkin
Dado que inicie sesion en la aplicacion como un usuario con rol de AGENDADOR y me encuentro en la pagina "Listar citas medicas"

Cuando selecciono un medico/terapista que no tenga citas agendadas, o una fecha donde no existen citas agendadas

Entonces el sistema debe indicar que no se encontaron registros de citas agendadas para los filtros de listado indicados.
```

---

#### Notas Tecnicas

Notas tecnicas

---