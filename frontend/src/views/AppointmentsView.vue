<script setup>
import { ref, onMounted } from 'vue'
import { appointmentService } from '@/api'

const appointments = ref([])
const isLoading = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const showCreateForm = ref(false)

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

async function fetchAppointments() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    appointments.value = await appointmentService.getAll()
  } catch (error) {
    errorMessage.value = error.message || 'Error al cargar las citas medicas.'
  } finally {
    isLoading.value = false
  }
}

async function handleSearch() {
  if (!filter.value.professionalId || !filter.value.date) {
    errorMessage.value = 'Para buscar, debes ingresar el ID del profesional y la fecha.'
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
  if (
    !form.value.patientId ||
    !form.value.professionalId ||
    !form.value.appointmentDate ||
    !form.value.startTime ||
    !form.value.endTime
  ) {
    errorMessage.value = 'Por favor completa todos los campos para agendar la cita.'
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const created = await appointmentService.create({
      patientId: form.value.patientId,
      professionalId: form.value.professionalId,
      appointmentDate: form.value.appointmentDate,
      startTime: form.value.startTime,
      endTime: form.value.endTime,
    })

    successMessage.value = `¡Cita medica #${created.id} agendada correctamente!`
    form.value = {
      patientId: '',
      professionalId: '',
      appointmentDate: '',
      startTime: '08:00',
      endTime: '08:30',
    }
    showCreateForm.value = false
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

onMounted(() => {
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
        <button
          @click="showCreateForm = !showCreateForm"
          class="btn"
          :class="showCreateForm ? 'btn-secondary' : 'btn-primary'"
        >
          {{ showCreateForm ? '✖ Cancelar' : '➕ Agendar Cita' }}
        </button>
        <button @click="fetchAppointments" class="btn btn-outline" :disabled="isLoading">
          🔄 Refrescar
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

    <!-- Formulario para agendar cita (Colapsable) -->
    <div v-if="showCreateForm" class="card create-card">
      <h3>Agendar Nueva Cita Medica</h3>
      <form @submit.prevent="handleCreate" class="form-grid">
        <div class="form-group">
          <label for="patientId">ID del Paciente</label>
          <input
            id="patientId"
            v-model="form.patientId"
            type="number"
            placeholder="ej. 1"
            required
            :disabled="isSubmitting"
          />
        </div>

        <div class="form-group">
          <label for="professionalId">ID del Profesional</label>
          <input
            id="professionalId"
            v-model="form.professionalId"
            type="number"
            placeholder="ej. 2"
            required
            :disabled="isSubmitting"
          />
        </div>

        <div class="form-group">
          <label for="appointmentDate">Fecha</label>
          <input
            id="appointmentDate"
            v-model="form.appointmentDate"
            type="date"
            required
            :disabled="isSubmitting"
          />
        </div>

        <div class="form-group">
          <label for="startTime">Hora Inicio</label>
          <input
            id="startTime"
            v-model="form.startTime"
            type="time"
            required
            :disabled="isSubmitting"
          />
        </div>

        <div class="form-group">
          <label for="endTime">Hora Fin</label>
          <input
            id="endTime"
            v-model="form.endTime"
            type="time"
            required
            :disabled="isSubmitting"
          />
        </div>

        <div class="form-actions">
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
          <label for="filterProfId">ID Profesional</label>
          <input
            id="filterProfId"
            v-model="filter.professionalId"
            type="number"
            placeholder="ej. 1"
          />
        </div>
        <div class="form-group">
          <label for="filterDate">Fecha</label>
          <input id="filterDate" v-model="filter.date" type="date" />
        </div>
        <div class="filter-buttons">
          <button type="submit" class="btn btn-primary" :disabled="isLoading">
            🔍 Buscar
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
              <td>Dr(a). ID #{{ item.professionalId }}</td>
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
                  @click="handleDelete(item.id)"
                  class="btn-danger-sm"
                  title="Cancelar Cita"
                >
                  Cancelar
                </button>
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
  border-left: 4px solid #2563eb;
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

.form-group input {
  padding: 0.65rem 0.85rem;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.9rem;
  outline: none;
}

.form-group input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
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
  background: #2563eb;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #1d4ed8;
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
  color: #1d4ed8;
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

.spinner-large {
  width: 32px;
  height: 32px;
  border: 3px solid #2563eb;
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

