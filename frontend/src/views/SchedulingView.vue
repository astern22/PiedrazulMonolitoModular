<script setup>
import { ref, onMounted } from 'vue'
import { schedulingService, professionalService } from '@/api'

const professionals = ref([])
const selectedProfId = ref('')
const weeklyAvailabilities = ref([])
const isLoadingAvailabilities = ref(false)

// Formulario de nueva disponibilidad semanal
const newAvailability = ref({
  professionalId: '',
  dayOfWeek: 1,
  startTime: '08:00',
  endTime: '12:00',
})
const isSubmittingAvailability = ref(false)

// Consulta de franjas horarias libres (Available Slots)
const slotQuery = ref({
  professionalId: '',
  date: new Date().toISOString().split('T')[0],
})
const availableSlots = ref([])
const isLoadingSlots = ref(false)
const hasQueriedSlots = ref(false)

// Mensajes de alerta
const errorMessage = ref('')
const successMessage = ref('')

const daysOfWeekMap = {
  1: 'Lunes',
  2: 'Martes',
  3: 'Miércoles',
  4: 'Jueves',
  5: 'Viernes',
  6: 'Sábado',
  7: 'Domingo',
}

async function loadProfessionals() {
  try {
    professionals.value = await professionalService.getActive()
    if (professionals.value.length > 0) {
      const firstId = professionals.value[0].id
      selectedProfId.value = firstId
      newAvailability.value.professionalId = firstId
      slotQuery.value.professionalId = firstId
      await fetchWeeklyAvailabilities(firstId)
    }
  } catch (error) {
    console.error('Error al cargar lista de profesionales:', error)
  }
}

async function fetchWeeklyAvailabilities(profId = selectedProfId.value) {
  if (!profId) return
  isLoadingAvailabilities.value = true
  errorMessage.value = ''
  try {
    weeklyAvailabilities.value = await schedulingService.getByProfessional(profId)
  } catch (error) {
    errorMessage.value = error.message || 'Error al consultar disponibilidad semanal.'
  } finally {
    isLoadingAvailabilities.value = false
  }
}

async function handleCreateAvailability() {
  if (!newAvailability.value.professionalId) {
    errorMessage.value = 'Debes seleccionar o ingresar un ID de profesional.'
    return
  }

  isSubmittingAvailability.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    await schedulingService.createWeeklyAvailability(newAvailability.value)
    successMessage.value = `¡Disponibilidad para el día ${daysOfWeekMap[newAvailability.value.dayOfWeek]} agregada con éxito!`
    if (selectedProfId.value == newAvailability.value.professionalId) {
      await fetchWeeklyAvailabilities(selectedProfId.value)
    }
  } catch (error) {
    errorMessage.value = error.message || 'No se pudo registrar la disponibilidad.'
  } finally {
    isSubmittingAvailability.value = false
  }
}

async function handleDeactivate(id) {
  if (!confirm('¿Deseas desactivar esta franja de disponibilidad semanal?')) return
  errorMessage.value = ''
  successMessage.value = ''
  try {
    await schedulingService.deactivate(id)
    successMessage.value = 'Franja de disponibilidad desactivada correctamente.'
    await fetchWeeklyAvailabilities(selectedProfId.value)
  } catch (error) {
    errorMessage.value = error.message || 'Error al desactivar la disponibilidad.'
  }
}

async function handleQuerySlots() {
  if (!slotQuery.value.professionalId || !slotQuery.value.date) {
    errorMessage.value = 'Ingresa el profesional y la fecha para consultar las franjas libres.'
    return
  }

  isLoadingSlots.value = true
  hasQueriedSlots.value = true
  errorMessage.value = ''
  try {
    availableSlots.value = await schedulingService.getAvailableSlots(
      slotQuery.value.professionalId,
      slotQuery.value.date
    )
  } catch (error) {
    errorMessage.value = error.message || 'Error al obtener las franjas horarias libres.'
    availableSlots.value = []
  } finally {
    isLoadingSlots.value = false
  }
}

onMounted(() => {
  loadProfessionals()
})
</script>

<template>
  <div class="view-container">
    <div class="page-header">
      <div>
        <h1>Disponibilidad y Franjas de Horario</h1>
        <p>Configuración de horarios semanales y cálculo de franjas horarias disponibles</p>
      </div>
    </div>

    <!-- Alertas -->
    <div v-if="errorMessage" class="alert alert-error">
      {{ errorMessage }}
    </div>
    <div v-if="successMessage" class="alert alert-success">
      {{ successMessage }}
    </div>

    <div class="layout-grid">
      <!-- Sección Izquierda: Configurar Disponibilidad Semanal -->
      <div class="column">
        <div class="card">
          <h3>➕ Registrar Horario Semanal</h3>
          <p class="section-desc">
            Define los días y rangos en los que el profesional atiende consultas.
          </p>

          <form @submit.prevent="handleCreateAvailability">
            <div class="form-group">
              <label for="profSelect">Profesional</label>
              <select
                id="profSelect"
                v-model="newAvailability.professionalId"
                required
                :disabled="isSubmittingAvailability"
              >
                <option value="" disabled>Selecciona un profesional</option>
                <option
                  v-for="prof in professionals"
                  :key="prof.id"
                  :value="prof.id"
                >
                  Dr(a). ID #{{ prof.id }} ({{ prof.professionalType }})
                </option>
              </select>
            </div>

            <div class="form-group">
              <label for="dayOfWeek">Día de la Semana</label>
              <select
                id="dayOfWeek"
                v-model="newAvailability.dayOfWeek"
                required
                :disabled="isSubmittingAvailability"
              >
                <option :value="1">Lunes</option>
                <option :value="2">Martes</option>
                <option :value="3">Miércoles</option>
                <option :value="4">Jueves</option>
                <option :value="5">Viernes</option>
                <option :value="6">Sábado</option>
                <option :value="7">Domingo</option>
              </select>
            </div>

            <div class="time-row">
              <div class="form-group">
                <label for="startTime">Hora Inicio</label>
                <input
                  id="startTime"
                  v-model="newAvailability.startTime"
                  type="time"
                  required
                  :disabled="isSubmittingAvailability"
                />
              </div>
              <div class="form-group">
                <label for="endTime">Hora Fin</label>
                <input
                  id="endTime"
                  v-model="newAvailability.endTime"
                  type="time"
                  required
                  :disabled="isSubmittingAvailability"
                />
              </div>
            </div>

            <button type="submit" class="btn btn-primary w-full" :disabled="isSubmittingAvailability">
              <span v-if="isSubmittingAvailability" class="spinner"></span>
              <span v-else>Guardar Disponibilidad Semanal</span>
            </button>
          </form>
        </div>

        <!-- Listado de disponibilidades registradas -->
        <div class="card mt-4">
          <div class="card-header-flex">
            <h3>Horarios Semanales</h3>
            <select
              v-model="selectedProfId"
              @change="fetchWeeklyAvailabilities(selectedProfId)"
              class="select-sm"
            >
              <option value="" disabled>Filtrar por profesional</option>
              <option v-for="prof in professionals" :key="prof.id" :value="prof.id">
                Dr(a). ID #{{ prof.id }}
              </option>
            </select>
          </div>

          <div v-if="isLoadingAvailabilities" class="loading-state">
            <div class="spinner-small"></div>
            <p>Cargando disponibilidad...</p>
          </div>

          <div v-else-if="weeklyAvailabilities.length === 0" class="empty-state">
            <p>No hay disponibilidad semanal activa registrada para este profesional.</p>
          </div>

          <ul v-else class="availability-list">
            <li v-for="item in weeklyAvailabilities" :key="item.id" class="availability-item">
              <div>
                <strong>{{ daysOfWeekMap[item.dayOfWeek] }}</strong>:
                <span class="hours">{{ item.startTime }} - {{ item.endTime }}</span>
              </div>
              <button
                @click="handleDeactivate(item.id)"
                class="btn-danger-xs"
                title="Desactivar horario"
              >
                Desactivar
              </button>
            </li>
          </ul>
        </div>
      </div>

      <!-- Sección Derecha: Consulta en Tiempo Real de Franjas Libres -->
      <div class="column">
        <div class="card">
          <h3>🔎 Consultar Franjas Horarias Disponibles</h3>
          <p class="section-desc">
            Calcula dinámicamente los intervalos libres para citas según la duración asignada y citas existentes.
          </p>

          <form @submit.prevent="handleQuerySlots" class="slots-query-form">
            <div class="form-group">
              <label for="slotProf">Profesional</label>
              <select id="slotProf" v-model="slotQuery.professionalId" required>
                <option value="" disabled>Selecciona profesional</option>
                <option v-for="prof in professionals" :key="prof.id" :value="prof.id">
                  Dr(a). ID #{{ prof.id }} ({{ prof.professionalType }} - {{ prof.appointmentIntervalMinutes }} min)
                </option>
              </select>
            </div>

            <div class="form-group">
              <label for="slotDate">Fecha</label>
              <input id="slotDate" v-model="slotQuery.date" type="date" required />
            </div>

            <button type="submit" class="btn btn-primary" :disabled="isLoadingSlots">
              <span v-if="isLoadingSlots" class="spinner"></span>
              <span v-else>Calcular Franjas Libres</span>
            </button>
          </form>

          <div class="slots-result mt-4">
            <div v-if="isLoadingSlots" class="loading-state">
              <div class="spinner-large"></div>
              <p>Calculando ranuras libres disponibles...</p>
            </div>

            <div v-else-if="hasQueriedSlots && availableSlots.length === 0" class="empty-state">
              <p>⚠️ No hay franjas disponibles para este profesional en la fecha seleccionada.</p>
              <small>Verifica si el profesional tiene disponibilidad semanal ese día y no tiene la agenda llena.</small>
            </div>

            <div v-else-if="availableSlots.length > 0">
              <h4>Franjas Disponibles ({{ availableSlots.length }})</h4>
              <div class="slots-grid">
                <div
                  v-for="(slot, idx) in availableSlots"
                  :key="idx"
                  class="slot-card"
                >
                  <span class="slot-icon">🕒</span>
                  <span class="slot-text">{{ slot.startTime }} - {{ slot.endTime }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.page-header {
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 1.8rem;
  color: #0f172a;
  margin: 0 0 0.25rem;
}

.page-header p {
  color: #64748b;
  margin: 0;
  font-size: 0.95rem;
}

.layout-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

@media (max-width: 850px) {
  .layout-grid {
    grid-template-columns: 1fr;
  }
}

.card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card h3 {
  margin-top: 0;
  margin-bottom: 0.4rem;
  color: #1e293b;
  font-size: 1.15rem;
}

.section-desc {
  color: #64748b;
  font-size: 0.85rem;
  margin: 0 0 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  margin-bottom: 1rem;
}

.form-group label {
  font-size: 0.825rem;
  font-weight: 600;
  color: #334155;
}

.form-group input,
.form-group select {
  padding: 0.65rem 0.85rem;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.9rem;
  outline: none;
  background: white;
}

.form-group input:focus,
.form-group select:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.time-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.btn {
  padding: 0.7rem 1.25rem;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-primary {
  background: #2563eb;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #1d4ed8;
}

.btn-danger-xs {
  background: #fee2e2;
  color: #b91c1c;
  border: 1px solid #fca5a5;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
}

.btn-danger-xs:hover {
  background: #fecaca;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.w-full {
  width: 100%;
}

.mt-4 {
  margin-top: 1.5rem;
}

.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.card-header-flex h3 {
  margin: 0;
}

.select-sm {
  padding: 0.35rem 0.6rem;
  font-size: 0.8rem;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
}

.availability-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.availability-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.65rem 0.5rem;
  border-bottom: 1px solid #f1f5f9;
  font-size: 0.9rem;
}

.availability-item .hours {
  color: #2563eb;
  font-family: monospace;
  font-weight: 600;
  margin-left: 0.4rem;
}

.slots-query-form {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  gap: 0.75rem;
  margin-top: 0.75rem;
}

.slot-card {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  padding: 0.6rem 0.5rem;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.3rem;
  color: #1e40af;
  font-weight: 600;
  font-size: 0.825rem;
}

.alert {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-size: 0.875rem;
  margin-bottom: 1.5rem;
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

.loading-state,
.empty-state {
  padding: 2rem 1rem;
  text-align: center;
  color: #64748b;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid white;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

.spinner-small {
  width: 20px;
  height: 20px;
  border: 2px solid #2563eb;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  margin: 0 auto 0.5rem;
}

.spinner-large {
  width: 28px;
  height: 28px;
  border: 3px solid #2563eb;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  margin: 0 auto 0.5rem;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>

