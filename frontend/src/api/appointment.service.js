import apiClient from './client.js'

/**
 * Servicio para la gestion de Citas Medicas (/api/appointments)
 */
export const appointmentService = {
  /**
   * Crea una nueva cita medica.
   * @param {Object} data
   * @param {number} data.patientId - ID del paciente
   * @param {number} data.professionalId - ID del profesional medico
   * @param {string} data.appointmentDate - Fecha de la cita (formato YYYY-MM-DD)
   * @param {string} data.startTime - Hora de inicio (formato HH:mm o HH:mm:ss)
   * @param {string} data.endTime - Hora de finalizacion (formato HH:mm o HH:mm:ss)
   * @returns {Promise<Object>} Cita creada con su estado
   */
  async create({ patientId, professionalId, appointmentDate, startTime, endTime }) {
    const response = await apiClient.post('/api/appointments/create', {
      patientId: Number(patientId),
      professionalId: Number(professionalId),
      appointmentDate,
      startTime: startTime.length === 5 ? `${startTime}:00` : startTime,
      endTime: endTime.length === 5 ? `${endTime}:00` : endTime,
    })
    return response.data
  },

  /**
   * Obtiene la lista de todas las citas registradas.
   * @returns {Promise<Array<Object>>}
   */
  async getAll() {
    const response = await apiClient.get('/api/appointments')
    return response.data
  },

  /**
   * Obtiene los detalles de una cita por su identificador.
   * @param {number|string} id - Identificador de la cita
   * @returns {Promise<Object>}
   */
  async getById(id) {
    const response = await apiClient.get(`/api/appointments/${id}`)
    return response.data
  },

  /**
   * Obtiene la cita asociada a un profesional.
   * @param {number|string} professionalId - Identificador del profesional
   * @returns {Promise<Object>}
   */
  async getByProfessionalId(professionalId) {
    const response = await apiClient.get(`/api/appointments/professional/${professionalId}`)
    return response.data
  },

  /**
   * Busca citas por profesional y fecha.
   * @param {Object} params
   * @param {number|string} params.professionalId - ID del profesional
   * @param {string} params.date - Fecha en formato YYYY-MM-DD
   * @returns {Promise<Array<Object>>}
   */
  async search({ professionalId, date }) {
    const response = await apiClient.get('/api/appointments/search', {
      params: {
        professionalId,
        date,
      },
    })
    return response.data
  },

  /**
   * Actualiza los datos de una cita existente.
   * @param {number|string} id - ID de la cita a actualizar
   * @param {Object} appointmentData - Datos de la cita
   * @returns {Promise<Object>}
   */
  async update(id, appointmentData) {
    const response = await apiClient.put(`/api/appointments/${id}`, appointmentData)
    return response.data
  },

  /**
   * Elimina/Cancela una cita por su ID.
   * @param {number|string} id - ID de la cita a eliminar
   * @returns {Promise<void>}
   */
  async delete(id) {
    const response = await apiClient.delete(`/api/appointments/${id}`)
    return response.data
  },
}

export default appointmentService
