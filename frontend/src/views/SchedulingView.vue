<script setup>
import { ref, computed, onMounted } from 'vue'
import { schedulingService, professionalService } from '@/api'
import { useAuth } from '@/composables/useAuth'
import {
  validateWeeklyAvailabilityForm,
  validateIntervalMinutes,
  findOverlappingAvailability,
  hasErrors,
} from '@/utils/validators'
import { professionalLabel, professionalName } from '@/utils/professionals'

const { canManage } = useAuth()

const professionals = ref([])
// Profesional seleccionado: lo comparten el formulario, la duracion y la lista de horarios
const selectedProfId = ref('')
const weeklyAvailabilities = ref([])
const isLoadingAvailabilities = ref(false)
const fieldErrors = ref({})

// Formulario de nueva disponibilidad semanal
const newAvailability = ref({
  dayOfWeek: 1,
  startTime: '08:00',
  endTime: '12:00',
})
const isSubmittingAvailability = ref(false)

// Duracion de las citas del profesional seleccionado
const intervalMinutes = ref('')
const intervalError = ref('')
const isSavingInterval = ref(false)

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
  3: 'Miercoles',
  4: 'Jueves',
  5: 'Viernes',
  6: 'Sabado',
  7: 'Domingo',
}

const selectedProfessional = computed(() =>
  professionals.value.find(prof => String(prof.id) === String(selectedProfId.value)) || null
)

// Horarios ordenados por dia y hora para leerlos mejor
const sortedAvailabilities = computed(() =>
  [...weeklyAvailabilities.value].sort(
    (a, b) =>
      a.dayOfWeek - b.dayOfWeek ||
      String(a.startTime).localeCompare(String(b.startTime))
  )
)

// IDs de horarios que se cruzan con otro del mismo dia (por ejemplo, datos registrados antes de la validacion)
const overlappingIds = computed(() => {
  const ids = new Set()
  const list = sortedAvailabilities.value
  for (let i = 0; i < list.length; i++) {
    for (let j = i + 1; j < list.length; j++) {
      if (list[i].dayOfWeek !== list[j].dayOfWeek) continue
      const startI = String(list[i].startTime).substring(0, 5)
      const endI = String(list[i].endTime).substring(0, 5)
      const startJ = String(list[j].startTime).substring(0, 5)
      const endJ = String(list[j].endTime).substring(0, 5)
      if (startI < endJ && endI > startJ) {
        ids.add(list[i].id)
        ids.add(list[j].id)
      }
    }
  }
  return ids
})

function formatHour(time) {
  return String(time).substring(0, 5)
}

function syncIntervalFromSelection() {
  intervalMinutes.value = selectedProfessional.value
    ? selectedProfessional.value.appointmentIntervalMinutes
    : ''
  intervalError.value = ''
}

async function loadProfessionals(keepSelection = false) {
  try {
    professionals.value = await professionalService.getActive()
    if (professionals.value.length > 0) {
      const stillExists = professionals.value.some(
        prof => String(prof.id) === String(selectedProfId.value)
      )
      if (!keepSelection || !stillExists) {
        selectedProfId.value = professionals.value[0].id
        slotQuery.value.professionalId = professionals.value[0].id
      }
      syncIntervalFromSelection()
      await fetchWeeklyAvailabilities(selectedProfId.value)
    }
  } catch (error) {
    console.error('Error al cargar lista de profesionales:', error)
  }
}

async function handleProfessionalChange() {
  errorMessage.value = ''
  successMessage.value = ''
  fieldErrors.value = {}
  syncIntervalFromSelection()
  await fetchWeeklyAvailabilities(selectedProfId.value)
}

async function fetchWeeklyAvailabilities(profId = selectedProfId.value) {
  if (!profId) return
  isLoadingAvailabilities.value = true
  try {
    weeklyAvailabilities.value = await schedulingService.getByProfessional(profId)
  } catch (error) {
    errorMessage.value = error.message || 'Error al consultar disponibilidad semanal.'
  } finally {
    isLoadingAvailabilities.value = false
  }
}

async function handleCreateAvailability() {
  errorMessage.value = ''
  successMessage.value = ''

  const payload = {
    professionalId: selectedProfId.value,
    dayOfWeek: newAvailability.value.dayOfWeek,
    startTime: newAvailability.value.startTime,
    endTime: newAvailability.value.endTime,
  }

  fieldErrors.value = validateWeeklyAvailabilityForm(payload)
  if (hasErrors(fieldErrors.value)) {
    errorMessage.value = 'Corrige los errores del formulario de disponibilidad.'
    return
  }

  // Evita cruces de horario en el mismo dia antes de llamar al servidor
  const overlap = findOverlappingAvailability(weeklyAvailabilities.value, payload)
  if (overlap) {
    fieldErrors.value = {
      startTime: 'Este rango se cruza con otro horario del mismo dia.',
    }
    errorMessage.value = `El horario se cruza con el del ${daysOfWeekMap[overlap.dayOfWeek]} ${formatHour(overlap.startTime)} - ${formatHour(overlap.endTime)}. Ajusta las horas o desactiva ese horario primero.`
    return
  }

  isSubmittingAvailability.value = true

  try {
    await schedulingService.createWeeklyAvailability({
      professionalId: Number(payload.professionalId),
      dayOfWeek: Number(payload.dayOfWeek),
      startTime: payload.startTime,
      endTime: payload.endTime,
    })
    successMessage.value = `¡Disponibilidad del ${daysOfWeekMap[payload.dayOfWeek]} agregada con exito!`
    fieldErrors.value = {}
    await fetchWeeklyAvailabilities(selectedProfId.value)
  } catch (error) {
    errorMessage.value = error.message || 'No se pudo registrar la disponibilidad.'
  } finally {
    isSubmittingAvailability.value = false
  }
}

async function handleSaveInterval() {
  errorMessage.value = ''
  successMessage.value = ''
  intervalError.value = validateIntervalMinutes(intervalMinutes.value) || ''
  if (intervalError.value) return

  if (Number(intervalMinutes.value) === selectedProfessional.value?.appointmentIntervalMinutes) {
    intervalError.value = 'La duracion es la misma que la actual.'
    return
  }

  isSavingInterval.value = true
  try {
    const updated = await professionalService.updateAppointmentInterval(
      selectedProfId.value,
      intervalMinutes.value
    )
    successMessage.value = `Duracion de cita de ${professionalName(updated)} actualizada a ${updated.appointmentIntervalMinutes} minutos. Las citas ya agendadas no se modifican.`
    await loadProfessionals(true)
    // Si ya se habia consultado una fecha, recalcula las franjas con la nueva duracion
    if (hasQueriedSlots.value && slotQuery.value.professionalId) {
      await handleQuerySlots()
    }
  } catch (error) {
    errorMessage.value = error.message || 'No se pudo actualizar la duracion de la cita.'
  } finally {
    isSavingInterval.value = false
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
    errorMessage.value = 'Selecciona el profesional y la fecha para consultar las franjas libres.'
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
        <p>Configuracion de horarios semanales y calculo de franjas horarias disponibles</p>
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
      <!-- Seccion Izquierda: Configurar Disponibilidad Semanal -->
      <div class="column">
        <!-- Solo ADMIN y SCHEDULER pueden registrar horarios semanales -->
        <div v-if="canManage" class="card">
          <h3>Registrar Horario Semanal</h3>
          <p class="section-desc">
            Define los dias y rangos en los que el profesional atiende consultas.
          </p>

          <form @submit.prevent="handleCreateAvailability" novalidate>
            <div class="form-group">
              <label for="profSelect">Profesional</label>
              <select
                id="profSelect"
                v-model="selectedProfId"
                :disabled="isSubmittingAvailability"
                @change="handleProfessionalChange"
              >
                <option value="" disabled>Selecciona un profesional</option>
                <option
                  v-for="prof in professionals"
                  :key="prof.id"
                  :value="prof.id"
                >
                  {{ professionalLabel(prof) }}
                </option>
              </select>
              <span v-if="fieldErrors.professionalId" class="field-error">{{ fieldErrors.professionalId }}</span>
            </div>

            <div class="form-group">
              <label for="dayOfWeek">Dia de la Semana</label>
              <select
                id="dayOfWeek"
                v-model="newAvailability.dayOfWeek"
                :disabled="isSubmittingAvailability"
              >
                <option :value="1">Lunes</option>
                <option :value="2">Martes</option>
                <option :value="3">Miercoles</option>
                <option :value="4">Jueves</option>
                <option :value="5">Viernes</option>
                <option :value="6">Sabado</option>
                <option :value="7">Domingo</option>
              </select>
              <span v-if="fieldErrors.dayOfWeek" class="field-error">{{ fieldErrors.dayOfWeek }}</span>
            </div>

            <div class="time-row">
              <div class="form-group">
                <label for="startTime">Hora Inicio</label>
                <input
                  id="startTime"
                  v-model="newAvailability.startTime"
                  type="time"
                  :disabled="isSubmittingAvailability"
                />
                <span v-if="fieldErrors.startTime" class="field-error">{{ fieldErrors.startTime }}</span>
              </div>
              <div class="form-group">
                <label for="endTime">Hora Fin</label>
                <input
                  id="endTime"
                  v-model="newAvailability.endTime"
                  type="time"
                  :disabled="isSubmittingAvailability"
                />
                <span v-if="fieldErrors.endTime" class="field-error">{{ fieldErrors.endTime }}</span>
              </div>
            </div>

            <button type="submit" class="btn btn-primary w-full" :disabled="isSubmittingAvailability">
              <span v-if="isSubmittingAvailability" class="spinner"></span>
              <span v-else>Guardar Disponibilidad Semanal</span>
            </button>
          </form>
        </div>

        <!-- Duracion de las citas del profesional seleccionado -->
        <div v-if="selectedProfessional" class="card" :class="{ 'mt-4': canManage }">
          <h3>Duracion de las Citas</h3>
          <p class="section-desc">
            Tiempo que dura cada cita de {{ professionalName(selectedProfessional) }}.
            Puedes cambiarlo cuando lo necesite; las citas ya agendadas no se modifican.
          </p>

          <form v-if="canManage" @submit.prevent="handleSaveInterval" class="interval-form" novalidate>
            <div class="form-group interval-input">
              <label for="intervalMinutes">Minutos por cita</label>
              <input
                id="intervalMinutes"
                v-model="intervalMinutes"
                type="number"
                min="5"
                max="480"
                step="5"
                :disabled="isSavingInterval"
              />
            </div>
            <button type="submit" class="btn btn-primary" :disabled="isSavingInterval">
              <span v-if="isSavingInterval" class="spinner"></span>
              <span v-else>Guardar Duracion</span>
            </button>
          </form>
          <span v-if="intervalError" class="field-error">{{ intervalError }}</span>

          <p v-if="!canManage" class="interval-readonly">
            <strong>{{ selectedProfessional.appointmentIntervalMinutes }} minutos</strong> por cita
          </p>
        </div>

        <!-- Listado de disponibilidades registradas -->
        <div class="card mt-4">
          <div class="card-header-flex">
            <h3>Horarios Semanales</h3>
            <select
              v-model="selectedProfId"
              @change="handleProfessionalChange"
              class="select-sm"
            >
              <option value="" disabled>Filtrar por profesional</option>
              <option v-for="prof in professionals" :key="prof.id" :value="prof.id">
                {{ professionalName(prof) }}
              </option>
            </select>
          </div>

          <div v-if="overlappingIds.size > 0" class="alert alert-warning">
            Hay horarios que se cruzan entre si (marcados abajo). Desactiva el que sobre para
            evitar franjas repetidas.
          </div>

          <div v-if="isLoadingAvailabilities" class="loading-state">
            <div class="spinner-small"></div>
            <p>Cargando disponibilidad...</p>
          </div>

          <div v-else-if="weeklyAvailabilities.length === 0" class="empty-state">
            <p>No hay disponibilidad semanal activa registrada para este profesional.</p>
          </div>

          <ul v-else class="availability-list">
            <li
              v-for="item in sortedAvailabilities"
              :key="item.id"
              class="availability-item"
              :class="{ 'availability-conflict': overlappingIds.has(item.id) }"
            >
              <div>
                <strong>{{ daysOfWeekMap[item.dayOfWeek] }}</strong>:
                <span class="hours">{{ formatHour(item.startTime) }} - {{ formatHour(item.endTime) }}</span>
                <span v-if="overlappingIds.has(item.id)" class="conflict-badge">Se cruza</span>
              </div>
              <button
                v-if="canManage"
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

      <!-- Seccion Derecha: Consulta en Tiempo Real de Franjas Libres -->
      <div class="column">
        <div class="card">
          <h3>Consultar Franjas Horarias Disponibles</h3>
          <p class="section-desc">
            Calcula dinamicamente los intervalos libres para citas segun la duracion asignada y citas existentes.
          </p>

          <form @submit.prevent="handleQuerySlots" class="slots-query-form">
            <div class="form-group">
              <label for="slotProf">Profesional</label>
              <select id="slotProf" v-model="slotQuery.professionalId" required>
                <option value="" disabled>Selecciona profesional</option>
                <option v-for="prof in professionals" :key="prof.id" :value="prof.id">
                  {{ professionalLabel(prof, { withInterval: true }) }}
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
              <small>Verifica si el profesional tiene disponibilidad semanal ese dia y no tiene la agenda llena.</small>
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
  border-color: #4d1d93;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.field-error {
  color: #dc2626;
  font-size: 0.75rem;
  margin-top: 0.15rem;
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
  background: #4d1d93;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #4d2785;
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
  color: #4d1d93;
  font-family: monospace;
  font-weight: 600;
  margin-left: 0.4rem;
}

.interval-form {
  display: flex;
  align-items: flex-end;
  gap: 0.75rem;
}

.interval-input {
  flex: 1;
  margin-bottom: 0;
}

.interval-readonly {
  margin: 0;
  color: #334155;
}

.alert-warning {
  background: #fffbeb;
  color: #92400e;
  border: 1px solid #fde68a;
  margin-bottom: 1rem;
}

.availability-conflict {
  background: #fffbeb;
}

.conflict-badge {
  margin-left: 0.5rem;
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fde68a;
  border-radius: 9999px;
  padding: 0.1rem 0.5rem;
  font-size: 0.7rem;
  font-weight: 600;
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
  border: 2px solid #4d1d93;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  margin: 0 auto 0.5rem;
}

.spinner-large {
  width: 28px;
  height: 28px;
  border: 3px solid #4d1d93;
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

