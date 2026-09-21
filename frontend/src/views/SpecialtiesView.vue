<script setup>
import { ref, onMounted } from 'vue'
import { specialtyService } from '@/api'

const specialties = ref([])
const newName = ref('')
const isLoading = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

async function fetchSpecialties() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    specialties.value = await specialtyService.getAll()
  } catch (error) {
    errorMessage.value = error.message || 'Error al cargar las especialidades.'
  } finally {
    isLoading.value = false
  }
}

async function handleCreate() {
  if (!newName.value.trim()) {
    errorMessage.value = 'El nombre de la especialidad es obligatorio.'
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const created = await specialtyService.create({ name: newName.value.trim() })
    successMessage.value = `¡Especialidad "${created.name}" creada exitosamente!`
    newName.value = ''
    await fetchSpecialties()
  } catch (error) {
    errorMessage.value = error.message || 'No se pudo crear la especialidad.'
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  fetchSpecialties()
})
</script>

<template>
  <div class="view-container">
    <div class="page-header">
      <div>
        <h1>Especialidades Medicas</h1>
        <p>Consulta y registro de especialidades disponibles en Piedrazul</p>
      </div>
      <button @click="fetchSpecialties" class="btn btn-outline" :disabled="isLoading">
        🔄 Refrescar
      </button>
    </div>

    <!-- Alertas -->
    <div v-if="errorMessage" class="alert alert-error">
      {{ errorMessage }}
    </div>
    <div v-if="successMessage" class="alert alert-success">
      {{ successMessage }}
    </div>

    <div class="content-grid">
      <!-- Formulario para crear -->
      <div class="card form-card">
        <h3>Nueva Especialidad</h3>
        <form @submit.prevent="handleCreate">
          <div class="form-group">
            <label for="specialtyName">Nombre de la especialidad</label>
            <input
              id="specialtyName"
              v-model="newName"
              type="text"
              placeholder="ej. Cardiologia, Pediatria"
              required
              :disabled="isSubmitting"
            />
          </div>
          <button type="submit" class="btn btn-primary w-full" :disabled="isSubmitting">
            <span v-if="isSubmitting" class="spinner"></span>
            <span v-else>+ Registrar Especialidad</span>
          </button>
        </form>
      </div>

      <!-- Lista de especialidades -->
      <div class="card list-card">
        <h3>Especialidades Registradas ({{ specialties.length }})</h3>

        <div v-if="isLoading" class="loading-state">
          <div class="spinner-large"></div>
          <p>Cargando especialidades...</p>
        </div>

        <div v-else-if="specialties.length === 0" class="empty-state">
          <p>No hay especialidades registradas aun.</p>
        </div>

        <div v-else class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th style="width: 80px">ID</th>
                <th>Nombre</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in specialties" :key="item.id">
                <td class="cell-id">#{{ item.id }}</td>
                <td class="cell-name">
                  <strong>{{ item.name }}</strong>
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
  max-width: 1000px;
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

.content-grid {
  display: grid;
  grid-template-columns: 340px 1fr;
  gap: 1.5rem;
}

@media (max-width: 768px) {
  .content-grid {
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
  gap: 0.4rem;
  margin-bottom: 1.25rem;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #334155;
}

.form-group input {
  padding: 0.7rem 0.9rem;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.95rem;
  outline: none;
}

.form-group input:focus {
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

.cell-name {
  color: #1e293b;
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

