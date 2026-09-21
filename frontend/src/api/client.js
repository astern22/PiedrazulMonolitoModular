import axios from 'axios'

export const TOKEN_KEY = 'piedrazul_token'

/**
 * Obtiene el token JWT almacenado.
 * @returns {string|null}
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * Almacena el token JWT.
 * @param {string} token
 */
export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  }
}

/**
 * Elimina el token JWT almacenado.
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * Decodifica el payload de un token JWT.
 * @param {string} token
 * @returns {object|null}
 */
export function decodeToken(token = getToken()) {
  if (!token) return null
  try {
    const parts = token.split('.')
    if (parts.length < 2) return null
    const base64Url = parts[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
  } catch (error) {
    console.error('Error al decodificar token JWT:', error)
    return null
  }
}

/**
 * Verifica si el token actual esta expirado o no existe.
 * @param {string} token
 * @returns {boolean}
 */
export function isTokenExpired(token = getToken()) {
  if (!token) return true
  const decoded = decodeToken(token)
  if (!decoded || !decoded.exp) return true
  const currentTime = Math.floor(Date.now() / 1000)
  return decoded.exp < currentTime
}

/**
 * Instancia de Axios configurada para el backend Piedrazul.
 */
const apiClient = axios.create({
  baseURL:
    (typeof import.meta !== 'undefined' &&
      import.meta.env &&
      import.meta.env.VITE_API_BASE_URL) ||
    '',
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
  timeout: 15000,
})

// Interceptor de Peticion: adjuntar token JWT
apiClient.interceptors.request.use(
  config => {
    const token = getToken()
    if (token && !isTokenExpired(token)) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// Interceptor de Respuesta: tratamiento unificado de errores
apiClient.interceptors.response.use(
  response => response,
  error => {
    const status = error.response ? error.response.status : null

    if (status === 401) {
      removeToken()
      window.dispatchEvent(new CustomEvent('auth:unauthorized'))
    }

    // Normalizar mensaje de error para la vista
    const errorResponse = {
      status,
      message:
        (error.response && error.response.data && (error.response.data.message || error.response.data.error)) ||
        error.message ||
        'Ha ocurrido un error inesperado al conectar con el servidor',
      data: error.response?.data,
    }

    return Promise.reject(errorResponse)
  }
)

export default apiClient
