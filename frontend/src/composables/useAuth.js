import { ref, computed } from 'vue'
import { authService } from '@/api'

const token = ref(authService.getToken())
const user = ref(authService.getCurrentUser())

if (typeof window !== 'undefined') {
  window.addEventListener('auth:unauthorized', () => {
    token.value = null
    user.value = null
  })
}

/**
 * Composable para gestionar el estado reactivo de autenticacion en la SPA.
 */
export function useAuth() {
  const isAuthenticated = computed(() => {
    return Boolean(token.value && authService.isAuthenticated())
  })

  /**
   * Inicia sesion con credenciales y actualiza el estado reactivo.
   * @param {Object} credentials
   * @returns {Promise<Object>}
   */
  async function login(credentials) {
    const data = await authService.login(credentials)
    token.value = data.token
    user.value = authService.getCurrentUser()
    return data
  }

  /**
   * Registra un nuevo usuario en la base de datos.
   * @param {Object} userData
   * @returns {Promise<Object>}
   */
  async function register(userData) {
    return await authService.register(userData)
  }

  /**
   * Cierra la sesion activa y reinicia el estado reactivo.
   */
  function logout() {
    authService.logout()
    token.value = null
    user.value = null
  }

  return {
    token,
    user,
    isAuthenticated,
    login,
    register,
    logout,
  }
}

