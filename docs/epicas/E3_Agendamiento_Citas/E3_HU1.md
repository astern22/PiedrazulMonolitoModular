# E3-HU1 Configuracion de parametros del sistema para permitir agendamiento de citas a pacientes

**Como** un administrador,

**Necesito** configurar los parametros del sistema,

**Para** que el agendamiento de citas autonomo funcione acorde a la disponibilidad de los medicos y terapistas de Piedrazul.

---

#### Reglas de negocio

1. Cada medico/terapista tiene una cantidad de dias a la semana para atenciones a pacientes.
2. Cada medico/terapista tiene una franja horaria.
3. Cada medico/terapista tiene un intervalo de tiempo entre cita y cita.
4. El sistema debe tener una ventana de tiempo en que se habilitaran las citas, en semanas.
5. Datos obligatorios: ventana de tiempo para asignacion de cita, Numero de dias a la semana de atencion del medico/especialista, franja horaria del medico/especialista, intervalo de tiempo entre citas en minutos.

---

#### Criterios de Aceptacion

**Escenario 1:** Configuracion de parametros para agendamiento correcta

```gherkin
Dado que inicie sesion en la aplicacion como administrador y me encuentro en la pagina de "Configuracion de parametros"

Cuando ingreso todos los campos obligatorios

Y trato de confirmar la configuracion de parametros

Entonces el sistema debe informar que la configuracion de parametros ha sido correcta.
```

**Escenario 2:** Configuracion de parametros para agendamiento incompleta

```gherkin
Dado que inicie sesion en la aplicacion como administrador y me encuentro en la pagina de "Configuracion de parametros"

Cuando dejo algunos campos obligatorios vacios

Y trato de confirmar la configuracion de parametros

Entonces el sistema debe impedir que se confirme la configuracion, y resaltar los campos que hacen falta por completar.
```

**Escenario 3:** Configuracion de parametros para agendamiento invalida

```gherkin
Dado que inicie sesion en la aplicacion como administrador y me encuentro en la pagina de "Configuracion de parametros"

Cuando ingreso valores invalidos

Y trato de confirmar la configuracion de parametros

Entonces el sistema debe impedir que se confirme la configuracion, e informar que se han introducido datos que no cumplen con las reglas del negocio.
```

---

#### Notas Tecnicas

Notas tecnicas

---