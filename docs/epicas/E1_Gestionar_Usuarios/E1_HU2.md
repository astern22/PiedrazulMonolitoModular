# E1-HU2 Asignacion de rol 

**Como** administrador,

**Necesito** asignar un rol definido a un usuario registrado en el sistema,

**Para** darle funcionalidades que le permitan hacer uso de los servicios del centro medico Piedra Azul de acuerdo a su rol.

---

#### Reglas de negocio

1. Un usuario con rol CLIENTE no puede tener ningun otro rol asignado.
2. Un usuario con rol MEDICO/TERAPISTA puede tener tambien el rol de AGENDADOR y viceversa.
3. Un usuario con rol ADMINISTRADOR no puede tener nigun otro rol asignado.

---

#### Criterios de Aceptacion

**Escenario 1:** Asignacion de rol exitosa

```gherkin
Dado que contexto inicial

Cuando accion realizada

Y otra accion

Entonces resultado esperado
```

---

#### Notas Tecnicas

Notas tecnicas

---