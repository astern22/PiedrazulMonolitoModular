import apiClient from './client.js'

/**
 * Servicio para la gestion de Pacientes (/api/patients)
 * Corresponde a PatientController
 */
export const patientService = {
  /**
   * Obtiene la lista completa de todos los pacientes.
   * @returns {Promise<Array<Object>>}
   */
  async getAll() {
    const response = await apiClient.get('/api/patients')
    return response.data
  },

  /**
   * Obtiene el paciente vinculado al usuario actualmente autenticado.
   * @returns {Promise<Object>}
   */
  async getMe() {
    const response = await apiClient.get('/api/patients/me')
    return response.data
  },

  /**
   * Obtiene un paciente por su ID.
   * @param {number|string} id
   * @returns {Promise<Object>}
   */
  async getById(id) {
    const response = await apiClient.get(`/api/patients/${id}`)
    return response.data
  },

  /**
   * Busca un paciente por su numero de documento.
   * @param {string} documentNumber
   * @returns {Promise<Object>}
   */
  async getByDocument(documentNumber) {
    const response = await apiClient.get(`/api/patients/document/${encodeURIComponent(documentNumber)}`)
    return response.data
  },

  /**
   * Registra un nuevo paciente en el sistema.
   * @param {Object} data
   * @param {string} data.documentNumber - Numero de documento
   * @param {string} [data.phone] - Telefono de contacto
   * @param {string} [data.birthDate] - Fecha de nacimiento (YYYY-MM-DD)
   * @param {number} [data.userId] - ID del usuario del sistema (opcional)
   * @returns {Promise<Object>}
   */
  async create({ documentNumber, phone, birthDate, userId }) {
    const payload = {
      documentNumber: String(documentNumber).trim(),
      phone: phone ? String(phone).trim() : null,
      birthDate: birthDate || null,
      userId: userId ? Number(userId) : null,
    }
    const response = await apiClient.post('/api/patients', payload)
    return response.data
  },

  /**
   * Actualiza la informacion de un paciente existente.
   * @param {number|string} id - ID del paciente a actualizar
   * @param {Object} data
   * @param {string} data.documentNumber
   * @param {string} [data.phone]
   * @param {string} [data.birthDate]
   * @param {number} [data.userId]
   * @returns {Promise<Object>}
   */
  async update(id, { documentNumber, phone, birthDate, userId }) {
    const payload = {
      documentNumber: String(documentNumber).trim(),
      phone: phone ? String(phone).trim() : null,
      birthDate: birthDate || null,
      userId: userId ? Number(userId) : null,
    }
    const response = await apiClient.put(`/api/patients/${id}`, payload)
    return response.data
  },

  /**
   * Elimina un paciente por su ID.
   * @param {number|string} id
   * @returns {Promise<void>}
   */
  async delete(id) {
    const response = await apiClient.delete(`/api/patients/${id}`)
    return response.data
  },
}

export default patientService

