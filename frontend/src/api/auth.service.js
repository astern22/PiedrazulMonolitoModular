import apiClient, {
  getToken,
  setToken,
  removeToken,
  decodeToken,
  isTokenExpired,
} from './client.js'

/**
 * Servicio de Autenticacion para interactuar con /auth
 */
export const authService = {
  /**
   * Inicia sesion con usuario y contraseña.
   * Guarda automaticamente el token JWT obtenido.
   * @param {Object} credentials
   * @param {string} credentials.username
   * @param {string} credentials.password
   * @returns {Promise<{token: string}>}
   */
  async login({ username, password }) {
    const response = await apiClient.post('/auth/login', { username, password })
    if (response.data && response.data.token) {
      setToken(response.data.token)
    }
    return response.data
  },

  /**
   * Registra un nuevo usuario en la plataforma.
   * @param {Object} userData
   * @param {string} userData.username
   * @param {string} userData.password
   * @param {string} userData.fullName
   * @param {string} userData.email
   * @returns {Promise<{username: string}>}
   */
  async register({ username, password, fullName, email }) {
    const response = await apiClient.post('/auth/register', {
      username,
      password,
      fullName,
      email,
    })
    return response.data
  },

  /**
   * Cierra la sesion activa eliminando el token.
   */
  logout() {
    removeToken()
  },

  /**
   * Obtiene el token almacenado.
   * @returns {string|null}
   */
  getToken() {
    return getToken()
  },

  /**
   * Verifica si el usuario actual tiene una sesion valida y no expirada.
   * @returns {boolean}
   */
  isAuthenticated() {
    const token = getToken()
    return Boolean(token && !isTokenExpired(token))
  },

  /**
   * Retorna la informacion decodificada del usuario autenticado.
   * Incluye roles si el JWT los contiene en el claim "roles".
   * @returns {{username: string, roles: string[], exp?: number, iat?: number}|null}
   */
  getCurrentUser() {
    const decoded = decodeToken()
    if (!decoded) return null
    return {
      username: decoded.sub,
      roles: Array.isArray(decoded.roles) ? decoded.roles : [],
      exp: decoded.exp,
      iat: decoded.iat,
    }
  },
}

export default authService
