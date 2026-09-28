import apiClient from './client.js'

/**
 * Servicio para la gestion de Profesionales de la Salud (/api/professionals)
 * Corresponde a ProfessionalController
 */
export const professionalService = {
  /**
   * Registra un nuevo profesional de la salud.
   * @param {Object} data
   * @param {number} data.userId - ID del usuario asociado
   * @param {number} data.specialtyId - ID de la especialidad
   * @param {string} data.professionalType - Tipo de profesional (ej. MEDICO_GENERAL, ESPECIALISTA)
   * @param {number} data.appointmentIntervalMinutes - Duracion de cita en minutos (ej. 20, 30)
   * @returns {Promise<Object>}
   */
  async create({ userId, specialtyId, professionalType, appointmentIntervalMinutes }) {
    const response = await apiClient.post('/api/professionals', {
      userId: Number(userId),
      specialtyId: Number(specialtyId),
      professionalType,
      appointmentIntervalMinutes: Number(appointmentIntervalMinutes),
    })
    return response.data
  },

  /**
   * Registra un profesional junto con su cuenta de usuario.
   * @param {Object} data
   * @param {string} data.fullName - Nombre completo
   * @param {string} data.username - Nombre de usuario para iniciar sesion
   * @param {string} data.email - Correo electronico
   * @param {string} data.password - Contrasena inicial
   * @param {number} data.specialtyId - ID de la especialidad seleccionada
   * @param {string} data.professionalType - MEDICO o TERAPEUTA
   * @param {number} data.appointmentIntervalMinutes - Duracion de cita en minutos
   * @returns {Promise<Object>}
   */
  async register({
    fullName,
    username,
    email,
    password,
    specialtyId,
    professionalType,
    appointmentIntervalMinutes,
  }) {
    const response = await apiClient.post('/api/professionals/register', {
      fullName: fullName.trim(),
      username: username.trim(),
      email: email.trim(),
      password,
      specialtyId: Number(specialtyId),
      professionalType,
      appointmentIntervalMinutes: Number(appointmentIntervalMinutes),
    })
    return response.data
  },

  /**
   * Cambia la duracion de las citas de un profesional.
   * @param {number|string} id - ID del profesional
   * @param {number} minutes - Nueva duracion en minutos (5 a 480)
   * @returns {Promise<Object>}
   */
  async updateAppointmentInterval(id, minutes) {
    const response = await apiClient.patch(`/api/professionals/${id}/appointment-interval`, {
      appointmentIntervalMinutes: Number(minutes),
    })
    return response.data
  },

  /**
   * Obtiene la lista completa de todos los profesionales.
   * @returns {Promise<Array<Object>>}
   */
  async getAll() {
    const response = await apiClient.get('/api/professionals')
    return response.data
  },

  /**
   * Obtiene la lista de profesionales con estado activo.
   * @returns {Promise<Array<Object>>}
   */
  async getActive() {
    const response = await apiClient.get('/api/professionals/active')
    return response.data
  },

  /**
   * Obtiene los datos de un profesional por su ID.
   * @param {number|string} id - ID del profesional
   * @returns {Promise<Object>}
   */
  async getById(id) {
    const response = await apiClient.get(`/api/professionals/${id}`)
    return response.data
  },

  /**
   * Obtiene los profesionales pertenecientes a una especialidad.
   * @param {number|string} specialtyId - ID de la especialidad
   * @returns {Promise<Array<Object>>}
   */
  async getBySpecialty(specialtyId) {
    const response = await apiClient.get(`/api/professionals/specialty/${specialtyId}`)
    return response.data
  },
}

export default professionalService