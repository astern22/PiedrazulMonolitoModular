<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { user, isAuthenticated, canManage, canManagePatients, hasAnyRole, logout } = useAuth()
const MIN_SCALE = 0.8
const MAX_SCALE = 1.6
const SCALE_STEP = 0.1
const STORAGE_KEY = 'piedrazul-text-scale'

const textScale = ref(1)

onMounted(() => {
  const saved = parseFloat(localStorage.getItem(STORAGE_KEY))
  if (!Number.isNaN(saved) && saved >= MIN_SCALE && saved <= MAX_SCALE) {
    textScale.value = saved
  }
})

watch(textScale, (value) => {
  document.documentElement.style.fontSize = `${value * 16}px`
  localStorage.setItem(STORAGE_KEY, String(value))
})

function increaseScale() {
  textScale.value = Math.min(MAX_SCALE, Math.round((textScale.value + SCALE_STEP) * 100) / 100)
}

function decreaseScale() {
  textScale.value = Math.max(MIN_SCALE, Math.round((textScale.value - SCALE_STEP) * 100) / 100)
}

function resetScale() {
  textScale.value = 1
}

function handleLogout() {
  logout()
  router.push('/login')
}
</script>

<template>
  <div class="app-layout">
    <header class="app-header">
      <div class="nav-container">
        <div class="brand-container">
          <img src="../src/assets/logo.png" alt="">
          <router-link to="/" class="brand-link">
            <span class="brand-text">Piedra Azul</span>
          </router-link>

          <nav class="nav-menu">
            <router-link to="/" class="nav-item">Inicio</router-link>
            <router-link
              v-if="canManagePatients"
              to="/patients"
              class="nav-item"
            >
              Pacientes
            </router-link>
            <router-link v-if="canManage" to="/specialties" class="nav-item">
              Especialidades
            </router-link>
            <router-link v-if="canManage" to="/professionals" class="nav-item">
              Profesionales
            </router-link>
            <router-link
              v-if="hasAnyRole(['ADMIN', 'SCHEDULER', 'PROFESSIONAL'])"
              to="/scheduling"
              class="nav-item"
            >
              Horarios
            </router-link>
            <router-link v-if="isAuthenticated" to="/appointments" class="nav-item">
              Citas Medicas
            </router-link>
          </nav>
        </div>

        <div class="nav-auth">
          <template v-if="isAuthenticated">
            <div class="user-badge" title="Usuario activo">
              <span class="user-dot"></span>
              <span class="user-name">{{ user?.username }}</span>
            </div>
            <button @click="handleLogout" class="btn-logout">
              Cerrar Sesion
            </button>
          </template>
          <template v-else>
            <router-link to="/login" class="btn-nav btn-login">
              Ingresar
            </router-link>
            <router-link to="/register" class="btn-nav btn-register">
              Registrarse
            </router-link>
          </template>
        </div>
      </div>
    </header>

    <!-- Contenido dinamico SPA sin recarga -->
    <main class="app-main">
      <router-view />
    </main>

    <footer class="app-footer">
      <p>&copy; 2026 Piedrazul Monolito Modular — Modulo de Pacientes, Profesionales y Citas</p>
    </footer>
     <!-- Panel de accesibilidad siempre visible -->
    <div class="accessibility-panel" role="group" aria-label="Accesibilidad de texto">
      <span class="accessibility-label">Texto</span>
      <button
        class="accessibility-btn"
        @click="decreaseScale"
        :disabled="textScale <= MIN_SCALE"
        aria-label="Reducir tamaño del texto"
        title="Reducir texto"
      >
        A−
      </button>
      <button
        class="accessibility-btn accessibility-reset"
        @click="resetScale"
        aria-label="Restablecer tamaño del texto"
        title="Restablecer texto"
      >
        A
      </button>
      <button
        class="accessibility-btn"
        @click="increaseScale"
        :disabled="textScale >= MAX_SCALE"
        aria-label="Aumentar tamaño del texto"
        title="Aumentar texto"
      >
        A+
      </button>
    </div>
  </div>
</template>

<style> 
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: "IBM Plex Sans Thai", sans-serif;
  background-color: #f5f3ff;
  color: #1e293b;
  -webkit-font-smoothing: antialiased;
}
</style>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  position: sticky;
  top: 0;
  z-index: 50;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.nav-container {
  margin: 0 auto;
  padding: 0.85rem 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.5rem;
}

.brand-container {
  display: flex;
  align-items: center;
  width: fit-content;
  gap: 10px;
}

.brand-container img {
  width: 50px;
  height: 50px;
  object-fit: contain;
  filter: drop-shadow(2px 2px 4px rgba(0, 0, 0, 0.2));
}

.brand-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  color: #0f172a;
}

.brand-icon {
  font-size: 1.6rem;
}

.brand-text {
  font-size: 1.25rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: #2f0968;
}

.nav-menu {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.nav-item {
  padding: 0.5rem 0.85rem;
  color: #475569;
  text-decoration: none;
  font-size: 0.95rem;
  font-weight: 500;
  border-radius: 6px;
  transition: all 0.15s ease;
}

.nav-item:hover {
  color: #2f0968;
  background: #f1f5f9;
}

.nav-item.router-link-exact-active {
  color: #4d1d93;
  background: #eff6ff;
  font-weight: 600;
}

.nav-auth {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-badge {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  background: #f1f5f9;
  padding: 0.4rem 0.8rem;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
}

.user-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
}

.user-name {
  font-size: 0.85rem;
  font-weight: 600;
  color: #334155;
}

.btn-nav {
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 600;
  transition: all 0.15s;
}

.btn-login {
  color: #334155;
}

.btn-login:hover {
  background: #f1f5f9;
}

.btn-register {
  background: #4d1d93;
  color: white;
}

.btn-register:hover {
  background: #4d2785;
}

.btn-logout {
  background: transparent;
  color: #64748b;
  border: 1px solid #cbd5e1;
  padding: 0.45rem 0.85rem;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-logout:hover {
  background: #fee2e2;
  color: #b91c1c;
  border-color: #fca5a5;
}

.app-main {
  flex: 1;
}

.app-footer {
  text-align: center;
  padding: 1.5rem;
  color: #94a3b8;
  font-size: 0.825rem;
  border-top: 1px solid #e2e8f0;
  background: white;
}


.accessibility-panel {
  position: fixed;
  bottom: 1.5rem;
  right: 1.5rem;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 0.3rem;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 9999px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.14);
  padding: 0.45rem 0.6rem;
}

.accessibility-label {
  font-size: 0.7rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: #64748b;
  margin-right: 0.2rem;
}

.accessibility-btn {
  min-width: 2.2rem;
  height: 2.2rem;
  padding: 0 0.5rem;
  border: 1px solid #cbd5e1;
  border-radius: 9999px;
  background: #f8fafc;
  color: #2f0968;
  font-size: 0.85rem;
  font-weight: 700;
  line-height: 1;
  cursor: pointer;
  transition: all 0.15s;
}

.accessibility-btn:hover:not(:disabled) {
  background: #eff6ff;
  border-color: #4d1d93;
}

.accessibility-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.accessibility-reset {
  background: #4d1d93;
  border-color: #4d1d93;
  color: white;
}

.accessibility-reset:hover:not(:disabled) {
  background: #4d2785;
}
</style>
