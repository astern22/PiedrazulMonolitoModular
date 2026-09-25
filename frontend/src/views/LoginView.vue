<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { required } from '@/utils/validators'
import AuthCardTemplate from '@/views/components/AuthCardTemplate.vue'

const router = useRouter()
const route = useRoute()
const { login } = useAuth()

const username = ref('')
const password = ref('')
const isLoading = ref(false)
const errorMessage = ref('')

async function handleSubmit() {
  errorMessage.value = ''

  const usernameErr = required(username.value, 'El usuario')
  const passwordErr = required(password.value, 'La contraseña')

  if (usernameErr || passwordErr) {
    errorMessage.value = usernameErr || passwordErr
    return
  }

  isLoading.value = true

  try {
    await login({
      username: username.value.trim(),
      password: password.value,
    })

    const redirectPath = route.query.redirect || '/appointments'

    router.push(redirectPath)
  } catch (error) {
    errorMessage.value =
      error?.message ||
      'Credenciales inválidas. Por favor verifica tus datos.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <div class="auth-card">

      <AuthCardTemplate />

      <div class="auth-login">
        <div class="auth-header">
          <h2>Iniciar Sesión</h2>
          <p>Ingresa tus credenciales para acceder a Piedrazul</p>
        </div>

        <div v-if="errorMessage" class="alert alert-error">
          {{ errorMessage }}
        </div>

        <form
          class="auth-form"
          @submit.prevent="handleSubmit"
        >
          <div class="form-group">
            <label for="username">
              Nombre de usuario
            </label>

            <input
              id="username"
              v-model="username"
              type="text"
              placeholder="ej. juanperez"
              autocomplete="username"
              required
              :disabled="isLoading"
            />
          </div>

          <div class="form-group">
            <label for="password">
              Contraseña
            </label>

            <input
              id="password"
              v-model="password"
              type="password"
              placeholder="••••••••"
              autocomplete="current-password"
              required
              :disabled="isLoading"
            />
          </div>

          <button
            type="submit"
            class="btn-submit"
            :disabled="isLoading"
          >
            <span v-if="isLoading" class="spinner"></span>
            <span v-else>Entrar</span>
          </button>
        </form>

        <div class="auth-footer">
          <p>
            ¿No tienes una cuenta?
            <router-link to="/register">
              Regístrate aquí
            </router-link>
          </p>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f3f4f6;
  padding: 20px;
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

.auth-login {
  background: #ffffff;
  padding: 50px;

  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth-header {
  text-align: center;
  margin-bottom: 30px;
}

.auth-header h2 {
  margin: 15px 0 8px;
  color: #1f2937;
}

.auth-header p {
  margin: 0;
  color: #6b7280;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.form-group label {
  font-weight: 600;
  color: #374151;
}

.form-group input {
  padding: 13px 15px;

  border: 1px solid #d1d5db;
  border-radius: 8px;

  font-size: 15px;
  outline: none;

  transition: 0.2s;
}

.form-group input:focus {
  border-color: #7c3aed;

  box-shadow:
    0 0 0 3px rgba(124, 58, 237, 0.12);
}

.btn-submit {
  border: none;
  border-radius: 8px;

  padding: 14px;

  background: #7c3aed;
  color: white;

  font-size: 16px;
  font-weight: 600;

  cursor: pointer;
  transition: 0.2s;
}

.btn-submit:hover {
  background: #4d2785;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.auth-footer {
  text-align: center;
  margin-top: 25px;
  color: #6b7280;
}

.auth-footer a {
  color: #7c3aed;
  font-weight: 600;
  text-decoration: none;
}

.auth-footer a:hover {
  text-decoration: underline;
}

.alert-error {
  background: #fee2e2;
  color: #b91c1c;

  padding: 12px;
  border-radius: 8px;

  margin-bottom: 20px;
}

.spinner {
  display: inline-block;

  width: 18px;
  height: 18px;

  border: 3px solid rgba(255, 255, 255, 0.4);
  border-top-color: white;

  border-radius: 50%;

  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

</style>
