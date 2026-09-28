/**
 * Utilidades para mostrar profesionales por nombre (nunca por ID interno).
 */

/** Nombre visible del profesional; usa el ID solo como ultimo recurso. */
export function professionalName(prof) {
  if (!prof) return ''
  return prof.fullName || `Profesional #${prof.id}`
}

/** Etiqueta para listas desplegables: "Nombre (Especialidad - 30 min)". */
export function professionalLabel(prof, { withSpecialty = true, withInterval = false } = {}) {
  if (!prof) return ''
  const details = []
  if (withSpecialty && prof.specialtyName) details.push(prof.specialtyName)
  if (withInterval && prof.appointmentIntervalMinutes) {
    details.push(`${prof.appointmentIntervalMinutes} min`)
  }
  const base = professionalName(prof)
  return details.length ? `${base} (${details.join(' - ')})` : base
}

/** Busca el nombre de un profesional en una lista a partir de su ID. */
export function professionalNameById(list, id) {
  const found = (list || []).find(p => String(p.id) === String(id))
  return found ? professionalName(found) : `Profesional #${id}`
}
