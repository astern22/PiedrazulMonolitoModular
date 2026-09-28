<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { validateRegisterForm, hasErrors } from '@/utils/validators'
import AuthCardTemplate from './components/AuthCardTemplate.vue'

const router = useRouter()
const { register } = useAuth()

const form = ref({
  username: '',
  fullName: '',
  email: '',
  password: '',
  documentNumber: '',
})

const fieldErrors = ref({})
const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

async function handleSubmit() {
  errorMessage.value = ''
  successMessage.value = ''

  fieldErrors.value = validateRegisterForm(form.value)
  if (hasErrors(fieldErrors.value)) {
    errorMessage.value = 'Por favor corrige los errores en el formulario.'
    return
  }

  isLoading.value = true
  try {
    const result = await register({
      username: form.value.username.trim(),
      password: form.value.password,
      fullName: form.value.fullName.trim(),
      email: form.value.email.trim(),
      documentNumber: form.value.documentNumber.trim(),
    })
    successMessage.value = `¡Usuario "${result.username || form.value.username}" registrado con exito! Redirigiendo a inicio de sesion...`
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (error) {
    errorMessage.value =
      error.message || 'No se pudo completar el registro. Intenta nuevamente.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <div class="auth-card">
      <AuthCardTemplate />
      <div class="auth-register">
        <div class="auth-header">
          <div class="auth-icon"></div>
          <h2>Crear Cuenta</h2>
          <p>Registrate en la plataforma Piedrazul</p>
        </div>
  
        <div v-if="errorMessage" class="alert alert-error">
          {{ errorMessage }}
        </div>
  
        <div v-if="successMessage" class="alert alert-success">
          {{ successMessage }}
        </div>
  
        <form @submit.prevent="handleSubmit" class="auth-form">
          <div class="form-group">
            <label for="fullName">Nombre Completo</label>
            <input
              id="fullName"
              v-model="form.fullName"
              type="text"
              placeholder="ej. Juan Perez"
              required
              :disabled="isLoading"
            />
            <span v-if="fieldErrors.fullName" class="field-error">{{ fieldErrors.fullName }}</span>
          </div>
  
          <div class="form-group">
            <label for="email">Correo Electronico</label>
            <input
              id="email"
              v-model="form.email"
              type="email"
              placeholder="ej. juan@ejemplo.com"
              required
              :disabled="isLoading"
            />
            <span v-if="fieldErrors.email" class="field-error">{{ fieldErrors.email }}</span>
          </div>

          <div class="form-group">
            <label for="documentNumber">Número de Documento</label>
            <input
              id="documentNumber"
              v-model="form.documentNumber"
              type="text"
              placeholder="ej. 1061789234"
              required
              :disabled="isLoading"
            />
            <span v-if="fieldErrors.documentNumber" class="field-error">{{ fieldErrors.documentNumber }}</span>
          </div>
  
          <div class="form-group">
            <label for="username">Usuario</label>
            <input
              id="username"
              v-model="form.username"
              type="text"
              placeholder="ej. juanperez"
              required
              autocomplete="username"
              :disabled="isLoading"
            />
            <span v-if="fieldErrors.username" class="field-error">{{ fieldErrors.username }}</span>
          </div>
  
          <div class="form-group">
            <label for="password">Contraseña</label>
            <input
              id="password"
              v-model="form.password"
              type="password"
              placeholder="••••••••"
              required
              autocomplete="new-password"
              :disabled="isLoading"
            />
            <span v-if="fieldErrors.password" class="field-error">{{ fieldErrors.password }}</span>
          </div>
  
          <button type="submit" class="btn-submit" :disabled="isLoading">
            <span v-if="isLoading" class="spinner"></span>
            <span v-else>Registrarse</span>
          </button>
        </form>
  
        <div class="auth-footer">
          <p>
            ¿Ya tienes una cuenta?
            <router-link to="/login">Inicia sesion</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-container {
  min-height: calc(80vh - 80px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
}

.auth-card {
   width: 900px;
  max-width: 100%;
  min-height: 550px;

  display: grid;
  grid-template-columns: 1fr 1fr;

  background: white;
  border-radius: 20px;
  overflow: hidden;

  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.12);
}

.auth-register {
  background: #ffffff;
  padding: 50px;

  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth-header {
  text-align: center;
  margin-bottom: 1.75rem;
}

.auth-icon {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
}

.auth-header h2 {
  font-size: 1.6rem;
  color: #0f172a;
  margin: 0 0 0.5rem;
}

.auth-header p {
  font-size: 0.9rem;
  color: #64748b;
  margin: 0;
}

.alert {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-size: 0.875rem;
  margin-bottom: 1.25rem;
}

.alert-error {
  background: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.alert-success {
  background: #f0fdf4;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  text-align: left;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #334155;
}

.form-group input {
  padding: 0.7rem 0.9rem;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: border-color 0.2s;
  outline: none;
}

.form-group input:focus {
  border-color: #4d1d93;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.field-error {
  color: #dc2626;
  font-size: 0.75rem;
  margin-top: 0.15rem;
}

.btn-submit {
  margin-top: 0.5rem;
  padding: 0.85rem;
  background: #7c3aed;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.btn-submit:hover:not(:disabled) {
  background: #4d2785;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid #ffffff;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.auth-footer {
  margin-top: 1.5rem;
  text-align: center;
  font-size: 0.875rem;
  color: #64748b;
}

.auth-footer a {
  color: #4d1d93;
  text-decoration: none;
  font-weight: 600;
}

.auth-footer a:hover {
  text-decoration: underline;
}
</style>
