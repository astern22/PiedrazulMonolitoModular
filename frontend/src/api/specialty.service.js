import apiClient from './client.js'

/**
 * Servicio para la gestion de Especialidades Medicas (/api/specialties)
 */
export const specialtyService = {
  /**
   * Obtiene la lista de todas las especialidades registradas.
   * @returns {Promise<Array<{id: number, name: string}>>}
   */
  async getAll() {
    const response = await apiClient.get('/api/specialties')
    return response.data
  },

  /**
   * Crea una nueva especialidad medica.
   * @param {Object} data
   * @param {string} data.name - Nombre de la especialidad (requerido)
   * @returns {Promise<{id: number, name: string}>}
   */
  async create({ name }) {
    const response = await apiClient.post('/api/specialties', { name })
    return response.data
  },
}

export default specialtyService
