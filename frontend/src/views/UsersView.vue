<script setup>
import { ref, onMounted } from 'vue'
import { userService } from '@/api'
import { validateSchedulerForm, hasErrors } from '@/utils/validators'

const schedulers = ref([])
const fieldErrors = ref({})
const isLoading = ref(false)
const isSubmitting = ref(false)
const showPassword = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const emptyForm = () => ({
  fullName: '',
  username: '',
  email: '',
  password: '',
})

const form = ref(emptyForm())

async function fetchSchedulers() {
  isLoading.value = true
  try {
    schedulers.value = await userService.getSchedulers()
  } catch (error) {
    errorMessage.value = error.message || 'Error al obtener la lista de agendadores.'
  } finally {
    isLoading.value = false
  }
}

async function handleCreate() {
  errorMessage.value = ''
  successMessage.value = ''

  fieldErrors.value = validateSchedulerForm(form.value)
  if (hasErrors(fieldErrors.value)) {
    errorMessage.value = 'Corrige los errores del formulario.'
    return
  }

  isSubmitting.value = true
  try {
    const created = await userService.createScheduler(form.value)
    successMessage.value = `¡Agendador "${created.fullName}" creado con exito! Ya puede iniciar sesion con el usuario "${created.username}".`
    form.value = emptyForm()
    fieldErrors.value = {}
    await fetchSchedulers()
  } catch (error) {
    errorMessage.value = error.message || 'No se pudo crear el agendador.'
  } finally {
    isSubmitting.value = false
  }
}

onMounted(fetchSchedulers)
</script>

<template>
  <div class="view-container">
    <div class="page-header">
      <div>
        <h1>Usuarios del Sistema</h1>
        <p>Crea las cuentas de los agendadores que gestionan horarios y citas</p>
      </div>
      <button @click="fetchSchedulers" class="btn btn-outline" :disabled="isLoading">
        Refrescar
      </button>
    </div>

    <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>
    <div v-if="successMessage" class="alert alert-success">{{ successMessage }}</div>

    <div class="layout-grid">
      <div class="card form-card">
        <h3>Crear Agendador</h3>
        <p class="form-hint">
          El agendador podra registrar profesionales, horarios y agendar citas, pero no crear otros usuarios.
        </p>
        <form @submit.prevent="handleCreate" novalidate>
          <div class="form-group">
            <label for="schFullName">Nombre completo</label>
            <input
              id="schFullName"
              v-model="form.fullName"
              type="text"
              placeholder="ej. Ana Gomez"
              autocomplete="off"
              :disabled="isSubmitting"
            />
            <span v-if="fieldErrors.fullName" class="field-error">{{ fieldErrors.fullName }}</span>
          </div>

          <div class="form-group">
            <label for="schUsername">Usuario</label>
            <input
              id="schUsername"
              v-model="form.username"
              type="text"
              placeholder="ej. agomez"
              autocomplete="off"
              :disabled="isSubmitting"
            />
            <span v-if="fieldErrors.username" class="field-error">{{ fieldErrors.username }}</span>
          </div>

          <div class="form-group">
            <label for="schEmail">Correo electronico</label>
            <input
              id="schEmail"
              v-model="form.email"
              type="email"
              placeholder="ej. agomez@piedrazul.com"
              autocomplete="off"
              :disabled="isSubmitting"
            />
            <span v-if="fieldErrors.email" class="field-error">{{ fieldErrors.email }}</span>
          </div>

          <div class="form-group">
            <label for="schPassword">Contraseña inicial</label>
            <div class="password-row">
              <input
                id="schPassword"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="Minimo 6 caracteres"
                autocomplete="new-password"
                :disabled="isSubmitting"
              />
              <button type="button" class="btn btn-outline btn-sm" @click="showPassword = !showPassword">
                {{ showPassword ? 'Ocultar' : 'Ver' }}
              </button>
            </div>
            <span v-if="fieldErrors.password" class="field-error">{{ fieldErrors.password }}</span>
          </div>

          <button type="submit" class="btn btn-primary w-full" :disabled="isSubmitting">
            <span v-if="isSubmitting" class="spinner"></span>
            <span v-else>Crear Agendador</span>
          </button>
        </form>
      </div>

      <div class="card list-card">
        <h3>Agendadores ({{ schedulers.length }})</h3>

        <div v-if="isLoading" class="loading-state">
          <div class="spinner-large"></div>
          <p>Cargando agendadores...</p>
        </div>

        <div v-else-if="schedulers.length === 0" class="empty-state">
          <p>Aun no hay agendadores registrados.</p>
        </div>

        <div v-else class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Usuario</th>
                <th>Correo</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in schedulers" :key="user.id">
                <td><strong>{{ user.fullName }}</strong></td>
                <td>{{ user.username }}</td>
                <td>{{ user.email }}</td>
                <td>
                  <span class="status-badge" :class="user.enabled ? 'status-active' : 'status-inactive'">
                    {{ user.enabled ? 'ACTIVO' : 'INACTIVO' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-container { max-width: 1100px; margin: 0 auto; padding: 2rem 1rem; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem; }
.page-header h1 { font-size: 1.8rem; color: #0f172a; margin: 0 0 0.25rem; }
.page-header p { color: #64748b; margin: 0; font-size: 0.95rem; }
.layout-grid { display: grid; grid-template-columns: 320px 1fr; gap: 1.5rem; align-items: start; }
@media (max-width: 800px) { .layout-grid { grid-template-columns: 1fr; } }
.card { background: white; border-radius: 12px; padding: 1.5rem; border: 1px solid #e2e8f0; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04); }
.card h3 { margin-top: 0; margin-bottom: 1.25rem; color: #1e293b; font-size: 1.15rem; }
.form-hint { color: #64748b; font-size: 0.85rem; margin: -0.5rem 0 1.25rem; }
.form-group { display: flex; flex-direction: column; gap: 0.35rem; margin-bottom: 1rem; }
.form-group label { font-size: 0.825rem; font-weight: 600; color: #334155; }
.form-group input { padding: 0.65rem 0.85rem; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 0.9rem; outline: none; background: white; min-width: 0; }
.form-group input:focus { border-color: #4d1d93; box-shadow: 0 0 0 3px rgba(77, 29, 147, 0.15); }
.password-row { display: flex; gap: 0.5rem; }
.password-row input { flex: 1; }
.field-error { color: #dc2626; font-size: 0.75rem; margin-top: 0.15rem; }
.btn { padding: 0.7rem 1.25rem; border-radius: 8px; font-weight: 600; font-size: 0.9rem; cursor: pointer; border: none; transition: all 0.2s; display: inline-flex; align-items: center; justify-content: center; gap: 0.5rem; }
.btn-sm { padding: 0.4rem 0.75rem; font-size: 0.8rem; }
.btn-primary { background: #4d1d93; color: white; }
.btn-primary:hover:not(:disabled) { background: #4d2785; }
.btn-outline { background: transparent; color: #475569; border: 1px solid #cbd5e1; }
.btn-outline:hover:not(:disabled) { background: #f8fafc; color: #1e293b; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
.w-full { width: 100%; }
.status-badge { display: inline-block; padding: 0.2rem 0.5rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 700; }
.status-active { background: #f0fdf4; color: #15803d; }
.status-inactive { background: #fef2f2; color: #991b1b; }
.alert { padding: 0.75rem 1rem; border-radius: 8px; font-size: 0.875rem; margin-bottom: 1.5rem; }
.alert-error { background: #fef2f2; color: #991b1b; border: 1px solid #fecaca; }
.alert-success { background: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; }
.table-responsive { overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.data-table th { background: #f8fafc; color: #475569; font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em; padding: 0.75rem 1rem; border-bottom: 2px solid #e2e8f0; }
.data-table td { padding: 0.85rem 1rem; border-bottom: 1px solid #f1f5f9; font-size: 0.9rem; }
.loading-state, .empty-state { padding: 2.5rem 1rem; text-align: center; color: #64748b; }
.spinner { width: 16px; height: 16px; border: 2px solid white; border-top-color: transparent; border-radius: 50%; animation: spin 0.6s linear infinite; }
.spinner-large { width: 30px; height: 30px; border: 3px solid #4d1d93; border-top-color: transparent; border-radius: 50%; animation: spin 0.6s linear infinite; margin: 0 auto 0.5rem; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
