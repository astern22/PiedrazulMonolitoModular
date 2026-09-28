import apiClient from './client.js'

/**
 * Servicio de administracion de cuentas internas (/api/users).
 * Solo el rol ADMIN tiene acceso.
 */
export const userService = {
  /**
   * Crea la cuenta de un agendador (rol SCHEDULER).
   * @param {Object} data
   * @param {string} data.fullName
   * @param {string} data.username
   * @param {string} data.email
   * @param {string} data.password
   * @returns {Promise<{id: number, username: string, fullName: string, email: string, enabled: boolean}>}
   */
  async createScheduler({ fullName, username, email, password }) {
    const response = await apiClient.post('/api/users/schedulers', {
      fullName: fullName.trim(),
      username: username.trim(),
      email: email.trim(),
      password,
    })
    return response.data
  },

  /**
   * Lista las cuentas de agendadores.
   * @returns {Promise<Array<Object>>}
   */
  async getSchedulers() {
    const response = await apiClient.get('/api/users/schedulers')
    return response.data
  },
}

export default userService
