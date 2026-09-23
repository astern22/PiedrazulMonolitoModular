<script setup>
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { user, isAuthenticated, canManage, canManagePatients, hasAnyRole, logout } = useAuth()

function handleLogout() {
  logout()
  router.push('/login')
}
</script>

<template>
  <div class="app-layout">
    <header class="app-header">
      <div class="nav-container">
        <router-link to="/" class="brand-link">
          <span class="brand-text">Piedrazul</span>
          <span class="brand-subtitle">Salud</span>
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
  </div>
</template>

<style>
/* Estilos globales basicos */
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: "IBM Plex Sans Thai", sans-serif;
  background-color: #f8fafc;
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 0.85rem 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.5rem;
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
  color: #1e3a8a;
}

.brand-subtitle {
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  background: #eff6ff;
  color: #2563eb;
  padding: 0.15rem 0.4rem;
  border-radius: 4px;
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
  color: #1e3a8a;
  background: #f1f5f9;
}

.nav-item.router-link-exact-active {
  color: #2563eb;
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
  border-radius: 9999px;
  border: 1px solid #e2e8f0;
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
  background: #2563eb;
  color: white;
}

.btn-register:hover {
  background: #1d4ed8;
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
</style>
