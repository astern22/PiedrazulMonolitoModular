<script setup>
import { useAuth } from '@/composables/useAuth'

const { user, isAuthenticated } = useAuth()
</script>

<template>
  <div class="home-container">
    <section class="hero-card">
      <div class="hero-badge">Monolito Modular Piedrazul</div>
      <h1 class="hero-title">Sistema de Gestion de Citas Medicas</h1>
      <p class="hero-subtitle">
        Plataforma unificada para la administracion de especialidades, profesionales y citas medicas
        con autenticacion segura.
      </p>

      <div v-if="isAuthenticated" class="welcome-banner">
        <span class="avatar-icon">👤</span>
        <div>
          <h3>¡Bienvenido de nuevo, {{ user?.username }}!</h3>
          <p>Tu sesion se encuentra activa con token de acceso seguro.</p>
        </div>
      </div>

      <div class="action-buttons">
        <router-link v-if="!isAuthenticated" to="/login" class="btn btn-primary">
          Iniciar Sesion
        </router-link>
        <router-link v-if="!isAuthenticated" to="/register" class="btn btn-secondary">
          Registrarse
        </router-link>
        <router-link v-if="isAuthenticated" to="/appointments" class="btn btn-primary">
          Gestionar Citas
        </router-link>
        <router-link v-if="isAuthenticated" to="/scheduling" class="btn btn-secondary">
          Horarios y Franjas
        </router-link>
        <router-link v-if="isAuthenticated" to="/professionals" class="btn btn-secondary">
          Profesionales
        </router-link>
        <router-link v-if="isAuthenticated" to="/specialties" class="btn btn-secondary">
          Ver Especialidades
        </router-link>
      </div>
    </section>

    <section class="features-grid">
      <div class="feature-card">
        <div class="feature-icon">🔐</div>
        <h3>Autenticación JWT</h3>
        <p>Control de acceso basado en tokens y roles para pacientes y profesionales de la salud.</p>
        <span class="endpoint-tag">/auth</span>
      </div>

      <div class="feature-card">
        <div class="feature-icon">🩺</div>
        <h3>Especialidades Médicas</h3>
        <p>Catálogo centralizado de especialidades médicas disponibles en el centro de salud.</p>
        <span class="endpoint-tag">/api/specialties</span>
      </div>

      <div class="feature-card">
        <div class="feature-icon">👨‍⚕️</div>
        <h3>Profesionales de la Salud</h3>
        <p>Registro de profesionales, asociación con usuarios y definición de intervalos de consulta.</p>
        <span class="endpoint-tag">/api/professionals</span>
      </div>

      <div class="feature-card">
        <div class="feature-icon">⏰</div>
        <h3>Disponibilidad y Franjas</h3>
        <p>Gestión de disponibilidad semanal y cálculo automático de franjas horarias libres.</p>
        <span class="endpoint-tag">/api/availability</span>
      </div>

      <div class="feature-card">
        <div class="feature-icon">📅</div>
        <h3>Agendamiento de Citas</h3>
        <p>Creación, actualización, filtrado por profesional/fecha y cancelación de citas médicas.</p>
        <span class="endpoint-tag">/api/appointments</span>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.hero-card {
  background: white;
  border-radius: 16px;
  padding: 3rem 2rem;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
  margin-bottom: 2.5rem;
}

.hero-badge {
  display: inline-block;
  background: #eff6ff;
  color: #2563eb;
  padding: 0.35rem 1rem;
  border-radius: 9999px;
  font-size: 0.85rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.hero-title {
  font-size: 2.25rem;
  color: #0f172a;
  margin-bottom: 1rem;
  font-weight: 800;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 1.1rem;
  color: #64748b;
  max-width: 650px;
  margin: 0 auto 2rem;
  line-height: 1.6;
}

.welcome-banner {
  display: flex;
  align-items: center;
  gap: 1rem;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 12px;
  padding: 1rem 1.5rem;
  max-width: 500px;
  margin: 0 auto 2rem;
  text-align: left;
}

.avatar-icon {
  font-size: 2rem;
}

.welcome-banner h3 {
  margin: 0 0 0.25rem;
  font-size: 1rem;
  color: #166534;
}

.welcome-banner p {
  margin: 0;
  font-size: 0.85rem;
  color: #15803d;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  padding: 0.75rem 1.75rem;
  border-radius: 8px;
  font-weight: 600;
  text-decoration: none;
  font-size: 0.95rem;
  transition: all 0.2s ease;
  display: inline-block;
}

.btn-primary {
  background: #2563eb;
  color: white;
}

.btn-primary:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
}

.btn-secondary {
  background: #f8fafc;
  color: #334155;
  border: 1px solid #cbd5e1;
}

.btn-secondary:hover {
  background: #f1f5f9;
  border-color: #94a3b8;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.feature-card {
  background: white;
  border-radius: 12px;
  padding: 1.75rem;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
  display: flex;
  flex-direction: column;
}

.feature-icon {
  font-size: 2.25rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  font-size: 1.2rem;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.feature-card p {
  font-size: 0.9rem;
  color: #64748b;
  line-height: 1.5;
  flex: 1;
}

.endpoint-tag {
  align-self: flex-start;
  margin-top: 1rem;
  background: #f1f5f9;
  color: #475569;
  font-family: monospace;
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}
</style>

