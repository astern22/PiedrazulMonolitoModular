/**
 * Modulo de validacion para formularios del frontend.
 * Las reglas coinciden con las restricciones del backend (DTOs + DB).
 *
 * Cada validador retorna null si es valido, o un string con el mensaje de error.
 */

// ─── Validadores Genericos ───

export function required(value, fieldName = 'Este campo') {
  if (value === null || value === undefined || String(value).trim() === '') {
    return `${fieldName} es obligatorio.`
  }
  return null
}

export function maxLength(value, max, fieldName = 'Este campo') {
  if (value && String(value).length > max) {
    return `${fieldName} no debe exceder los ${max} caracteres.`
  }
  return null
}

export function minLength(value, min, fieldName = 'Este campo') {
  if (value && String(value).length < min) {
    return `${fieldName} debe tener al menos ${min} caracteres.`
  }
  return null
}

export function minValue(value, min, fieldName = 'Este campo') {
  if (value !== '' && value !== null && value !== undefined && Number(value) < min) {
    return `${fieldName} debe ser al menos ${min}.`
  }
  return null
}

export function maxValue(value, max, fieldName = 'Este campo') {
  if (value !== '' && value !== null && value !== undefined && Number(value) > max) {
    return `${fieldName} no debe ser mayor a ${max}.`
  }
  return null
}

export function isPositiveInteger(value, fieldName = 'Este campo') {
  if (value === '' || value === null || value === undefined) return null
  const num = Number(value)
  if (!Number.isInteger(num) || num <= 0) {
    return `${fieldName} debe ser un numero entero positivo.`
  }
  return null
}

export function isEmail(value) {
  if (!value || String(value).trim() === '') return null
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(String(value).trim())) {
    return 'Ingresa un correo electronico valido.'
  }
  return null
}

export function noSpaces(value, fieldName = 'Este campo') {
  if (value && /\s/.test(String(value))) {
    return `${fieldName} no debe contener espacios.`
  }
  return null
}

export function isTime(value) {
  if (!value) return null
  if (!/^\d{2}:\d{2}(:\d{2})?$/.test(value)) {
    return 'El formato de hora debe ser HH:mm o HH:mm:ss.'
  }
  return null
}

export function isDate(value) {
  if (!value) return null
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return 'El formato de fecha debe ser YYYY-MM-DD.'
  }
  const parsed = new Date(value + 'T00:00:00')
  if (isNaN(parsed.getTime())) {
    return 'La fecha ingresada no es valida.'
  }
  return null
}

export function isFutureOrToday(value) {
  if (!value) return null
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const date = new Date(value + 'T00:00:00')
  if (date < today) {
    return 'La fecha no puede ser anterior al dia de hoy.'
  }
  return null
}

export function isPastDate(value, fieldName = 'La fecha') {
  if (!value) return null
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const date = new Date(value + 'T00:00:00')
  if (date >= today) {
    return `${fieldName} debe ser anterior a hoy.`
  }
  return null
}

export function isTimeRangeValid(startTime, endTime) {
  if (!startTime || !endTime) return null
  const start = startTime.substring(0, 5)
  const end = endTime.substring(0, 5)
  if (start >= end) {
    return 'La hora de inicio debe ser anterior a la hora de fin.'
  }
  return null
}

// ─── Helpers ───

/**
 * Ejecuta un array de validadores y retorna el primer error encontrado o null.
 */
export function firstError(...errors) {
  for (const err of errors) {
    if (err) return err
  }
  return null
}

/**
 * Ejecuta validadores para un formulario completo.
 * Retorna un objeto { field: errorMsg } con los errores encontrados.
 * Si no hay errores, retorna un objeto vacio.
 */
export function validateFields(validations) {
  const errors = {}
  for (const [field, errorMsg] of Object.entries(validations)) {
    if (errorMsg) {
      errors[field] = errorMsg
    }
  }
  return errors
}

export function hasErrors(errors) {
  return Object.keys(errors).length > 0
}

// ─── Validadores por Formulario ───

/**
 * Validacion de RegisterRequest.
 * DB: username VARCHAR(50), password VARCHAR(255), full_name VARCHAR(150), email VARCHAR(120)
 */
export function validateRegisterForm({ username, password, fullName, email, documentNumber }) {
  return validateFields({
    username: firstError(
      required(username, 'El usuario'),
      noSpaces(username, 'El usuario'),
      maxLength(username, 50, 'El usuario')
    ),
    password: firstError(
      required(password, 'La contraseña'),
      minLength(password, 6, 'La contraseña')
    ),
    fullName: firstError(
      required(fullName, 'El nombre completo'),
      maxLength(fullName, 150, 'El nombre completo')
    ),
    email: firstError(
      required(email, 'El correo electronico'),
      isEmail(email),
      maxLength(email, 120, 'El correo electronico')
    ),
    documentNumber: firstError(
      required(documentNumber, 'El numero de documento'),
      noSpaces(documentNumber, 'El numero de documento'),
      maxLength(documentNumber, 20, 'El numero de documento')
    ),
  })
}

/**
 * Validacion de SpecialtyRequest.
 * DB: name VARCHAR(100), DTO: @NotBlank
 */
export function validateSpecialtyForm({ name }) {
  return validateFields({
    name: firstError(
      required(name, 'El nombre de la especialidad'),
      maxLength(name, 100, 'El nombre de la especialidad')
    ),
  })
}

/**
 * Validacion de ProfessionalRequest.
 * DTO: @NotNull userId, @NotNull specialtyId, @NotBlank professionalType, @NotNull @Min(1) appointmentIntervalMinutes
 * DB: professional_type VARCHAR(20)
 */
export function validateProfessionalForm({
  userId,
  specialtyId,
  professionalType,
  appointmentIntervalMinutes,
}) {
  return validateFields({
    userId: firstError(
      required(userId, 'El ID de usuario'),
      isPositiveInteger(userId, 'El ID de usuario')
    ),
    specialtyId: required(specialtyId, 'La especialidad'),
    professionalType: firstError(
      required(professionalType, 'El tipo de profesional'),
      maxLength(professionalType, 20, 'El tipo de profesional')
    ),
    appointmentIntervalMinutes: firstError(
      required(appointmentIntervalMinutes, 'La duracion de cita'),
      minValue(appointmentIntervalMinutes, 1, 'La duracion de cita'),
      maxValue(appointmentIntervalMinutes, 480, 'La duracion de cita')
    ),
  })
}

/**
 * Validacion de WeeklyAvailabilityRequest.
 * DTO: @NotNull professionalId, @NotNull @Min(1) @Max(7) dayOfWeek,
 *      @NotNull startTime, @NotNull endTime
 */
export function validateWeeklyAvailabilityForm({
  professionalId,
  dayOfWeek,
  startTime,
  endTime,
}) {
  return validateFields({
    professionalId: firstError(
      required(professionalId, 'El profesional'),
      isPositiveInteger(professionalId, 'El ID del profesional')
    ),
    dayOfWeek: firstError(
      required(dayOfWeek, 'El dia de la semana'),
      minValue(dayOfWeek, 1, 'El dia de la semana'),
      maxValue(dayOfWeek, 7, 'El dia de la semana')
    ),
    startTime: firstError(
      required(startTime, 'La hora de inicio'),
      isTime(startTime)
    ),
    endTime: firstError(
      required(endTime, 'La hora de fin'),
      isTime(endTime),
      isTimeRangeValid(startTime, endTime)
    ),
  })
}

/**
 * Validacion de AppointmentRequest.
 * DTO: patientId @NotNull, professionalId @NotNull,
 *      appointmentDate @NotNull, startTime @NotNull, endTime @NotNull
 */
export function validateAppointmentForm({
  patientId,
  professionalId,
  appointmentDate,
  startTime,
  endTime,
}) {
  return validateFields({
    patientId: firstError(
      required(patientId, 'El ID del paciente'),
      isPositiveInteger(patientId, 'El ID del paciente')
    ),
    professionalId: required(professionalId, 'El profesional'),
    appointmentDate: firstError(
      required(appointmentDate, 'La fecha'),
      isDate(appointmentDate),
      isFutureOrToday(appointmentDate)
    ),
    startTime: firstError(
      required(startTime, 'La hora de inicio'),
      isTime(startTime)
    ),
    endTime: firstError(
      required(endTime, 'La hora de fin'),
      isTime(endTime),
      isTimeRangeValid(startTime, endTime)
    ),
  })
}

/**
 * Validacion de PatientRequest.
 * DB: document_number VARCHAR(20) NOT NULL UNIQUE, phone VARCHAR(20), birth_date DATE, user_id BIGINT UNIQUE
 */
export function validatePatientForm({
  documentNumber,
  phone,
  birthDate,
  userId,
}) {
  return validateFields({
    documentNumber: firstError(
      required(documentNumber, 'El numero de documento'),
      noSpaces(documentNumber, 'El numero de documento'),
      maxLength(documentNumber, 20, 'El numero de documento')
    ),
    phone: firstError(
      maxLength(phone, 20, 'El telefono')
    ),
    birthDate: firstError(
      birthDate ? isDate(birthDate) : null,
      birthDate ? isPastDate(birthDate, 'La fecha de nacimiento') : null
    ),
    userId: firstError(
      userId !== '' && userId !== null && userId !== undefined
        ? isPositiveInteger(userId, 'El ID de usuario')
        : null
    ),
  })
}

