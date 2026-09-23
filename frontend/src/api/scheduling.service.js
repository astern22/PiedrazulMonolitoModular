import apiClient from './client.js'

/**
 * Servicio para la gestion de Horarios y Disponibilidad Semanal (/api/availability)
 * Corresponde a los controladores AvailableSlotController y WeeklyAvailabilityController
 */
export const schedulingService = {
  /**
   * Obtiene las franjas horarias libres de un profesional para una fecha dada.
   * @param {number|string} professionalId - ID del profesional
   * @param {string} date - Fecha en formato YYYY-MM-DD
   * @returns {Promise<Array<{startTime: string, endTime: string}>>}
   */
  async getAvailableSlots(professionalId, date) {
    const response = await apiClient.get(
      `/api/availability/professional/${professionalId}/slots`,
      {
        params: { date },
      }
    )
    return response.data
  },

  /**
   * Registra una nueva franja de disponibilidad semanal para un profesional.
   * @param {Object} data
   * @param {number} data.professionalId - ID del profesional
   * @param {number} data.dayOfWeek - Dia de la semana (1 = Lunes, ..., 7 = Domingo)
   * @param {string} data.startTime - Hora de inicio (formato HH:mm o HH:mm:ss)
   * @param {string} data.endTime - Hora de fin (formato HH:mm o HH:mm:ss)
   * @returns {Promise<Object>}
   */
  async createWeeklyAvailability({ professionalId, dayOfWeek, startTime, endTime }) {
    const formattedStartTime = startTime.length === 5 ? `${startTime}:00` : startTime
    const formattedEndTime = endTime.length === 5 ? `${endTime}:00` : endTime

    const response = await apiClient.post('/api/availability', {
      professionalId: Number(professionalId),
      dayOfWeek: Number(dayOfWeek),
      startTime: formattedStartTime,
      endTime: formattedEndTime,
    })
    return response.data
  },

  /**
   * Consulta toda la disponibilidad activa de un profesional.
   * @param {number|string} professionalId - ID del profesional
   * @returns {Promise<Array<Object>>}
   */
  async getByProfessional(professionalId) {
    const response = await apiClient.get(
      `/api/availability/professional/${professionalId}`
    )
    return response.data
  },

  /**
   * Consulta la disponibilidad de un profesional para un dia especifico de la semana.
   * @param {number|string} professionalId - ID del profesional
   * @param {number} dayOfWeek - Dia de la semana (1 a 7)
   * @returns {Promise<Array<Object>>}
   */
  async getByProfessionalAndDay(professionalId, dayOfWeek) {
    const response = await apiClient.get(
      `/api/availability/professional/${professionalId}/day/${dayOfWeek}`
    )
    return response.data
  },

  /**
   * Desactiva una franja de disponibilidad semanal.
   * @param {number|string} id - ID del registro de disponibilidad
   * @returns {Promise<void>}
   */
  async deactivate(id) {
    const response = await apiClient.patch(`/api/availability/${id}/deactivate`)
    return response.data
  },
}

export default schedulingService

