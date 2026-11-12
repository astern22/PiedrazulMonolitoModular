# Épica E5 — Reportes de citas

Generación de reportes gráficos de citas por mes y médico.

## Historias de usuario

### E5-HU01 — Reporte de citas por mes y médico

| Código | Rol | Quiero | Para |
|---|---|---|---|
| `E5-HU01` | Como MEDICO/TERAPISTA | generar un reporte gráfico de cantidad de citas por mes y médico | analizar la cantidad de citas |

#### Criterios de aceptación

| Código | Título | Contexto | Evento | Resultado esperado |
|---|---|---|---|---|
| `E5-HU01-CA01` | Reporte exitoso por obligatoriedad | En caso que inicie sesion como un MEDICO/TERAPISTA, y me encuentre en el menu de reportes | cuando intento generar un reporte de citas medicas, escogiendo un año, mes y medico | el sistema debe mostrar un listado del historial de citas que coincidan con los parametros de busqueda, y mostrar un resumen de la cantidad de citas medicas. |
| `E5-HU01-CA02` | Falla de reporte por obligatoriedad | En caso que inicie sesion como un MEDICO/TERAPISTA, y me encuentre en el menu de reportes | cuando intento generar un reporte de citas medicas dejando en blanco alguno de los parametros obligatorios | el sistema debe informar que los filtros de busqueda son obligatorios y debe impedir que se realice la consulta del reporte |
| `E5-HU01-CA03` | Reporte sin resultados | En caso que inicie sesion como un MEDICO/TERAPISTA, y me encuentre en el menu de reportes | cuando intento generar un reporte de citas medicas, escogiendo un año, mes y medico | el sistema debe informar que no se encuentran coincidendias para los filtros seleccionados y el reporte debe mostrar que la cantidad de citas medicas es cero. |
