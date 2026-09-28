<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { appointmentService, schedulingService, professionalService, patientService } from '@/api'
import { useAuth } from '@/composables/useAuth'
import { validateAppointmentForm, hasErrors } from '@/utils/validators'
import { professionalLabel, professionalNameById } from '@/utils/professionals'

const { canManage, isPatient } = useAuth()

const appointments = ref([])
// Todos los profesionales (para mostrar el nombre en la tabla) y los activos (para elegir)
const allProfessionals = ref([])
const professionals = computed(() =>
  allProfessionals.value.filter(prof => prof.active !== false)
)
const fieldErrors = ref({})
const isLoading = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const showCreateForm = ref(false)

// Id del paciente vinculado al usuario autenticado (solo rol paciente)
const currentPatient = ref(null)
const isLoadingCurrentPatient = ref(false)

// Busqueda de paciente por cedula (rol agendador/admin)
const patientSearch = ref({
  documentNumber: '',
  result: null,
  isSearching: false,
  error: '',
})

// Franjas de horario disponibles
const availableSlots = ref([])
const isLoadingSlots = ref(false)
const selectedSlotIndex = ref(null)

const showManualTime = computed(() =>
  form.value.professionalId &&
  form.value.appointmentDate &&
  !isLoadingSlots.value &&
  availableSlots.value.length === 0
)

const todayStr = computed(() => new Date().toISOString().split('T')[0])

// Formulario de nueva cita
const form = ref({
  patientId: '',
  professionalId: '',
  appointmentDate: '',
  startTime: '08:00',
  endTime: '08:30',
})

// Filtros de busqueda
const filter = ref({
  professionalId: '',
  date: '',
})

async function loadPatientsList() {
  if (!isPatient.value) return
  isLoadingCurrentPatient.value = true
  try {
    currentPatient.value = await patientService.getMe()
    form.value.patientId = currentPatient.value ? currentPatient.value.id : ''
  } catch (error) {
    currentPatient.value = null
    console.error('Error al cargar el paciente del usuario actual:', error)
  } finally {
    isLoadingCurrentPatient.value = false
  }
}

function resetPatientSearch() {
  patientSearch.value = {
    documentNumber: '',
    result: null,
    isSearching: false,
    error: '',
  }
}

function openCreateForm() {
  showCreateForm.value = true
  if (isPatient.value) {
    form.value.patientId = currentPatient.value ? currentPatient.value.id : ''
  } else {
    resetPatientSearch()
    form.value.patientId = ''
  }
}

function closeCreateForm() {
  showCreateForm.value = false
  if (isPatient.value) {
    form.value.patientId = currentPatient.value ? currentPatient.value.id : ''
  } else {
    resetPatientSearch()
    form.value.patientId = ''
  }
}

function onPatientDocumentChange() {
  patientSearch.value.result = null
  patientSearch.value.error = ''
  form.value.patientId = ''
}

async function searchPatientByDocument() {
  const doc = patientSearch.value.documentNumber.trim()
  if (!doc) return

  patientSearch.value.isSearching = true
  patientSearch.value.error = ''
  patientSearch.value.result = null

  try {
    const patient = await patientService.getByDocument(doc)
    patientSearch.value.result = patient
    form.value.patientId = patient.id
  } catch (error) {
    form.value.patientId = ''
    patientSearch.value.error =
      'No se encontro un paciente con ese numero de documento. Verifica el numero o registralo primero en el modulo de Pacientes.'
  } finally {
    patientSearch.value.isSearching = false
  }
}

function addMinutesToTime(time, minutes) {
  if (!time || !minutes) return time
  const [h, m] = time.split(':').map(Number)
  if (Number.isNaN(h) || Number.isNaN(m)) return time
  const totalMinutes = h * 60 + m + Number(minutes)
  const wrapped = ((totalMinutes % 1440) + 1440) % 1440
  const hh = String(Math.floor(wrapped / 60)).padStart(2, '0')
  const mm = String(wrapped % 60).padStart(2, '0')
  return `${hh}:${mm}`
}

function recalculateEndTime() {
  const professional = professionals.value.find(
    (prof) => String(prof.id) === String(form.value.professionalId)
  )
  if (professional?.appointmentIntervalMinutes && form.value.startTime) {
    form.value.endTime = addMinutesToTime(
      form.value.startTime,
      professional.appointmentIntervalMinutes
    )
  }
}

async function loadProfessionals() {
  try {
    allProfessionals.value = await professionalService.getAll()
  } catch (error) {
    console.error('Error al cargar lista de profesionales:', error)
  }
}

async function fetchAppointments() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    // Un paciente solo debe ver SUS PROPIAS citas, nunca las de otros pacientes.
    if (isPatient.value) {
      appointments.value = currentPatient.value
        ? await appointmentService.getByPatientId(currentPatient.value.id)
        : []
    } else {
      appointments.value = await appointmentService.getAll()
    }
  } catch (error) {
    errorMessage.value = error.message || 'Error al cargar las citas medicas.'
  } finally {
    isLoading.value = false
  }
}

async function fetchAvailableSlots() {
  if (!form.value.professionalId || !form.value.appointmentDate) {
    availableSlots.value = []
    selectedSlotIndex.value = null
    return
  }

  isLoadingSlots.value = true
  selectedSlotIndex.value = null
  try {
    const slots = await schedulingService.getAvailableSlots(
      form.value.professionalId,
      form.value.appointmentDate
    )
    availableSlots.value = slots || []
  } catch (error) {
    console.error('Error al obtener franjas horarias:', error)
    availableSlots.value = []
  } finally {
    isLoadingSlots.value = false
  }
}

watch(
  () => [form.value.professionalId, form.value.appointmentDate],
  () => {
    if (showCreateForm.value) {
      fetchAvailableSlots()
    }
  }
)

// Calcula Hora Fin automaticamente io
watch(
  () => [form.value.professionalId, form.value.startTime],
  () => {
    if (showCreateForm.value) {
      recalculateEndTime()
    }
  }
)

function selectSlot(slot, index) {
  selectedSlotIndex.value = index
  form.value.startTime = slot.startTime.substring(0, 5)
  form.value.endTime = slot.endTime.substring(0, 5)
}

async function handleSearch() {
  if (!filter.value.professionalId || !filter.value.date) {
    errorMessage.value = 'Para buscar, selecciona el profesional y la fecha.'
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  try {
    appointments.value = await appointmentService.search({
      professionalId: filter.value.professionalId,
      date: filter.value.date,
    })
  } catch (error) {
    errorMessage.value = error.message || 'Error al buscar citas con los criterios indicados.'
  } finally {
    isLoading.value = false
  }
}

function handleClearSearch() {
  filter.value.professionalId = ''
  filter.value.date = ''
  fetchAppointments()
}

async function handleCreate() {
  errorMessage.value = ''
  successMessage.value = ''

  if (isPatient.value) {
    if (!currentPatient.value) {
      errorMessage.value =
        'No se encontro un paciente vinculado a tu usuario. Contacta al administrador.'
      return
    }
    form.value.patientId = currentPatient.value.id
  }

  fieldErrors.value = validateAppointmentForm(form.value)
  if (hasErrors(fieldErrors.value)) {
    errorMessage.value = 'Corrige los errores del formulario de cita.'
    return
  }

  isSubmitting.value = true

  try {
    const created = await appointmentService.create({
      patientId: Number(form.value.patientId),
      professionalId: Number(form.value.professionalId),
      appointmentDate: form.value.appointmentDate,
      startTime: form.value.startTime,
      endTime: form.value.endTime,
    })

    successMessage.value = `¡Cita medica #${created.id} agendada correctamente!`
    form.value = {
      patientId: isPatient.value && currentPatient.value ? currentPatient.value.id : '',
      professionalId: '',
      appointmentDate: '',
      startTime: '08:00',
      endTime: '08:30',
    }
    fieldErrors.value = {}
    availableSlots.value = []
    selectedSlotIndex.value = null
    showCreateForm.value = false
    resetPatientSearch()
    await fetchAppointments()
  } catch (error) {
    errorMessage.value = error.message || 'No se pudo crear la cita medica.'
  } finally {
    isSubmitting.value = false
  }
}

async function handleDelete(id) {
  if (!confirm(`¿Estas seguro de que deseas cancelar la cita #${id}?`)) {
    return
  }

  errorMessage.value = ''
  successMessage.value = ''
  try {
    await appointmentService.delete(id)
    successMessage.value = `Cita #${id} cancelada exitosamente.`
    await fetchAppointments()
  } catch (error) {
    errorMessage.value = error.message || `No se pudo cancelar la cita #${id}.`
  }
}

onMounted(async () => {
  loadProfessionals()

  if (isPatient.value) {
    await loadPatientsList()
  }
  fetchAppointments()
})
</script>

<template>
  <div class="view-container">
    <div class="page-header">
      <div>
        <h1>Gestion de Citas Medicas</h1>
        <p>Agendamiento, busqueda y administracion de citas de la entidad de salud</p>
      </div>
      <div class="header-actions">
        <!-- Solo Pacientes, Administradores y Agendadores pueden crear citas -->
        <button
          v-if="canManage || isPatient"
          @click="showCreateForm ? closeCreateForm() : openCreateForm()"
          class="btn"
          :class="showCreateForm ? 'btn-secondary' : 'btn-primary'"
        >
          {{ showCreateForm ? 'Cancelar' : 'Agendar Cita' }}
        </button>
        <button @click="fetchAppointments" class="btn btn-outline" :disabled="isLoading">
          Refrescar
        </button>
      </div>
    </div>

    <!-- Alertas -->
    <div v-if="errorMessage" class="alert alert-error">
      {{ errorMessage }}
    </div>
    <div v-if="successMessage" class="alert alert-success">
      {{ successMessage }}
    </div>

    <!-- Formulario para agendar cita -->
    <div v-if="showCreateForm" class="card create-card">
      <h3>Agendar Nueva Cita Medica</h3>
      <form @submit.prevent="handleCreate" novalidate>
        <div class="form-grid">
          <div v-if="isPatient" class="form-group patient-info">
            <label>Paciente</label>
            <span v-if="isLoadingCurrentPatient" class="text-muted">Cargando tu informacion...</span>
            <span v-else-if="currentPatient" class="patient-chip">
              Paciente #{{ currentPatient.id }}
            </span>
            <span v-else class="text-error">
              No se encontro un paciente vinculado a tu usuario.
            </span>
          </div>

          <div v-else class="form-group patient-search-group">
            <label for="patientDocument">Cedula del Paciente</label>
            <div class="patient-search-row">
              <input
                id="patientDocument"
                v-model="patientSearch.documentNumber"
                type="text"
                placeholder="ej. 1061789234"
                :disabled="isSubmitting"
                @input="onPatientDocumentChange"
                @keydown.enter.prevent="searchPatientByDocument"
              />
              <button
                type="button"
                class="btn btn-secondary btn-search"
                @click="searchPatientByDocument"
                :disabled="isSubmitting || patientSearch.isSearching || !patientSearch.documentNumber.trim()"
              >
                <span v-if="patientSearch.isSearching" class="spinner-sm"></span>
                <span v-else>Buscar</span>
              </button>
            </div>
            <span v-if="patientSearch.result" class="patient-found">
              ✓ {{ patientSearch.result.fullName || 'Paciente sin cuenta de usuario' }} — Doc.
              {{ patientSearch.result.documentNumber }}
            </span>
            <span v-else-if="patientSearch.error" class="field-error">{{ patientSearch.error }}</span>
            <span v-else-if="fieldErrors.patientId" class="field-error">{{ fieldErrors.patientId }}</span>
          </div>

          <div class="form-group">
            <label for="professionalId">Profesional</label>
            <select
              id="professionalId"
              v-model="form.professionalId"
              :disabled="isSubmitting"
            >
              <option value="" disabled>Selecciona profesional</option>
              <option v-for="prof in professionals" :key="prof.id" :value="prof.id">
                {{ professionalLabel(prof) }}
              </option>
            </select>
            <span v-if="fieldErrors.professionalId" class="field-error">{{ fieldErrors.professionalId }}</span>
          </div>

          <div class="form-group">
            <label for="appointmentDate">Fecha</label>
            <input
              id="appointmentDate"
              v-model="form.appointmentDate"
              type="date"
              :min="todayStr"
              :disabled="isSubmitting"
            />
            <span v-if="fieldErrors.appointmentDate" class="field-error">{{ fieldErrors.appointmentDate }}</span>
          </div>

          <div v-if="showManualTime" class="form-group">
            <label for="startTime">Hora Inicio</label>
            <input
              id="startTime"
              v-model="form.startTime"
              type="time"
              :disabled="isSubmitting"
            />
            <span v-if="fieldErrors.startTime" class="field-error">{{ fieldErrors.startTime }}</span>
          </div>

          <div v-if="showManualTime" class="form-group">
            <label for="endTime">Hora Fin</label>
            <input
              id="endTime"
              v-model="form.endTime"
              type="time"
              :disabled="isSubmitting"
            />
            <span class="text-muted-xs"></span>
            <span v-if="fieldErrors.endTime" class="field-error">{{ fieldErrors.endTime }}</span>
          </div>
        </div>

        <!-- Franjas horarias disponibles calculadas por scheduling/AvailableSlotService -->
        <div v-if="form.professionalId && form.appointmentDate" class="slots-section">
          <label class="slots-label">
            Franjas Horarias Disponibles:
          </label>
          <div v-if="isLoadingSlots" class="slots-loading">
            <span class="spinner-sm"></span> Calculando franjas libres...
          </div>
          <div v-else-if="availableSlots.length === 0" class="no-slots">
            No se encontraron franjas libres para este dia. Puedes ingresar el horario manualmente arriba.
          </div>
          <div v-else class="slots-container">
            <button
              type="button"
              v-for="(slot, idx) in availableSlots"
              :key="idx"
              @click="selectSlot(slot, idx)"
              class="slot-chip"
              :class="{ 'slot-selected': selectedSlotIndex === idx }"
            >
              {{ slot.startTime.substring(0, 5) }} - {{ slot.endTime.substring(0, 5) }}
            </button>
          </div>
        </div>

        <div class="form-actions mt-3">
          <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
            <span v-if="isSubmitting" class="spinner"></span>
            <span v-else>Guardar Cita</span>
          </button>
        </div>
      </form>
    </div>

    <!-- Barra de busqueda y filtros -->
    <div class="card filter-card">
      <h4>Filtrar Citas por Profesional y Fecha</h4>
      <form @submit.prevent="handleSearch" class="filter-form">
        <div class="form-group">
          <label for="filterProfId">Profesional</label>
          <select id="filterProfId" v-model="filter.professionalId">
            <option value="">Selecciona un profesional</option>
            <option v-for="prof in professionals" :key="prof.id" :value="prof.id">
              {{ professionalLabel(prof) }}
            </option>
          </select>
        </div>
        <div class="form-group">
          <label for="filterDate">Fecha</label>
          <input id="filterDate" v-model="filter.date" type="date" />
        </div>
        <div class="filter-buttons">
          <button type="submit" class="btn btn-primary" :disabled="isLoading">
            Buscar
          </button>
          <button
            type="button"
            @click="handleClearSearch"
            class="btn btn-secondary"
            :disabled="isLoading"
          >
            Limpiar
          </button>
        </div>
      </form>
    </div>

    <!-- Tabla de Citas -->
    <div class="card table-card">
      <div class="table-header">
        <h3>Citas Medicas ({{ appointments.length }})</h3>
      </div>

      <div v-if="isLoading" class="loading-state">
        <div class="spinner-large"></div>
        <p>Cargando citas medicas...</p>
      </div>

      <div v-else-if="appointments.length === 0" class="empty-state">
        <p>No se encontraron citas medicas registradas con los criterios seleccionados.</p>
      </div>

      <div v-else class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Paciente</th>
              <th>Profesional</th>
              <th>Fecha</th>
              <th>Horario</th>
              <th>Estado</th>
              <th style="text-align: right">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in appointments" :key="item.id">
              <td class="cell-id">#{{ item.id }}</td>
              <td><strong>Paciente #{{ item.patientId }}</strong></td>
              <td>{{ professionalNameById(allProfessionals, item.professionalId) }}</td>
              <td>{{ item.appointmentDate }}</td>
              <td>{{ item.startTime }} - {{ item.endTime }}</td>
              <td>
                <span
                  class="status-badge"
                  :class="{
                    'status-scheduled': item.status === 'SCHEDULED' || item.status === 'PROGRAMADA',
                    'status-completed': item.status === 'COMPLETED' || item.status === 'COMPLETADA',
                    'status-cancelled': item.status === 'CANCELLED' || item.status === 'CANCELADA',
                  }"
                >
                  {{ item.status }}
                </span>
              </td>
              <td style="text-align: right">
                <button
                  v-if="canManage"
                  @click="handleDelete(item.id)"
                  class="btn-danger-sm"
                  title="Cancelar Cita"
                >
                  Cancelar
                </button>
                <span v-else class="text-muted-xs">—</span>
              </td>
            </tr>
          </tbody>
        </table>
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
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

.header-actions {
  display: flex;
  gap: 0.75rem;
}

.card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 1.5rem;
}

.card h3,
.card h4 {
  margin-top: 0;
  color: #1e293b;
}

.create-card {
  border-left: 4px solid #c725eb;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
  align-items: flex-end;
}

.form-actions {
  display: flex;
  align-items: flex-end;
}

.mt-3 {
  margin-top: 1rem;
}

.slots-section {
  margin-top: 1.25rem;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px dashed #cbd5e1;
}

.slots-label {
  display: block;
  font-size: 0.825rem;
  font-weight: 700;
  color: #2f0968;
  margin-bottom: 0.6rem;
}

.slots-loading,
.no-slots {
  font-size: 0.85rem;
  color: #64748b;
  padding: 0.5rem 0;
}

.slots-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.slot-chip {
  background: white;
  border: 1px solid #93c5fd;
  color: #4d2785;
  padding: 0.4rem 0.8rem;
  border-radius: 9999px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
}

.slot-chip:hover {
  background: #eff6ff;
  border-color: #3b82f6;
  transform: translateY(-1px);
}

.slot-selected {
  background: #4d1d93 !important;
  color: white !important;
  border-color: #4d2785 !important;
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.3);
}

.filter-form {
  display: flex;
  gap: 1rem;
  align-items: flex-end;
  flex-wrap: wrap;
}

.filter-buttons {
  display: flex;
  gap: 0.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  text-align: left;
  flex: 1;
  min-width: 140px;
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

.text-muted-xs {
  color: #94a3b8;
  font-size: 0.85rem;
}

.patient-info {
  justify-content: center;
}

.patient-search-group {
  min-width: 240px;
}

.patient-search-row {
  display: flex;
  gap: 0.5rem;
}

.patient-search-row input {
  flex: 1;
}

.btn-search {
  padding: 0.65rem 1rem;
  white-space: nowrap;
}

.patient-found {
  color: #15803d;
  font-size: 0.8rem;
  font-weight: 600;
}

.patient-chip {
  display: inline-block;
  padding: 0.65rem 0.85rem;
  background: #eff6ff;
  color: #4d2785;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
}

.text-muted {
  color: #64748b;
  font-size: 0.9rem;
}

.text-error {
  color: #dc2626;
  font-size: 0.85rem;
}

.btn {
  padding: 0.65rem 1.25rem;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
}

.btn-primary {
  background: #4d1d93;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #4d2785;
}

.btn-secondary {
  background: #f1f5f9;
  color: #334155;
  border: 1px solid #cbd5e1;
}

.btn-secondary:hover:not(:disabled) {
  background: #e2e8f0;
}

.btn-outline {
  background: transparent;
  color: #475569;
  border: 1px solid #cbd5e1;
}

.btn-outline:hover:not(:disabled) {
  background: #f8fafc;
  color: #1e293b;
}

.btn-danger-sm {
  background: #fee2e2;
  color: #b91c1c;
  border: 1px solid #fca5a5;
  padding: 0.35rem 0.75rem;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-danger-sm:hover {
  background: #fecaca;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.6rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  background: #f1f5f9;
  color: #475569;
}

.status-scheduled {
  background: #eff6ff;
  color: #4d2785;
}

.status-completed {
  background: #f0fdf4;
  color: #15803d;
}

.status-cancelled {
  background: #fef2f2;
  color: #b91c1c;
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

.table-responsive {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.data-table th {
  background: #f8fafc;
  color: #475569;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 0.75rem 1rem;
  border-bottom: 2px solid #e2e8f0;
}

.data-table td {
  padding: 0.85rem 1rem;
  border-bottom: 1px solid #f1f5f9;
  font-size: 0.9rem;
}

.cell-id {
  color: #94a3b8;
  font-family: monospace;
}

.loading-state,
.empty-state {
  padding: 3rem 1rem;
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

.spinner-sm {
  display: inline-block;
  width: 12px;
  height: 12px;
  border: 2px solid #4d1d93;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  vertical-align: middle;
}

.spinner-large {
  width: 32px;
  height: 32px;
  border: 3px solid #4d1d93;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  margin: 0 auto 0.75rem;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>