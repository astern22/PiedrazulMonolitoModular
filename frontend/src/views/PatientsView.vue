<script setup>
import { ref, computed, onMounted } from 'vue'
import { patientService } from '@/api'
import { useAuth } from '@/composables/useAuth'
import { validatePatientForm, hasErrors } from '@/utils/validators'

const { canManagePatients } = useAuth()

const patients = ref([])
const isLoading = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const searchQuery = ref('')
const showForm = ref(false)
const editingId = ref(null)

const form = ref({
  documentNumber: '',
  phone: '',
  birthDate: '',
  userId: '',
})

const fieldErrors = ref({})

// Filtrado de pacientes en tiempo real
const filteredPatients = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()
  if (!query) return patients.value

  return patients.value.filter(p => {
    const doc = p.documentNumber?.toLowerCase() || ''
    const phone = p.phone?.toLowerCase() || ''
    const name = p.fullName?.toLowerCase() || ''
    const email = p.email?.toLowerCase() || ''
    const id = String(p.id)
    const userId = p.userId ? String(p.userId) : ''

    return (
      doc.includes(query) ||
      phone.includes(query) ||
      name.includes(query) ||
      email.includes(query) ||
      id === query ||
      userId === query
    )
  })
})

function calculateAge(birthDate) {
  if (!birthDate) return 'N/A'
  const today = new Date()
  const birth = new Date(birthDate + 'T00:00:00')
  if (isNaN(birth.getTime())) return 'N/A'

  let age = today.getFullYear() - birth.getFullYear()
  const monthDiff = today.getMonth() - birth.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age--
  }
  return `${age} años`
}

async function fetchPatients() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    patients.value = await patientService.getAll()
  } catch (error) {
    errorMessage.value = error.message || 'Error al cargar la lista de pacientes.'
  } finally {
    isLoading.value = false
  }
}

function openCreateForm() {
  editingId.value = null
  form.value = {
    documentNumber: '',
    phone: '',
    birthDate: '',
    userId: '',
  }
  fieldErrors.value = {}
  showForm.value = true
}

function startEdit(patient) {
  editingId.value = patient.id
  form.value = {
    documentNumber: patient.documentNumber || '',
    phone: patient.phone || '',
    birthDate: patient.birthDate || '',
    userId: patient.userId ? String(patient.userId) : '',
  }
  fieldErrors.value = {}
  showForm.value = true
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function cancelForm() {
  showForm.value = false
  editingId.value = null
  fieldErrors.value = {}
}

async function handleSubmit() {
  errorMessage.value = ''
  successMessage.value = ''

  fieldErrors.value = validatePatientForm(form.value)
  if (hasErrors(fieldErrors.value)) {
    errorMessage.value = 'Por favor corrige los errores en el formulario.'
    return
  }

  isSubmitting.value = true

  try {
    if (editingId.value) {
      await patientService.update(editingId.value, form.value)
      successMessage.value = `¡Paciente #${editingId.value} actualizado con exito!`
    } else {
      const created = await patientService.create(form.value)
      successMessage.value = `¡Paciente #${created.id} registrado con exito!`
    }
    cancelForm()
    await fetchPatients()
  } catch (error) {
    errorMessage.value = error.message || 'Error al procesar la solicitud del paciente.'
  } finally {
    isSubmitting.value = false
  }
}

async function handleDelete(id) {
  if (!confirm(`¿Estas seguro de que deseas eliminar al paciente #${id}?`)) {
    return
  }

  errorMessage.value = ''
  successMessage.value = ''
  try {
    await patientService.delete(id)
    successMessage.value = `Paciente #${id} eliminado correctamente.`
    await fetchPatients()
  } catch (error) {
    errorMessage.value = error.message || `No se pudo eliminar al paciente #${id}.`
  }
}

onMounted(() => {
  fetchPatients()
})
</script>

<template>
  <div class="view-container">
    <div class="page-header">
      <div>
        <div class="header-badge">Modulo Medico</div>
        <h1>Gestion de Pacientes</h1>
        <p>Administracion y registro de la informacion clinica y personal de los pacientes</p>
      </div>
      <div class="header-actions">
        <button
          v-if="canManagePatients"
          @click="showForm ? cancelForm() : openCreateForm()"
          class="btn"
          :class="showForm ? 'btn-secondary' : 'btn-primary'"
        >
          {{ showForm ? 'Cancelar' : '+ Nuevo Paciente' }}
        </button>
        <button @click="fetchPatients" class="btn btn-outline" :disabled="isLoading">
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

    <!-- Formulario para Registrar / Editar Paciente -->
    <div v-if="showForm && canManagePatients" class="card form-card">
      <div class="form-card-header">
        <h3>{{ editingId ? `Editar Paciente #${editingId}` : 'Registrar Nuevo Paciente' }}</h3>
        <button @click="cancelForm" class="btn-close" title="Cerrar formulario">&times;</button>
      </div>

      <form @submit.prevent="handleSubmit" novalidate>
        <div class="form-grid">
          <div class="form-group">
            <label for="docNumber">Numero de Documento *</label>
            <input
              id="docNumber"
              v-model="form.documentNumber"
              type="text"
              placeholder="ej. 1061789234"
              :disabled="isSubmitting"
            />
            <span v-if="fieldErrors.documentNumber" class="field-error">{{ fieldErrors.documentNumber }}</span>
          </div>

          <div class="form-group">
            <label for="patientPhone">Telefono de Contacto</label>
            <input
              id="patientPhone"
              v-model="form.phone"
              type="tel"
              placeholder="ej. 3001234567"
              :disabled="isSubmitting"
            />
            <span v-if="fieldErrors.phone" class="field-error">{{ fieldErrors.phone }}</span>
          </div>

          <div class="form-group">
            <label for="birthDate">Fecha de Nacimiento</label>
            <input
              id="birthDate"
              v-model="form.birthDate"
              type="date"
              :disabled="isSubmitting"
            />
            <span v-if="fieldErrors.birthDate" class="field-error">{{ fieldErrors.birthDate }}</span>
          </div>

          <div class="form-group">
            <label for="userId">ID de Usuario Vinculado (Opcional)</label>
            <input
              id="userId"
              v-model="form.userId"
              type="number"
              placeholder="ej. 3"
              :disabled="isSubmitting"
            />
            <span v-if="fieldErrors.userId" class="field-error">{{ fieldErrors.userId }}</span>
          </div>
        </div>

        <div class="form-actions mt-3">
          <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
            <span v-if="isSubmitting" class="spinner"></span>
            <span v-else>{{ editingId ? 'Guardar Cambios' : 'Registrar Paciente' }}</span>
          </button>
          <button type="button" @click="cancelForm" class="btn btn-outline" :disabled="isSubmitting">
            Cancelar
          </button>
        </div>
      </form>
    </div>

    <!-- Barra de Busqueda -->
    <div class="card search-card">
      <div class="search-box">
        <label for="searchInput" class="search-label">Buscar Pacientes</label>
        <div class="search-input-wrapper">
          <input
            id="searchInput"
            v-model="searchQuery"
            type="text"
            placeholder="Buscar por cedula, nombre, telefono o ID..."
            class="search-input"
          />
          <button
            v-if="searchQuery"
            @click="searchQuery = ''"
            class="btn-clear-search"
            title="Limpiar busqueda"
          >
            &times;
          </button>
        </div>
      </div>
    </div>

    <!-- Tabla de Pacientes -->
    <div class="card list-card">
      <div class="list-header">
        <h3>
          Pacientes Registrados ({{ filteredPatients.length }})
        </h3>
      </div>

      <div v-if="isLoading" class="loading-state">
        <div class="spinner-large"></div>
        <p>Cargando informacion de pacientes...</p>
      </div>

      <div v-else-if="filteredPatients.length === 0" class="empty-state">
        <p v-if="searchQuery">No se encontraron pacientes que coincidan con "{{ searchQuery }}".</p>
        <p v-else>No hay pacientes registrados en el sistema.</p>
      </div>

      <div v-else class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th style="width: 70px">ID</th>
              <th>Documento</th>
              <th>Nombre / Usuario</th>
              <th>Telefono</th>
              <th>F. Nacimiento</th>
              <th>Edad</th>
              <th style="text-align: right">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="patient in filteredPatients" :key="patient.id">
              <td class="cell-id">#{{ patient.id }}</td>
              <td class="cell-doc">
                <strong>{{ patient.documentNumber }}</strong>
              </td>
              <td>
                <div v-if="patient.fullName" class="user-info">
                  <span class="user-fullname">{{ patient.fullName }}</span>
                  <span v-if="patient.email" class="user-email">{{ patient.email }}</span>
                </div>
                <span v-else-if="patient.userId" class="badge-user">
                  Usuario #{{ patient.userId }}
                </span>
                <span v-else class="text-muted-xs">Sin vincular</span>
              </td>
              <td>
                <span v-if="patient.phone">{{ patient.phone }}</span>
                <span v-else class="text-muted-xs">—</span>
              </td>
              <td>
                <span v-if="patient.birthDate">{{ patient.birthDate }}</span>
                <span v-else class="text-muted-xs">—</span>
              </td>
              <td>
                <span class="badge-age">{{ calculateAge(patient.birthDate) }}</span>
              </td>
              <td style="text-align: right">
                <div class="actions-cell">
                  <button
                    v-if="canManagePatients"
                    @click="startEdit(patient)"
                    class="btn-action btn-edit"
                    title="Editar informacion"
                  >
                    Editar
                  </button>
                  <button
                    v-if="canManagePatients"
                    @click="handleDelete(patient.id)"
                    class="btn-action btn-delete"
                    title="Eliminar paciente"
                  >
                    Eliminar
                  </button>
                </div>
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

.header-badge {
  display: inline-block;
  background: #eff6ff;
  color: #2563eb;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.2rem 0.6rem;
  border-radius: 9999px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.4rem;
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

.form-card {
  border-left: 4px solid #2563eb;
}

.form-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
}

.form-card-header h3 {
  margin: 0;
  color: #1e293b;
  font-size: 1.2rem;
}

.btn-close {
  background: transparent;
  border: none;
  font-size: 1.5rem;
  line-height: 1;
  color: #94a3b8;
  cursor: pointer;
  padding: 0 0.25rem;
}

.btn-close:hover {
  color: #334155;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  text-align: left;
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
  background: white;
}

.form-group input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.field-error {
  color: #dc2626;
  font-size: 0.75rem;
  margin-top: 0.15rem;
}

.form-actions {
  display: flex;
  gap: 0.75rem;
}

.mt-3 {
  margin-top: 1.25rem;
}

.search-card {
  padding: 1rem 1.25rem;
}

.search-box {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.search-label {
  font-size: 0.8rem;
  font-weight: 600;
  color: #475569;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-input {
  width: 100%;
  padding: 0.7rem 2.2rem 0.7rem 0.9rem;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.95rem;
  outline: none;
}

.search-input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.btn-clear-search {
  position: absolute;
  right: 0.6rem;
  background: transparent;
  border: none;
  font-size: 1.2rem;
  color: #94a3b8;
  cursor: pointer;
}

.btn-clear-search:hover {
  color: #334155;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.list-header h3 {
  margin: 0;
  color: #1e293b;
  font-size: 1.15rem;
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

.cell-doc {
  color: #1e293b;
  font-family: monospace;
  font-size: 0.95rem;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.user-fullname {
  font-weight: 600;
  color: #1e293b;
}

.user-email {
  font-size: 0.75rem;
  color: #64748b;
}

.badge-user {
  background: #f1f5f9;
  color: #475569;
  padding: 0.2rem 0.5rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
}

.badge-age {
  background: #f0fdf4;
  color: #166534;
  padding: 0.2rem 0.5rem;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
}

.text-muted-xs {
  color: #94a3b8;
  font-size: 0.85rem;
}

.actions-cell {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.btn-action {
  padding: 0.35rem 0.7rem;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.15s ease;
}

.btn-edit {
  background: #eff6ff;
  color: #2563eb;
  border-color: #bfdbfe;
}

.btn-edit:hover {
  background: #2563eb;
  color: white;
}

.btn-delete {
  background: #fef2f2;
  color: #dc2626;
  border-color: #fecaca;
}

.btn-delete:hover {
  background: #dc2626;
  color: white;
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
  gap: 0.5rem;
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

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

