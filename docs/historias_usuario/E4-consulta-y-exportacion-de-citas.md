# Épica E4 — Consulta y exportación de citas

Listado, consulta por cédula y exportación de las citas médicas registradas.

## Historias de usuario

### E4-HU01 — Listado de citas por médico y fecha

| Código | Rol | Quiero | Para |
|---|---|---|---|
| `E4-HU01` | Como AGENDADOR DE CITAS | listar las citas médicas de un determinado medico/terapista en una fecha determinada | visualizar el listado y la cantidad de citas |

#### Criterios de aceptación

| Código | Título | Contexto | Evento | Resultado esperado |
|---|---|---|---|---|
| `E4-HU01-CA01` | Listado de citas correcto | En caso que inicie sesión de usuario como agendador de citas, y me encuentre en el menú principal | cuando selecciono la opción de listar citas y diligenciar una fecha y un médico/terapista determinado | el sistema debe mostrar el listado de citas agendadas para dicha fecha, y la cantidad total de citas |

### E4-HU02 — Exportación de citas a CSV

| Código | Rol | Quiero | Para |
|---|---|---|---|
| `E4-HU02` | Como MEDICO/AGENDADOR/TERAPISTA | exportar las citas correspondientes a una fecha específica y a un médico o terapista determinado, en un formato de texto compatible con hojas de cálculo CSV | que durante la jornada de atención haya un listado que permita organizar los pacientes y su ingreso a la consulta médica. |

#### Criterios de aceptación

| Código | Título | Contexto | Evento | Resultado esperado |
|---|---|---|---|---|
| `E4-HU02-CA01` | Exportación exitosa con filtros válidos | El usuario (médico, agendador o terapista) selecciona una fecha válida que cuenta con citas programadas y un médico/terapista existente con agenda activa, | cuando haga clic en el botón "Exportar a CSV", | el sistema genere y descargue correctamente un archivo de texto en formato .csv con la lista detallada de los pacientes, horas y estados, listo para ser abierto en hojas de cálculo. |
| `E4-HU02-CA02` | Sin registros para los filtros seleccionados | El usuario selecciona una fecha y/o un médico/terapista que no tiene citas agendadas, | cuando intente realizar la exportación, | el sistema muestre una alerta o mensaje en pantalla indicando que “No se encontraron registros de citas para los filtros seleccionados” y evite la descarga de un archivo vacío. |
| `E4-HU02-CA03` | Validación de campos obligatorios incompletos | El usuario deja sin seleccionar la fecha o el médico/terapista en los filtros de búsqueda, | cuando intente hacer clic en el botón de exportación, | el sistema bloquee la acción, resalte visualmente los campos faltantes y muestre un mensaje de advertencia indicando que son obligatorios. |

### E4-HU03 — Consulta de citas por cédula

| Código | Rol | Quiero | Para |
|---|---|---|---|
| `E4-HU03` | Como MEDICO/AGENDADOR/TERAPISTA | consultar las citas medicas de un paciente registrado en el sistema a partir de su cédula | conocer el historial de citas medicas que ha tenido |

#### Criterios de aceptación

| Código | Título | Contexto | Evento | Resultado esperado |
|---|---|---|---|---|
| `E4-HU03-CA01` | Consulta exitosa con historial existente | El usuario (médico, agendador o terapista) ingresa el número de cédula de un paciente válido y registrado en el sistema que posee registros previos, | haga clic en el botón "Buscar / Consultar", | busque en la base de datos y despliegue un listado cronológico con el historial completo de citas médicas (fecha, hora, especialista, especialidad y estado: atendida, cancelada, inasistencia, etc.). |
| `E4-HU03-CA02` | Paciente registrado sin citas previas | El usuario ingresa la cédula de un paciente registrado que no tiene historial de citas agendadas ni atendidas, | intente realizar la búsqueda, | muestre la información básica del paciente junto con un mensaje indicando “El paciente no registra citas médicas en el historial”. |
| `E4-HU03-CA03` | Búsqueda de cédula no registrada en el sistema | El usuario ingresa un número de cédula que no corresponde a ningún paciente existente en la base de datos del centro médico, | intente ejecutar la consulta, | notifique al usuario mediante un mensaje de advertencia indicando “No se encontró ningún paciente registrado con la cédula ingresada” y sugiera la opción de registro. |
| `E4-HU03-CA04` | Validación de formato de documento inválido | El usuario ingresa caracteres alfabéticos, símbolos o deja el campo de la cédula en blanco, | presione el botón de búsqueda, | bloquee la solicitud, resalte el campo en rojo y despliegue una alerta indicando que el campo es obligatorio y solo acepta caracteres numéricos válidos. |
