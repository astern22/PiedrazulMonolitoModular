/**
 * Utilidades para mostrar profesionales por nombre (nunca por ID interno).
 */

/** Tipos de profesional que existen en el centro de medicina alternativa. */
export const PROFESSIONAL_TYPES = [
  { value: 'MEDICO', label: 'Medico' },
  { value: 'TERAPEUTA', label: 'Terapeuta' },
]

// Valores antiguos que pudieron quedar registrados en la base de datos.
const LEGACY_TYPE_LABELS = {
  MEDICO_GENERAL: 'Medico General',
  ESPECIALISTA: 'Especialista',
  ODONTOLOGO: 'Odontologo',
  PSICOLOGO: 'Psicologo',
  PEDIATRA: 'Pediatra',
}

export function professionalTypeLabel(type) {
  const known = PROFESSIONAL_TYPES.find(t => t.value === type)
  return known ? known.label : LEGACY_TYPE_LABELS[type] || type || ''
}

/** Nombre visible del profesional; usa el ID solo como ultimo recurso. */
export function professionalName(prof) {
  if (!prof) return ''
  return prof.fullName || `Profesional #${prof.id}`
}

/** Etiqueta para listas desplegables: "Nombre (Tipo - 30 min)". */
export function professionalLabel(prof, { withType = true, withInterval = false } = {}) {
  if (!prof) return ''
  const details = []
  if (withType && prof.professionalType) details.push(professionalTypeLabel(prof.professionalType))
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
