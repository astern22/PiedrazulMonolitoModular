import { ref, computed } from 'vue'
import { authService } from '@/api'

const token = ref(authService.getToken())
const user = ref(authService.getCurrentUser())

// Escucha el evento global emitido por el interceptor cuando un token caduca (401)
if (typeof window !== 'undefined') {
  window.addEventListener('auth:unauthorized', () => {
    token.value = null
    user.value = null
  })
}

/**
 * Composable para gestionar el estado reactivo de autenticacion y roles en la SPA.
 */
export function useAuth() {
  const isAuthenticated = computed(() => {
    return Boolean(token.value && authService.isAuthenticated())
  })

  /** Roles del usuario actual como array reactivo */
  const roles = computed(() => {
    return user.value?.roles || []
  })

  // ─── Helpers de rol ───
  const isAdmin = computed(() => roles.value.includes('ADMIN'))
  const isPatient = computed(() => roles.value.includes('PATIENT'))
  const isProfessional = computed(() => roles.value.includes('PROFESSIONAL'))
  const isScheduler = computed(() => roles.value.includes('SCHEDULER'))
  const isDoctor = computed(() => roles.value.includes('PROFESSIONAL') || roles.value.includes('MEDICO'))
  const canManagePatients = computed(() => isDoctor.value || isAdmin.value)

  /**
   * Verifica si el usuario tiene un rol especifico.
   * @param {string} role
   * @returns {boolean}
   */
  function hasRole(role) {
    return roles.value.includes(role)
  }

  /**
   * Verifica si el usuario tiene al menos uno de los roles indicados.
   * @param {string[]} roleList
   * @returns {boolean}
   */
  function hasAnyRole(roleList) {
    return roleList.some(r => roles.value.includes(r))
  }

  /**
   * Verifica si el usuario puede administrar (ADMIN o SCHEDULER).
   */
  const canManage = computed(() => {
    return isAdmin.value || isScheduler.value
  })

  /**
   * Inicia sesion con credenciales y actualiza el estado reactivo.
   */
  async function login(credentials) {
    const data = await authService.login(credentials)
    token.value = data.token
    user.value = authService.getCurrentUser()
    return data
  }

  /**
   * Registra un nuevo usuario en la base de datos.
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
    roles,
    isAdmin,
    isPatient,
    isProfessional,
    isScheduler,
    isDoctor,
    canManage,
    canManagePatients,
    hasRole,
    hasAnyRole,
    login,
    register,
    logout,
  }
}
