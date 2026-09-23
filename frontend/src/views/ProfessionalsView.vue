<script setup>
import { ref, onMounted } from 'vue'
import { professionalService, specialtyService } from '@/api'
import { useAuth } from '@/composables/useAuth'
import { validateProfessionalForm, hasErrors } from '@/utils/validators'

const { canManage } = useAuth()

const professionals = ref([])
const specialties = ref([])
const fieldErrors = ref({})
const isLoading = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const filterSpecialtyId = ref('')

const form = ref({
  userId: '',
  specialtyId: '',
  professionalType: 'MEDICO_GENERAL',
  appointmentIntervalMinutes: 30,
})

async function fetchSpecialties() {
  try {
    specialties.value = await specialtyService.getAll()
  } catch (err) {
    console.error('Error al cargar especialidades:', err)
  }
}

async function fetchProfessionals() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    if (filterSpecialtyId.value) {
      professionals.value = await professionalService.getBySpecialty(filterSpecialtyId.value)
    } else {
      professionals.value = await professionalService.getActive()
    }
  } catch (error) {
    errorMessage.value = error.message || 'Error al obtener la lista de profesionales.'
  } finally {
    isLoading.value = false
  }
}

async function handleCreate() {
  errorMessage.value = ''
  successMessage.value = ''

  fieldErrors.value = validateProfessionalForm(form.value)
  if (hasErrors(fieldErrors.value)) {
    errorMessage.value = 'Corrige los errores del formulario.'
    return
  }

  isSubmitting.value = true

  try {
    const created = await professionalService.create({
      userId: Number(form.value.userId),
      specialtyId: Number(form.value.specialtyId),
      professionalType: form.value.professionalType,
      appointmentIntervalMinutes: Number(form.value.appointmentIntervalMinutes),
    })
    successMessage.value = `¡Profesional #${created.id} registrado con exito!`
    form.value = {
      userId: '',
      specialtyId: '',
      professionalType: 'MEDICO_GENERAL',
      appointmentIntervalMinutes: 30,
    }
    fieldErrors.value = {}
    await fetchProfessionals()
  } catch (error) {
    errorMessage.value = error.message || 'No se pudo registrar el profesional.'
  } finally {
    isSubmitting.value = false
  }
}

function getSpecialtyName(id) {
  const spec = specialties.value.find(s => s.id === id)
  return spec ? spec.name : `Especialidad #${id}`
}

onMounted(async () => {
  await fetchSpecialties()
  await fetchProfessionals()
})
</script>

<template>
  <div class="view-container">
    <div class="page-header">
      <div>
        <h1>Profesionales de la Salud</h1>
        <p>Administracion de medicos, especialistas e intervalos de atencion</p>
      </div>
      <button @click="fetchProfessionals" class="btn btn-outline" :disabled="isLoading">
        Refrescar
      </button>
    </div>

    <!-- Alertas -->
    <div v-if="errorMessage" class="alert alert-error">
      {{ errorMessage }}
    </div>
    <div v-if="successMessage" class="alert alert-success">
      {{ successMessage }}
    </div>

    <div class="layout-grid" :class="{ 'single-column': !canManage }">
      <!-- Formulario para registrar profesional (solo ADMIN y SCHEDULER) -->
      <div v-if="canManage" class="card form-card">
        <h3>Registrar Profesional</h3>
        <form @submit.prevent="handleCreate" novalidate>
          <div class="form-group">
            <label for="userId">ID de Usuario</label>
            <input
              id="userId"
              v-model="form.userId"
              type="number"
              placeholder="ej. 2"
              :disabled="isSubmitting"
            />
            <span v-if="fieldErrors.userId" class="field-error">{{ fieldErrors.userId }}</span>
          </div>

          <div class="form-group">
            <label for="specialtySelect">Especialidad</label>
            <select
              id="specialtySelect"
              v-model="form.specialtyId"
              :disabled="isSubmitting"
            >
              <option value="" disabled>Selecciona una especialidad</option>
              <option v-for="spec in specialties" :key="spec.id" :value="spec.id">
                {{ spec.name }}
              </option>
            </select>
            <span v-if="fieldErrors.specialtyId" class="field-error">{{ fieldErrors.specialtyId }}</span>
          </div>

          <div class="form-group">
            <label for="profType">Tipo de Profesional</label>
            <select
              id="profType"
              v-model="form.professionalType"
              :disabled="isSubmitting"
            >
              <option value="MEDICO_GENERAL">Medico General</option>
              <option value="ESPECIALISTA">Especialista</option>
              <option value="ODONTOLOGO">Odontologo</option>
              <option value="PSICOLOGO">Psicologo</option>
              <option value="PEDIATRA">Pediatra</option>
            </select>
            <span v-if="fieldErrors.professionalType" class="field-error">{{ fieldErrors.professionalType }}</span>
          </div>

          <div class="form-group">
            <label for="interval">Duracion de Cita (minutos)</label>
            <input
              id="interval"
              v-model="form.appointmentIntervalMinutes"
              type="number"
              min="5"
              step="5"
              placeholder="30"
              :disabled="isSubmitting"
            />
            <span v-if="fieldErrors.appointmentIntervalMinutes" class="field-error">{{ fieldErrors.appointmentIntervalMinutes }}</span>
          </div>

          <button type="submit" class="btn btn-primary w-full" :disabled="isSubmitting">
            <span v-if="isSubmitting" class="spinner"></span>
            <span v-else>Guardar Profesional</span>
          </button>
        </form>
      </div>

      <!-- Listado de profesionales -->
      <div class="card list-card">
        <div class="list-header">
          <h3>Profesionales Activos ({{ professionals.length }})</h3>
          <div class="filter-box">
            <select
              v-model="filterSpecialtyId"
              @change="fetchProfessionals"
              class="select-filter"
            >
              <option value="">Todas las especialidades</option>
              <option v-for="spec in specialties" :key="spec.id" :value="spec.id">
                {{ spec.name }}
              </option>
            </select>
          </div>
        </div>

        <div v-if="isLoading" class="loading-state">
          <div class="spinner-large"></div>
          <p>Cargando profesionales...</p>
        </div>

        <div v-else-if="professionals.length === 0" class="empty-state">
          <p>No hay profesionales registrados con los criterios seleccionados.</p>
        </div>

        <div v-else class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Usuario</th>
                <th>Especialidad</th>
                <th>Tipo</th>
                <th>Duracion Cita</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="prof in professionals" :key="prof.id">
                <td class="cell-id">#{{ prof.id }}</td>
                <td>User #{{ prof.userId }}</td>
                <td>
                  <span class="badge-specialty">
                    {{ getSpecialtyName(prof.specialtyId) }}
                  </span>
                </td>
                <td>{{ prof.professionalType }}</td>
                <td>{{ prof.appointmentIntervalMinutes }} min</td>
                <td>
                  <span class="status-badge status-active">
                    {{ prof.active ? 'ACTIVO' : 'INACTIVO' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
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

.layout-grid {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 1.5rem;
}

.layout-grid.single-column {
  grid-template-columns: 1fr;
}

.field-error {
  color: #dc2626;
  font-size: 0.75rem;
  margin-top: 0.15rem;
}

@media (max-width: 800px) {
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
  margin-bottom: 1.25rem;
  color: #1e293b;
  font-size: 1.15rem;
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

.btn-outline {
  background: transparent;
  color: #475569;
  border: 1px solid #cbd5e1;
}

.btn-outline:hover:not(:disabled) {
  background: #f8fafc;
  color: #1e293b;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.w-full {
  width: 100%;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.list-header h3 {
  margin: 0;
}

.select-filter {
  padding: 0.4rem 0.75rem;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 0.85rem;
}

.badge-specialty {
  background: #eff6ff;
  color: #1e40af;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.825rem;
  font-weight: 600;
}

.status-badge {
  display: inline-block;
  padding: 0.2rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 700;
}

.status-active {
  background: #f0fdf4;
  color: #15803d;
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
  padding: 2.5rem 1rem;
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
  width: 30px;
  height: 30px;
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

