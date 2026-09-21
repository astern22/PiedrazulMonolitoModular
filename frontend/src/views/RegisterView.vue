<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { register } = useAuth()

const form = ref({
  username: '',
  fullName: '',
  email: '',
  password: '',
})

const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

async function handleSubmit() {
  errorMessage.value = ''
  successMessage.value = ''

  if (!form.value.username || !form.value.password || !form.value.fullName || !form.value.email) {
    errorMessage.value = 'Por favor completa todos los campos requeridos.'
    return
  }

  isLoading.value = true
  try {
    const result = await register({
      username: form.value.username.trim(),
      password: form.value.password,
      fullName: form.value.fullName.trim(),
      email: form.value.email.trim(),
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
      <div class="auth-header">
        <div class="auth-icon">📝</div>
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
  background: white;
  border-radius: 16px;
  padding: 2.5rem;
  width: 100%;
  max-width: 440px;
  box-shadow: 0 4px 25px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
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
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.btn-submit {
  margin-top: 0.5rem;
  padding: 0.85rem;
  background: #2563eb;
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
  background: #1d4ed8;
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
  color: #2563eb;
  text-decoration: none;
  font-weight: 600;
}

.auth-footer a:hover {
  text-decoration: underline;
}
</style>

