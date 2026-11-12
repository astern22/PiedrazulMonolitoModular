# Épica E3 — Gestión de citas médicas

Configuración de la disponibilidad médica, agendamiento autónomo y cancelación de citas.

## Historias de usuario

### E3-HU01 — Configuración de parámetros de agendamiento

| Código | Rol | Quiero | Para |
|---|---|---|---|
| `E3-HU01` | Como ADMINISTRADOR | configurar los parámetros del sistema | que el agendamiento de citas autónomo funcione acorde a la disponibilidad de los médicos y terapistas de Piedrazul |

#### Criterios de aceptación

| Código | Título | Contexto | Evento | Resultado esperado |
|---|---|---|---|---|
| `E3-HU01-CA01` | Configuracion exitosa | En caso que el administrador seleccione un médico/terapista activo y diligencie todos los parámetros obligatorios (ventana en semanas, días de atención, franja horaria, intervalo entre citas) | cuando guarda la configuración | el sistema almacena los parámetros, muestra un mensaje de éxito y los deja disponibles para el algoritmo de agendamiento autónomo |
| `E3-HU01-CA02` | Falla por campos obligatorios | En caso que el administrador NO diligencie todos los campos obligatorios de la configuración | cuando intenta guardar la configuración | el sistema impide el guardado y muestra un mensaje indicando los campos faltantes |
| `E3-HU01-CA03` | Falla por valores fuera de rango | En caso que el administrador ingrese un intervalo entre citas menor o igual a 0 minutos, o una ventana de agendamiento menor o igual a 0 semanas | cuando intenta guardar la configuración | el sistema rechaza la configuración y muestra un mensaje de validación indicando el rango permitido |
| `E3-HU01-CA04` | Falla por profesional inactivo | En caso que el administrador intente configurar parámetros para un médico/terapista en estado inactivo | cuando intenta guardar la configuración | el sistema impide la configuración y muestra un mensaje indicando que el profesional no se encuentra activo |

### E3-HU02 — Agendamiento de citas

| Código | Rol | Quiero | Para |
|---|---|---|---|
| `E3-HU02` | Como PACIENTE | agendar una cita mediante la web | tener una cita de manera sencilla y rapida sin tener que usar WhatsApp |

#### Criterios de aceptación

| Código | Título | Contexto | Evento | Resultado esperado |
|---|---|---|---|---|
| `E3-HU02-CA01` | Agendamiento correcto por obligatoriedad | En caso que inicie sesion de usuario como paciente, y me encuentre en el menu principal | cuando intento agendar una cita medica | el sistema debe informar que la cita quedo agendada correctamente |
| `E3-HU02-CA02` | Falla por campos obligatorios | En caso que el paciente autenticado NO seleccione un médico/terapista y/o una fecha/hora disponible | cuando intenta agendar la cita | el sistema impide el envío de la solicitud y muestra un mensaje indicando los campos obligatorios faltantes |
| `E3-HU02-CA03` | Falla por horario no disponible (concurrencia) | En caso que el horario seleccionado por el paciente haya sido tomado por otro paciente entre la consulta de disponibilidad y el envío de la solicitud | cuando intenta confirmar el agendamiento | el sistema rechaza la solicitud, informa que el horario ya no está disponible y sugiere seleccionar otra opción |
| `E3-HU02-CA04` | Falla por profesional sin disponibilidad configurada | En caso que el médico/terapista seleccionado esté inactivo o no tenga parámetros de agendamiento configurados | cuando el paciente intenta agendar una cita con dicho profesional | el sistema no ofrece a ese profesional como opción disponible y/o muestra un mensaje indicando que no hay disponibilidad |

### E3-HU03 — Cancelación de citas

| Código | Rol | Quiero | Para |
|---|---|---|---|
| `E3-HU03` | Como PACIENTE | cancelar una cita médica que agendé con anterioridad | liberar ese espacio a otro paciente. |

#### Criterios de aceptación

| Código | Título | Contexto | Evento | Resultado esperado |
|---|---|---|---|---|
| `E3-HU03-CA01` | Cancelacion de cita correcta por obligatoriedad | En caso que inicie sesion de usuario como PACIENTE, y me encuentre en el menu principal | Cuando intento cancelar una cita medica con al menos dos dias de anticipacion, y con la justificacion diligenciada | El sistema debe informar que la cita quedo cancelada correctamente. |
| `E3-HU03-CA02` | Falla por cambios obligatorios | En caso que el paciente autenticado NO diligencie la justificacion de la cancelacion de la cita medica | Cuando intenta cancelar la cita | El sistema debe impedir la cancelacion de la cita medica y mostrar un mensaje indicando que la justificacion es obligatoria |
| `E3-HU03-CA03` | Falla por anticipacion invalida | En caso que el plazo de la cita es menor a dos dias | Cuando intenta cancelar la cita | El sistema debe impedir la cancelacion de la cita medica y mostrar un mensaje indicando que ya no se puede cancelar la cita en los plazos actuales |
