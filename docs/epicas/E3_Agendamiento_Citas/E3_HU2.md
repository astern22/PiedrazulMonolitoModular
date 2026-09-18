# E3-HU2 Agendamiento de cita

**Como** paciente,

**Necesito** agendar una cita mediante la web,

**Para** tener una cita de manera sencilla y rápida sin tener que usar WhatsApp

---

#### Reglas de negocio

1. Un paciente no puede agendar dos o mas citas en un mismo horario
2. Datos obligatorios: telefono, fecha, hora, motivo de solicitud.

---

#### Criterios de Aceptacion

**Escenario 1:** Agendamiento de cita exitoso

```gherkin
Dado que inicie sesion en la aplicacion como un paciente y me encuentro en la pagina "Agendar cita"

Cuando ingreso todos los campos obligatorios

Y trato de agendar una cita

Entonces el sistema debe indicar que el agendamiento fue exitoso, y mostrar la informacion completa de la cita medica.
```

**Escenario 2:** Nombre del escenario

```gherkin
Dado que inicie sesion en la aplicacion como un paciente y me encuentro en la pagina "Agendar cita"

Cuando dejo campos obligatorios vacios

Y trato de agendar una cita

Entonces el sistema debe impedir que se realice el agendamiento, y debe resaltar los campos que hacen falta.
```

---

#### Notas Tecnicas

Notas tecnicas

---