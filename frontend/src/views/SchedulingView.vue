<script setup>
import { ref, computed, onMounted } from 'vue'
import { schedulingService, professionalService } from '@/api'
import { useAuth } from '@/composables/useAuth'
import { validateIntervalMinutes, findOverlappingAvailability } from '@/utils/validators'
import { professionalLabel, professionalName } from '@/utils/professionals'

const { canManage } = useAuth()

// ─── Constantes de la grilla ───
const DAYS = [
  { value: 1, short: 'Lun', name: 'Lunes' },
  { value: 2, short: 'Mar', name: 'Martes' },
  { value: 3, short: 'Mie', name: 'Miercoles' },
  { value: 4, short: 'Jue', name: 'Jueves' },
  { value: 5, short: 'Vie', name: 'Viernes' },
  { value: 6, short: 'Sab', name: 'Sabado' },
  { value: 7, short: 'Dom', name: 'Domingo' },
]
const STEP = 30 
const DAY_START = 6 * 60 
const DAY_END = 21 * 60 
const PX_PER_MIN = 0.7
const PRESETS = [
  { label: 'Manana', start: '08:00', end: '12:00' },
  { label: 'Tarde', start: '14:00', end: '18:00' },
  { label: 'Jornada completa', start: '08:00', end: '18:00' },
]

const professionals = ref([])
const selectedProfId = ref('')
const weeklyAvailabilities = ref([])
const isLoadingAvailabilities = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// Nuevo horario: dias marcados + rango de horas
const selectedDays = ref([1])
const startTime = ref('08:00')
const endTime = ref('12:00')
const isSaving = ref(false)

// Duracion de las citas
const intervalMinutes = ref('')
const intervalError = ref('')
const isSavingInterval = ref(false)

// Consulta de franjas libres
const localToday = () => {
  const d = new Date()
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
const slotDate = ref(localToday())
const availableSlots = ref([])
const isLoadingSlots = ref(false)
const hasQueriedSlots = ref(false)

// ─── Helpers de hora ───
const toMinutes = t => {
  const [h, m] = String(t).substring(0, 5).split(':').map(Number)
  return h * 60 + m
}
const toTime = min => `${String(Math.floor(min / 60)).padStart(2, '0')}:${String(min % 60).padStart(2, '0')}`
const formatHour = time => String(time).substring(0, 5)
const dayName = n => DAYS.find(d => d.value === Number(n))?.name || ''

// Opciones de hora cada 30 min (06:00 a 21:00)
const timeOptions = []
for (let m = DAY_START; m <= DAY_END; m += STEP) timeOptions.push(toTime(m))
const endOptions = computed(() => timeOptions.filter(t => toMinutes(t) > toMinutes(startTime.value)))

const selectedProfessional = computed(
  () => professionals.value.find(p => String(p.id) === String(selectedProfId.value)) || null
)

// ─── Estado de la grilla ───
// Si hay horarios fuera del rango 06-21 se amplia para que se vean todos
const gridRange = computed(() => {
  let start = DAY_START
  let end = DAY_END
  for (const a of weeklyAvailabilities.value) {
    start = Math.min(start, Math.floor(toMinutes(a.startTime) / 60) * 60)
    end = Math.max(end, Math.ceil(toMinutes(a.endTime) / 60) * 60)
  }
  return { start, end }
})
const gridHeight = computed(() => (gridRange.value.end - gridRange.value.start) * PX_PER_MIN)
const hourLabels = computed(() => {
  const labels = []
  for (let m = gridRange.value.start; m < gridRange.value.end; m += 60) labels.push(m)
  return labels
})

function blockStyle(startMin, endMin) {
  return {
    top: `${(startMin - gridRange.value.start) * PX_PER_MIN}px`,
    height: `${(endMin - startMin) * PX_PER_MIN}px`,
  }
}

// Horarios agrupados por dia
const blocksByDay = computed(() => {
  const map = {}
  DAYS.forEach(d => (map[d.value] = []))
  ;[...weeklyAvailabilities.value]
    .sort((a, b) => toMinutes(a.startTime) - toMinutes(b.startTime))
    .forEach(a => map[a.dayOfWeek]?.push(a))
  return map
})

// IDs de horarios que se cruzan entre si (datos guardados antes de existir la validacion)
const overlappingIds = computed(() => {
  const ids = new Set()
  for (const day of DAYS) {
    const list = blocksByDay.value[day.value]
    for (let i = 0; i < list.length; i++) {
      for (let j = i + 1; j < list.length; j++) {
        if (toMinutes(list[i].startTime) < toMinutes(list[j].endTime) &&
            toMinutes(list[i].endTime) > toMinutes(list[j].startTime)) {
          ids.add(list[i].id)
          ids.add(list[j].id)
        }
      }
    }
  }
  return ids
})

// ─── Vista previa del nuevo horario y deteccion de cruces ───
const rangeIsValid = computed(() => toMinutes(endTime.value) > toMinutes(startTime.value))

const conflictsByDay = computed(() => {
  const found = {}
  if (!rangeIsValid.value) return found
  for (const dayValue of selectedDays.value) {
    const overlap = findOverlappingAvailability(weeklyAvailabilities.value, {
      dayOfWeek: dayValue,
      startTime: startTime.value,
      endTime: endTime.value,
    })
    if (overlap) found[dayValue] = overlap
  }
  return found
})
const hasConflicts = computed(() => Object.keys(conflictsByDay.value).length > 0)

const durationMinutes = computed(() =>
  rangeIsValid.value ? toMinutes(endTime.value) - toMinutes(startTime.value) : 0
)
const appointmentCount = computed(() => {
  const interval = selectedProfessional.value?.appointmentIntervalMinutes
  return interval ? Math.floor(durationMinutes.value / interval) : 0
})
const tooShort = computed(
  () => rangeIsValid.value && appointmentCount.value < 1 && !!selectedProfessional.value
)

const canSave = computed(
  () => selectedDays.value.length > 0 && rangeIsValid.value && !hasConflicts.value && !tooShort.value && !isSaving.value
)

const summaryText = computed(() => {
  if (selectedDays.value.length === 0) return 'Elige al menos un dia.'
  if (!rangeIsValid.value) return 'La hora de fin debe ser posterior a la de inicio.'
  if (hasConflicts.value) {
    return Object.entries(conflictsByDay.value)
      .map(([d, c]) => `${dayName(d)}: se cruza con ${formatHour(c.startTime)} - ${formatHour(c.endTime)}`)
      .join(' · ')
  }
  if (tooShort.value) {
    return `El rango es menor que la duracion de una cita (${selectedProfessional.value.appointmentIntervalMinutes} min).`
  }
  const days = DAYS.filter(d => selectedDays.value.includes(d.value)).map(d => d.short).join(', ')
  return `${days} de ${startTime.value} a ${endTime.value} · alcanza para ${appointmentCount.value} citas de ${selectedProfessional.value?.appointmentIntervalMinutes} min por dia`
})

// ─── Interacciones del editor ───
function toggleDay(value) {
  selectedDays.value = selectedDays.value.includes(value)
    ? selectedDays.value.filter(d => d !== value)
    : [...selectedDays.value, value].sort((a, b) => a - b)
}
function selectWeekdays() { selectedDays.value = [1, 2, 3, 4, 5] }
function applyPreset(p) {
  startTime.value = p.start
  endTime.value = p.end
}
function onStartChange() {
  if (!rangeIsValid.value) endTime.value = toTime(Math.min(toMinutes(startTime.value) + 120, DAY_END))
}

// Clic en un espacio vacio de la grilla: pre-llena ese dia y esa hora
function onColumnClick(event, dayValue) {
  if (!canManage.value || event.target.closest('.block')) return
  const rect = event.currentTarget.getBoundingClientRect()
  const minutes = gridRange.value.start + (event.clientY - rect.top) / PX_PER_MIN
  const start = Math.min(Math.floor(minutes / STEP) * STEP, DAY_END - STEP)
  selectedDays.value = [dayValue]
  startTime.value = toTime(start)
  endTime.value = toTime(Math.min(start + 240, DAY_END))
}

// ─── Carga de datos ───
async function loadProfessionals(keepSelection = false) {
  try {
    professionals.value = await professionalService.getActive()
    if (professionals.value.length === 0) return
    const stillExists = professionals.value.some(p => String(p.id) === String(selectedProfId.value))
    if (!keepSelection || !stillExists) selectedProfId.value = professionals.value[0].id
    syncInterval()
    await fetchAvailabilities()
  } catch (error) {
    console.error('Error al cargar lista de profesionales:', error)
  }
}

function syncInterval() {
  intervalMinutes.value = selectedProfessional.value?.appointmentIntervalMinutes ?? ''
  intervalError.value = ''
}

async function handleProfessionalChange() {
  errorMessage.value = ''
  successMessage.value = ''
  availableSlots.value = []
  hasQueriedSlots.value = false
  syncInterval()
  await fetchAvailabilities()
}

async function fetchAvailabilities() {
  if (!selectedProfId.value) return
  isLoadingAvailabilities.value = true
  try {
    weeklyAvailabilities.value = await schedulingService.getByProfessional(selectedProfId.value)
  } catch (error) {
    errorMessage.value = error.message || 'Error al consultar disponibilidad semanal.'
  } finally {
    isLoadingAvailabilities.value = false
  }
}

async function handleSave() {
  if (!canSave.value) return
  errorMessage.value = ''
  successMessage.value = ''
  isSaving.value = true

  const saved = []
  const failed = []
  for (const dayValue of selectedDays.value) {
    try {
      await schedulingService.createWeeklyAvailability({
        professionalId: Number(selectedProfId.value),
        dayOfWeek: dayValue,
        startTime: startTime.value,
        endTime: endTime.value,
      })
      saved.push(dayName(dayValue))
    } catch (error) {
      failed.push(`${dayName(dayValue)} (${error.message || 'error'})`)
    }
  }

  if (saved.length) successMessage.value = `Horario ${startTime.value} - ${endTime.value} guardado: ${saved.join(', ')}.`
  if (failed.length) errorMessage.value = `No se pudo guardar: ${failed.join('; ')}.`
  await fetchAvailabilities()
  isSaving.value = false
}

async function handleDeactivate(item) {
  if (!canManage.value) return
  if (!confirm(`¿Quitar el horario del ${dayName(item.dayOfWeek)} ${formatHour(item.startTime)} - ${formatHour(item.endTime)}?`)) return
  errorMessage.value = ''
  successMessage.value = ''
  try {
    await schedulingService.deactivate(item.id)
    successMessage.value = 'Horario eliminado correctamente.'
    await fetchAvailabilities()
  } catch (error) {
    errorMessage.value = error.message || 'Error al desactivar la disponibilidad.'
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
    const updated = await professionalService.updateAppointmentInterval(selectedProfId.value, intervalMinutes.value)
    successMessage.value = `Duracion de cita de ${professionalName(updated)} actualizada a ${updated.appointmentIntervalMinutes} minutos. Las citas ya agendadas no se modifican.`
    await loadProfessionals(true)
    if (hasQueriedSlots.value) await handleQuerySlots()
  } catch (error) {
    errorMessage.value = error.message || 'No se pudo actualizar la duracion de la cita.'
  } finally {
    isSavingInterval.value = false
  }
}

async function handleQuerySlots() {
  if (!selectedProfId.value || !slotDate.value) {
    errorMessage.value = 'Selecciona el profesional y la fecha para consultar las franjas libres.'
    return
  }
  isLoadingSlots.value = true
  hasQueriedSlots.value = true
  errorMessage.value = ''
  try {
    availableSlots.value = await schedulingService.getAvailableSlots(selectedProfId.value, slotDate.value)
  } catch (error) {
    errorMessage.value = error.message || 'Error al obtener las franjas horarias libres.'
    availableSlots.value = []
  } finally {
    isLoadingSlots.value = false
  }
}

onMounted(() => loadProfessionals())
</script>

<template>
  <div class="view-container">
    <div class="page-header">
      <div>
        <h1>Horarios de Atencion</h1>
        <p>Define cuando atiende cada profesional y consulta las franjas libres para citas</p>
      </div>
      <div class="prof-picker">
        <label for="profSelect">Profesional</label>
        <select id="profSelect" v-model="selectedProfId" @change="handleProfessionalChange">
          <option value="" disabled>Selecciona un profesional</option>
          <option v-for="prof in professionals" :key="prof.id" :value="prof.id">
            {{ professionalLabel(prof) }}
          </option>
        </select>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>
    <div v-if="successMessage" class="alert alert-success">{{ successMessage }}</div>

    <div v-if="professionals.length === 0" class="card empty-state">
      <p>Aun no hay profesionales activos. Registralos primero en la seccion Profesionales.</p>
    </div>

    <template v-else>
      <div class="layout-grid">
        <div class="column">
          <!-- ═══ Agregar horario (ADMIN / SCHEDULER) ═══ -->
          <div v-if="canManage" class="card">
            <h3>Agregar horario</h3>

            <div class="form-group">
              <label>1. ¿Que dias?</label>
              <div class="chips">
                <button v-for="d in DAYS" :key="d.value" type="button" class="chip" :class="{ on: selectedDays.includes(d.value) }" @click="toggleDay(d.value)">{{ d.short }}</button>
              </div>
              <button type="button" class="link-btn" @click="selectWeekdays">Lunes a viernes</button>
            </div>

            <div class="form-group">
              <label>2. ¿En que horario?</label>
              <div class="chips">
                <button v-for="p in PRESETS" :key="p.label" type="button" class="chip" :class="{ on: startTime === p.start && endTime === p.end }" @click="applyPreset(p)">
                  {{ p.label }} <small>{{ p.start }}-{{ p.end }}</small>
                </button>
              </div>
              <div class="time-row">
                <div>
                  <small>Desde</small>
                  <select v-model="startTime" @change="onStartChange" :disabled="isSaving">
                    <option v-for="t in timeOptions.slice(0, -1)" :key="t" :value="t">{{ t }}</option>
                  </select>
                </div>
                <div>
                  <small>Hasta</small>
                  <select v-model="endTime" :disabled="isSaving">
                    <option v-for="t in endOptions" :key="t" :value="t">{{ t }}</option>
                  </select>
                </div>
              </div>
            </div>

            <p class="summary" :class="{ 'summary-bad': hasConflicts || tooShort || !rangeIsValid || selectedDays.length === 0 }">{{ summaryText }}</p>

            <button type="button" class="btn btn-primary w-full" :disabled="!canSave" @click="handleSave">
              <span v-if="isSaving" class="spinner"></span>
              <span v-else>Guardar horario</span>
            </button>
          </div>

          <!-- ═══ Duracion de citas ═══ -->
          <div v-if="selectedProfessional" class="card" :class="{ 'mt-4': canManage }">
            <h3>Duracion de las Citas</h3>
            <p class="section-desc">
              Tiempo que dura cada cita de {{ professionalName(selectedProfessional) }}. Las citas ya agendadas no se modifican.
            </p>
            <form v-if="canManage" @submit.prevent="handleSaveInterval" class="interval-form" novalidate>
              <div class="form-group interval-input">
                <label for="intervalMinutes">Minutos por cita</label>
                <input id="intervalMinutes" v-model="intervalMinutes" type="number" min="5" max="480" step="5" :disabled="isSavingInterval" />
              </div>
              <button type="submit" class="btn btn-primary" :disabled="isSavingInterval">
                <span v-if="isSavingInterval" class="spinner"></span>
                <span v-else>Guardar</span>
              </button>
            </form>
            <span v-if="intervalError" class="field-error">{{ intervalError }}</span>
            <p v-if="!canManage" class="interval-readonly">
              <strong>{{ selectedProfessional.appointmentIntervalMinutes }} minutos</strong> por cita
            </p>
          </div>
        </div>

        <!-- ═══ Franjas libres ═══ -->
        <div class="column">
          <div class="card">
            <h3>Franjas libres para citas</h3>
            <p class="section-desc">Elige una fecha para ver los espacios que aun estan disponibles.</p>

            <form @submit.prevent="handleQuerySlots" class="slots-query-form">
              <div class="form-group">
                <label for="slotDate">Fecha</label>
                <input id="slotDate" v-model="slotDate" type="date" required />
              </div>
              <button type="submit" class="btn btn-primary" :disabled="isLoadingSlots">
                <span v-if="isLoadingSlots" class="spinner"></span>
                <span v-else>Ver franjas</span>
              </button>
            </form>

            <div class="slots-result mt-4">
              <div v-if="isLoadingSlots" class="loading-state"><p>Calculando franjas...</p></div>
              <div v-else-if="hasQueriedSlots && availableSlots.length === 0" class="empty-state">
                <p>No hay franjas disponibles ese dia.</p>
                <small>Verifica que el profesional atienda ese dia de la semana y que la agenda no este llena.</small>
              </div>
              <div v-else-if="availableSlots.length > 0">
                <h4>{{ availableSlots.length }} franjas disponibles</h4>
                <div class="slots-grid">
                  <div v-for="(slot, idx) in availableSlots" :key="idx" class="slot-card">
                    {{ formatHour(slot.startTime) }} - {{ formatHour(slot.endTime) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ═══ Semana visual ═══ -->
      <div class="card mt-4">
        <div class="card-head">
          <h3>Semana de {{ professionalName(selectedProfessional) }}</h3>
          <span class="legend"><i class="dot dot-block"></i> Atiende <i class="dot dot-new"></i> Nuevo <i class="dot dot-bad"></i> Se cruza</span>
        </div>
        <p class="section-desc" v-if="canManage">
          Las franjas moradas son los horarios actuales. Haz clic en un espacio vacio para empezar uno nuevo, o usa el panel de arriba.
        </p>

        <div v-if="overlappingIds.size > 0" class="alert alert-warning">
          Hay horarios que se cruzan entre si (en rojo). Quita el que sobre para evitar franjas repetidas.
        </div>

        <div class="week-scroll">
          <div class="week">
            <div class="week-corner"></div>
            <div v-for="d in DAYS" :key="d.value" class="week-head" :class="{ 'is-selected': canManage && selectedDays.includes(d.value) }">{{ d.short }}</div>

            <div class="hours-col" :style="{ height: gridHeight + 'px' }">
              <span v-for="m in hourLabels" :key="m" class="hour-label" :style="{ top: (m - gridRange.start) * PX_PER_MIN + 'px' }">{{ toTime(m) }}</span>
            </div>

            <div
              v-for="d in DAYS"
              :key="d.value"
              class="day-col"
              :class="{ clickable: canManage }"
              :style="{ height: gridHeight + 'px' }"
              @click="onColumnClick($event, d.value)"
            >
              <div v-for="m in hourLabels" :key="m" class="hour-line" :style="{ top: (m - gridRange.start) * PX_PER_MIN + 'px' }"></div>

              <div
                v-for="item in blocksByDay[d.value]"
                :key="item.id"
                class="block"
                :class="{ 'block-bad': overlappingIds.has(item.id) }"
                :style="blockStyle(toMinutes(item.startTime), toMinutes(item.endTime))"
                :title="`${d.name} ${formatHour(item.startTime)} - ${formatHour(item.endTime)}`"
              >
                <span>{{ formatHour(item.startTime) }}<br />{{ formatHour(item.endTime) }}</span>
                <button v-if="canManage" type="button" class="block-x" title="Quitar horario" @click.stop="handleDeactivate(item)">×</button>
              </div>

              <!-- Vista previa del horario que se esta creando -->
              <div
                v-if="canManage && selectedDays.includes(d.value) && rangeIsValid"
                class="block block-new"
                :class="{ 'block-new-bad': conflictsByDay[d.value] }"
                :style="blockStyle(toMinutes(startTime), toMinutes(endTime))"
              >
                <span>{{ startTime }}<br />{{ endTime }}</span>
              </div>
            </div>
          </div>
        </div>
        <p v-if="!isLoadingAvailabilities && weeklyAvailabilities.length === 0" class="hint-empty">
          Este profesional aun no tiene horarios registrados.
        </p>
      </div>
    </template>
  </div>
</template>

<style scoped>
.view-container { max-width: 1100px; margin: 0 auto; padding: 2rem 1rem; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; flex-wrap: wrap; gap: 1rem; margin-bottom: 1.5rem; }
.page-header h1 { font-size: 1.8rem; color: #0f172a; margin: 0 0 0.25rem; }
.page-header p { color: #64748b; margin: 0; font-size: 0.95rem; }
.prof-picker { display: flex; flex-direction: column; gap: 0.25rem; min-width: 240px; }
.prof-picker label { font-size: 0.8rem; font-weight: 600; color: #334155; }
.prof-picker select { padding: 0.6rem 0.85rem; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 0.95rem; background: white; }

.layout-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; margin-top: 1.5rem; align-items: start; }
@media (max-width: 850px) { .layout-grid { grid-template-columns: 1fr; } }
.card { background: white; border-radius: 12px; padding: 1.5rem; border: 1px solid #e2e8f0; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.card h3 { margin: 0 0 0.5rem; color: #1e293b; font-size: 1.15rem; }
.card-head { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.5rem; }
.section-desc { color: #64748b; font-size: 0.875rem; margin: 0 0 1rem; }
.mt-4 { margin-top: 1.5rem; }
.w-full { width: 100%; }

/* Semana */
.legend { font-size: 0.75rem; color: #64748b; display: flex; align-items: center; gap: 0.35rem; }
.dot { display: inline-block; width: 10px; height: 10px; border-radius: 3px; margin-left: 0.5rem; }
.dot-block { background: #7c3aed; } .dot-new { background: #bbf7d0; border: 1px dashed #16a34a; } .dot-bad { background: #dc2626; }
.week-scroll { overflow-x: auto; }
.week { display: grid; grid-template-columns: 44px repeat(7, minmax(56px, 1fr)); min-width: 460px; }
.week-head { text-align: center; font-size: 0.8rem; font-weight: 700; color: #475569; padding: 0.4rem 0; border-bottom: 2px solid #e2e8f0; }
.week-head.is-selected { color: #4d1d93; border-bottom-color: #4d1d93; }
.hours-col, .day-col { position: relative; }
.hour-label { position: absolute; right: 6px; transform: translateY(-50%); font-size: 0.68rem; color: #94a3b8; }
.day-col { border-left: 1px solid #f1f5f9; background: #fff; }
.day-col.clickable { cursor: pointer; }
.day-col.clickable:hover { background: #faf5ff; }
.hour-line { position: absolute; left: 0; right: 0; border-top: 1px solid #f1f5f9; pointer-events: none; }
.block { position: absolute; left: 3px; right: 3px; border-radius: 6px; background: #7c3aed; color: white; font-size: 0.68rem; line-height: 1.25; padding: 3px 4px; overflow: hidden; box-sizing: border-box; cursor: default; }
.block-bad { background: #dc2626; }
.block-x { position: absolute; top: 1px; right: 2px; background: rgba(255,255,255,0.25); color: white; border: none; border-radius: 4px; width: 16px; height: 16px; line-height: 14px; font-size: 0.85rem; cursor: pointer; padding: 0; }
.block-x:hover { background: rgba(255,255,255,0.5); }
.block-new { background: rgba(187,247,208,0.7); border: 2px dashed #16a34a; color: #166534; pointer-events: none; z-index: 2; }
.block-new-bad { background: rgba(254,202,202,0.75); border-color: #dc2626; color: #991b1b; }
.hint-empty { text-align: center; color: #64748b; font-size: 0.85rem; margin: 0.75rem 0 0; }

/* Editor */
.form-group { display: flex; flex-direction: column; gap: 0.4rem; margin-bottom: 1rem; }
.form-group label { font-size: 0.85rem; font-weight: 700; color: #334155; }
.form-group input, select { padding: 0.6rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 0.9rem; background: white; width: 100%; box-sizing: border-box; }
.chips { display: flex; flex-wrap: wrap; gap: 0.4rem; }
.chip { padding: 0.45rem 0.8rem; border-radius: 9999px; border: 1px solid #cbd5e1; background: white; color: #475569; font-size: 0.85rem; font-weight: 600; cursor: pointer; }
.chip small { font-weight: 400; opacity: 0.8; }
.chip:hover { border-color: #4d1d93; }
.chip.on { background: #4d1d93; border-color: #4d1d93; color: white; }
.link-btn { align-self: flex-start; background: none; border: none; color: #4d1d93; font-size: 0.8rem; font-weight: 600; cursor: pointer; padding: 0; text-decoration: underline; }
.time-row { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; margin-top: 0.4rem; }
.time-row small { display: block; color: #64748b; margin-bottom: 0.2rem; }
.summary { background: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; border-radius: 8px; padding: 0.6rem 0.8rem; font-size: 0.85rem; margin: 0 0 1rem; }
.summary-bad { background: #fef2f2; color: #991b1b; border-color: #fecaca; }
.field-error { color: #dc2626; font-size: 0.75rem; }

.btn { padding: 0.7rem 1.25rem; border-radius: 8px; font-weight: 600; font-size: 0.9rem; cursor: pointer; border: none; display: inline-flex; align-items: center; justify-content: center; gap: 0.5rem; }
.btn-primary { background: #4d1d93; color: white; }
.btn-primary:hover:not(:disabled) { background: #4d2785; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.interval-form { display: flex; gap: 0.75rem; align-items: flex-end; }
.interval-input { flex: 1; margin-bottom: 0; }
.interval-readonly { margin: 0; color: #334155; }
.slots-query-form { display: flex; gap: 0.75rem; align-items: flex-end; }
.slots-query-form .form-group { flex: 1; margin-bottom: 0; }
.slots-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(120px, 1fr)); gap: 0.5rem; }
.slot-card { background: #eff6ff; border: 1px solid #dbeafe; color: #1e40af; border-radius: 8px; padding: 0.5rem; text-align: center; font-size: 0.85rem; font-weight: 600; }
.alert { padding: 0.75rem 1rem; border-radius: 8px; font-size: 0.875rem; margin-bottom: 1rem; }
.alert-error { background: #fef2f2; color: #991b1b; border: 1px solid #fecaca; }
.alert-success { background: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; }
.alert-warning { background: #fffbeb; color: #92400e; border: 1px solid #fde68a; }
.loading-state, .empty-state { padding: 1.5rem 1rem; text-align: center; color: #64748b; }
.spinner { width: 16px; height: 16px; border: 2px solid white; border-top-color: transparent; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>