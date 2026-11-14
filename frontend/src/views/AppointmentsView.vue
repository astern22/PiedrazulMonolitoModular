<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import {
  appointmentService,
  schedulingService,
  professionalService,
  patientService,
} from "@/api";
import { useAuth } from "@/composables/useAuth";
import { validateAppointmentForm, hasErrors } from "@/utils/validators";
import { professionalLabel, professionalNameById } from "@/utils/professionals";

const router = useRouter();
const { canManage, isPatient, isDoctor, isProfessional, isScheduler, isAdmin } = useAuth();
const canConsultHistory = computed(
  () =>
    !isPatient.value &&
    (isDoctor.value ||
      isProfessional.value ||
      isScheduler.value ||
      isAdmin.value ||
      canManage.value),
);

const appointments = ref([]);
// Todos los profesionales (para mostrar el nombre en la tabla) y los activos (para elegir)
const allProfessionals = ref([]);
const professionals = computed(() =>
  allProfessionals.value.filter((prof) => prof.active !== false),
);
const fieldErrors = ref({});
const isLoading = ref(false);
const isSubmitting = ref(false);
const isExporting = ref(false);
const errorMessage = ref("");
const successMessage = ref("");
const showCreateForm = ref(false);
const isFiltered = ref(false);

// Id del paciente vinculado al usuario autenticado (solo rol paciente)
const currentPatient = ref(null);
const isLoadingCurrentPatient = ref(false);

// Busqueda de paciente por cedula (rol agendador/admin)
const patientSearch = ref({
  documentNumber: "",
  result: null,
  isSearching: false,
  error: "",
});

// Historial de citas por cedula (E4-HU3: MEDICO, AGENDADOR, TERAPISTA, ADMIN)
const historySearch = ref({
  documentNumber: "",
  isLoading: false,
  hasSearched: false,
  error: "",
  patient: null,
  appointments: [],
  isWarningNotFound: false,
});

function validateHistoryDocument() {
  const doc = historySearch.value.documentNumber.trim();
  if (!doc || !/^\d+$/.test(doc)) {
    historySearch.value.error =
      "El campo es obligatorio y solo acepta caracteres numéricos válidos.";
    return false;
  }
  historySearch.value.error = "";
  return true;
}

function onHistoryDocumentInput() {
  if (historySearch.value.error && !historySearch.value.isWarningNotFound) {
    validateHistoryDocument();
  }
}

async function handleConsultHistory() {
  historySearch.value.isWarningNotFound = false;
  historySearch.value.patient = null;
  historySearch.value.appointments = [];

  // CA4: Validacion de formato de documento invalido
  if (!validateHistoryDocument()) {
    historySearch.value.hasSearched = false;
    return;
  }

  historySearch.value.isLoading = true;
  historySearch.value.error = "";
  try {
    const data = await appointmentService.getHistoryByDocument(
      historySearch.value.documentNumber.trim(),
    );
    historySearch.value.patient = data.patient;
    historySearch.value.appointments = data.appointments || [];
    historySearch.value.hasSearched = true;
  } catch (err) {
    historySearch.value.hasSearched = true;
    const status = err.response?.status;
    const msg = err.response?.data?.message || err.message;
    if (
      status === 404 ||
      (msg && msg.includes("No se encontró ningún paciente"))
    ) {
      // CA3: Cedula no registrada en el sistema
      historySearch.value.isWarningNotFound = true;
      historySearch.value.error =
        "No se encontró ningún paciente registrado con la cédula ingresada";
    } else {
      historySearch.value.error =
        msg || "Ocurrió un error al consultar el historial de citas.";
    }
  } finally {
    historySearch.value.isLoading = false;
  }
}

function handleClearHistory() {
  historySearch.value = {
    documentNumber: "",
    isLoading: false,
    hasSearched: false,
    error: "",
    patient: null,
    appointments: [],
    isWarningNotFound: false,
  };
}

function navigateToRegisterPatient() {
  router.push("/patients");
}

function getStatusBadgeClass(status) {
  const s = String(status || "").toUpperCase();
  if (s === "SCHEDULED" || s === "PROGRAMADA") return "status-scheduled";
  if (s === "COMPLETED" || s === "COMPLETADA" || s === "ATENDIDA")
    return "status-completed";
  if (
    s === "CANCELLED" ||
    s === "CANCELED" ||
    s === "CANCELADA" ||
    s === "INASISTENCIA"
  )
    return "status-cancelled";
  return "status-scheduled";
}

function formatStatus(status) {
  const s = String(status || "").toUpperCase();
  if (s === "SCHEDULED") return "PROGRAMADA";
  if (s === "COMPLETED") return "ATENDIDA";
  if (s === "CANCELLED" || s === "CANCELED" || s === "CANCELADA")
    return "CANCELADA";
  return status;
}

function isAppointmentCancelled(status) {
  const s = String(status || "").toUpperCase();
  return s === "CANCELLED" || s === "CANCELED" || s === "CANCELADA";
}

// Franjas de horario disponibles
const availableSlots = ref([]);
const isLoadingSlots = ref(false);
const selectedSlotIndex = ref(null);

const showManualTime = computed(
  () =>
    form.value.professionalId &&
    form.value.appointmentDate &&
    !isLoadingSlots.value &&
    availableSlots.value.length === 0,
);

const todayStr = computed(() => new Date().toISOString().split("T")[0]);

// Formulario de nueva cita
const form = ref({
  patientId: "",
  professionalId: "",
  appointmentDate: "",
  startTime: "08:00",
  endTime: "08:30",
});

// Filtros de busqueda
const filter = ref({
  professionalId: "",
  date: "",
});

async function loadPatientsList() {
  if (!isPatient.value) return;
  isLoadingCurrentPatient.value = true;
  try {
    currentPatient.value = await patientService.getMe();
    form.value.patientId = currentPatient.value ? currentPatient.value.id : "";
  } catch (error) {
    currentPatient.value = null;
    console.error("Error al cargar el paciente del usuario actual:", error);
  } finally {
    isLoadingCurrentPatient.value = false;
  }
}

function resetPatientSearch() {
  patientSearch.value = {
    documentNumber: "",
    result: null,
    isSearching: false,
    error: "",
  };
}

function openCreateForm() {
  showCreateForm.value = true;
  if (isPatient.value) {
    form.value.patientId = currentPatient.value ? currentPatient.value.id : "";
  } else {
    resetPatientSearch();
    form.value.patientId = "";
  }
}

function closeCreateForm() {
  showCreateForm.value = false;
  if (isPatient.value) {
    form.value.patientId = currentPatient.value ? currentPatient.value.id : "";
  } else {
    resetPatientSearch();
    form.value.patientId = "";
  }
}

function onPatientDocumentChange() {
  patientSearch.value.result = null;
  patientSearch.value.error = "";
  form.value.patientId = "";
}

async function searchPatientByDocument() {
  const doc = patientSearch.value.documentNumber.trim();
  if (!doc) return;

  patientSearch.value.isSearching = true;
  patientSearch.value.error = "";
  patientSearch.value.result = null;

  try {
    const patient = await patientService.getByDocument(doc);
    patientSearch.value.result = patient;
    form.value.patientId = patient.id;
  } catch (error) {
    form.value.patientId = "";
    patientSearch.value.error =
      "No se encontro un paciente con ese numero de documento. Verifica el numero o registralo primero en el modulo de Pacientes.";
  } finally {
    patientSearch.value.isSearching = false;
  }
}

function addMinutesToTime(time, minutes) {
  if (!time || !minutes) return time;
  const [h, m] = time.split(":").map(Number);
  if (Number.isNaN(h) || Number.isNaN(m)) return time;
  const totalMinutes = h * 60 + m + Number(minutes);
  const wrapped = ((totalMinutes % 1440) + 1440) % 1440;
  const hh = String(Math.floor(wrapped / 60)).padStart(2, "0");
  const mm = String(wrapped % 60).padStart(2, "0");
  return `${hh}:${mm}`;
}

function recalculateEndTime() {
  const professional = professionals.value.find(
    (prof) => String(prof.id) === String(form.value.professionalId),
  );
  if (professional?.appointmentIntervalMinutes && form.value.startTime) {
    form.value.endTime = addMinutesToTime(
      form.value.startTime,
      professional.appointmentIntervalMinutes,
    );
  }
}

async function loadProfessionals() {
  try {
    allProfessionals.value = await professionalService.getAll();
  } catch (error) {
    console.error("Error al cargar lista de profesionales:", error);
  }
}

async function fetchAppointments() {
  isLoading.value = true;
  errorMessage.value = "";
  isFiltered.value = false;
  try {
    // Un paciente solo ve SUS PROPIAS citas (el backend las filtra por el token).
    if (isPatient.value) {
      appointments.value = await appointmentService.getMine();
    } else {
      appointments.value = await appointmentService.getAll();
    }
  } catch (error) {
    errorMessage.value = error.message || "Error al cargar las citas medicas.";
  } finally {
    isLoading.value = false;
  }
}

async function fetchAvailableSlots() {
  if (!form.value.professionalId || !form.value.appointmentDate) {
    availableSlots.value = [];
    selectedSlotIndex.value = null;
    return;
  }

  isLoadingSlots.value = true;
  selectedSlotIndex.value = null;
  try {
    const slots = await schedulingService.getAvailableSlots(
      form.value.professionalId,
      form.value.appointmentDate,
    );
    availableSlots.value = slots || [];
  } catch (error) {
    console.error("Error al obtener franjas horarias:", error);
    availableSlots.value = [];
  } finally {
    isLoadingSlots.value = false;
  }
}

watch(
  () => [form.value.professionalId, form.value.appointmentDate],
  () => {
    if (showCreateForm.value) {
      fetchAvailableSlots();
    }
  },
);

// Calcula Hora Fin automaticamente io
watch(
  () => [form.value.professionalId, form.value.startTime],
  () => {
    if (showCreateForm.value) {
      recalculateEndTime();
    }
  },
);

function selectSlot(slot, index) {
  selectedSlotIndex.value = index;
  form.value.startTime = slot.startTime.substring(0, 5);
  form.value.endTime = slot.endTime.substring(0, 5);
}

async function handleSearch() {
  if (!filter.value.professionalId && !filter.value.date) {
    errorMessage.value = "Para buscar, selecciona un profesional o una fecha.";
    return;
  }

  isLoading.value = true;
  errorMessage.value = "";
  isFiltered.value = true;
  try {
    const params = {
      professionalId: filter.value.professionalId || undefined,
      date: filter.value.date || undefined,
    };
    // El paciente busca solo dentro de sus propias citas (el backend lo garantiza)
    appointments.value = isPatient.value
      ? await appointmentService.getMine(params)
      : await appointmentService.search(params);
  } catch (error) {
    errorMessage.value =
      error.message || "Error al buscar citas con los criterios indicados.";
  } finally {
    isLoading.value = false;
  }
}

function handleClearSearch() {
  filter.value.professionalId = "";
  filter.value.date = "";
  isFiltered.value = false;
  fetchAppointments();
}

// Excel con configuracion regional en español separa las columnas con punto y coma
const CSV_SEPARATOR = ";";

async function exportToCSV() {
  isExporting.value = true;
  const escapeCSV = (value) => {
    if (value === null || value === undefined) return "";
    const str = String(value);
    if (str.includes(CSV_SEPARATOR) || str.includes(",") || str.includes("\n") || str.includes('"')) {
      return `"${str.replace(/"/g, '""')}"`;
    }
    return str;
  };

  errorMessage.value = "";
  try {
    if (!appointments.value || appointments.value.length === 0) {
      errorMessage.value = "No hay datos para exportar.";
      return;
    }

    const patientIds = [
      ...new Set(appointments.value.map((item) => item.patientId)),
    ];
    const patientResults = await Promise.allSettled(
      patientIds.map((id) => patientService.getById(id)),
    );
    const failedLookups = patientResults.filter((r) => r.status === "rejected");
    if (failedLookups.length > 0) {
      console.warn("No se pudo cargar el paciente:", failedLookups.map((r) => r.reason));
      errorMessage.value = `No se pudieron cargar los datos de ${failedLookups.length} paciente(s); en el archivo aparecen sin nombre ni documento.`;
    }
    const patientsById = new Map();
    patientResults.forEach((result, index) => {
      if (result.status === "fulfilled" && result.value) {
        patientsById.set(patientIds[index], {
          fullName: result.value.fullName,
          documentNumber: result.value.documentNumber,
        });
      }
    });

    const columns = [
      { key: "id", label: "ID" },
      { key: "patientName", label: "Paciente" },
      { key: "documentNumber", label: "Documento" },
      { key: "professionalName", label: "Profesional" },
      { key: "appointmentDate", label: "Fecha" },
      { key: "startTime", label: "Hora Inicio" },
      { key: "endTime", label: "Hora Fin" },
      { key: "status", label: "Estado" },
    ];

    const dataToExport = appointments.value.map((item) => {
      const patient = patientsById.get(item.patientId);
      return {
        id: item.id,
        patientName: patient?.fullName || `Paciente #${item.patientId}`,
        documentNumber: patient?.documentNumber || "",
        professionalName: professionalNameById(
          allProfessionals.value,
          item.professionalId,
        ),
        appointmentDate: item.appointmentDate,
        startTime: item.startTime,
        endTime: item.endTime,
        status: item.status,
      };
    });

    const headers = columns.map((col) => escapeCSV(col.label)).join(CSV_SEPARATOR);
    const rows = dataToExport.map((item) =>
      columns
        .map((col) => {
          const value = item[col.key];
          if (col.key === "documentNumber" && value) {
            const formulaText = String(value).replace(/"/g, '""');
            return escapeCSV(`="${formulaText}"`);
          }
          return escapeCSV(value);
        })
        .join(CSV_SEPARATOR),
    );

    const csvContent = [headers, ...rows].join("\r\n");

    const BOM = "\uFEFF";
    const blob = new Blob([BOM + csvContent], {
      type: "text/csv;charset=utf-8;",
    });
    const url = URL.createObjectURL(blob);

    const link = document.createElement("a");
    link.href = url;
    // El nombre usa la fecha del filtro; si se filtro solo por profesional, la fecha de hoy
    const dateStr = filter.value.date || new Date().toISOString().split("T")[0];
    link.setAttribute("download", `citas_medicas_${dateStr}.csv`);

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    URL.revokeObjectURL(url);
  } catch (error) {
    errorMessage.value = error.message || "No se pudo exportar las citas.";
  } finally {
    isExporting.value = false;
  }
}

async function handleCreate() {
  errorMessage.value = "";
  successMessage.value = "";

  if (isPatient.value) {
    if (!currentPatient.value) {
      errorMessage.value =
        "No se encontro un paciente vinculado a tu usuario. Contacta al administrador.";
      return;
    }
    form.value.patientId = currentPatient.value.id;
  }

  fieldErrors.value = validateAppointmentForm(form.value);
  if (hasErrors(fieldErrors.value)) {
    errorMessage.value = "Corrige los errores del formulario de cita.";
    return;
  }

  isSubmitting.value = true;

  try {
    const created = await appointmentService.create({
      patientId: Number(form.value.patientId),
      professionalId: Number(form.value.professionalId),
      appointmentDate: form.value.appointmentDate,
      startTime: form.value.startTime,
      endTime: form.value.endTime,
    });

    successMessage.value = `¡Cita medica #${created.id} agendada correctamente!`;
    form.value = {
      patientId:
        isPatient.value && currentPatient.value ? currentPatient.value.id : "",
      professionalId: "",
      appointmentDate: "",
      startTime: "08:00",
      endTime: "08:30",
    };
    fieldErrors.value = {};
    availableSlots.value = [];
    selectedSlotIndex.value = null;
    showCreateForm.value = false;
    resetPatientSearch();
    await fetchAppointments();
  } catch (error) {
    errorMessage.value = error.message || "No se pudo crear la cita medica.";
  } finally {
    isSubmitting.value = false;
  }
}

async function handleDelete(id) {
  if (!confirm(`¿Estas seguro de que deseas cancelar la cita #${id}?`)) {
    return;
  }

  errorMessage.value = "";
  successMessage.value = "";
  try {
    await appointmentService.delete(id);
    successMessage.value = `Cita #${id} cancelada exitosamente.`;
    await fetchAppointments();
  } catch (error) {
    errorMessage.value = error.message || `No se pudo cancelar la cita #${id}.`;
  }
}

// Estado modal de cancelacion (paciente)
const showCancelModal = ref(false);
const cancellingAppointment = ref(null);
const cancellationReason = ref("");
const cancelError = ref("");
const isCancelling = ref(false);

function openCancelModal(item) {
  cancellingAppointment.value = item;
  cancellationReason.value = "";
  cancelError.value = "";
  showCancelModal.value = true;
}

function closeCancelModal() {
  if (isCancelling.value) return;
  showCancelModal.value = false;
  cancellingAppointment.value = null;
  cancellationReason.value = "";
  cancelError.value = "";
}

async function confirmCancelAppointment() {
  const reason = cancellationReason.value.trim();
  if (!reason) {
    cancelError.value = "El motivo de la cancelación es obligatorio.";
    return;
  }

  isCancelling.value = true;
  cancelError.value = "";
  errorMessage.value = "";
  successMessage.value = "";

  try {
    const apt = cancellingAppointment.value;
    const id = apt.id;
    await appointmentService.cancel(id, reason);
    const dateFormatted = apt.appointmentDate || "";
    const slotInfo = apt.startTime && apt.endTime
      ? ` (${dateFormatted} ${apt.startTime.substring(0, 5)} - ${apt.endTime.substring(0, 5)})`
      : "";
    successMessage.value = `Cita #${id} cancelada exitosamente. El espacio${slotInfo} ha quedado liberado.`;
    closeCancelModal();
    await fetchAppointments();
    if (showCreateForm.value && form.value.professionalId && form.value.appointmentDate) {
      await fetchAvailableSlots();
    }
  } catch (error) {
    cancelError.value =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      "No se pudo cancelar la cita.";
  } finally {
    isCancelling.value = false;
  }
}

function handleCancel(appointmentOrId) {
  if (typeof appointmentOrId === "object" && appointmentOrId !== null) {
    openCancelModal(appointmentOrId);
  } else {
    const app = appointments.value.find((a) => a.id === appointmentOrId);
    if (app) {
      openCancelModal(app);
    } else {
      openCancelModal({ id: appointmentOrId });
    }
  }
}

onMounted(async () => {
  loadProfessionals();

  if (isPatient.value) {
    await loadPatientsList();
  }
  fetchAppointments();
});
</script>

<template>
  <div class="view-container">
    <div class="page-header">
      <div>
        <h1>Gestion de Citas Medicas</h1>
        <p>
          Agendamiento, busqueda y administracion de citas de la entidad de
          salud
        </p>
      </div>
      <div class="header-actions">
        <!-- Solo Pacientes, Administradores y Agendadores pueden crear citas -->
        <button
          v-if="canManage || isPatient"
          @click="showCreateForm ? closeCreateForm() : openCreateForm()"
          class="btn"
          :class="showCreateForm ? 'btn-secondary' : 'btn-primary'"
        >
          {{ showCreateForm ? "Cancelar" : "Agendar Cita" }}
        </button>
        <button
          @click="fetchAppointments"
          class="btn btn-outline"
          :disabled="isLoading"
        >
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
            <span v-if="isLoadingCurrentPatient" class="text-muted"
              >Cargando tu informacion...</span
            >
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
                :disabled="
                  isSubmitting ||
                  patientSearch.isSearching ||
                  !patientSearch.documentNumber.trim()
                "
              >
                <span
                  v-if="patientSearch.isSearching"
                  class="spinner-sm"
                ></span>
                <span v-else>Buscar</span>
              </button>
            </div>
            <span v-if="patientSearch.result" class="patient-found">
              ✓
              {{
                patientSearch.result.fullName ||
                "Paciente sin cuenta de usuario"
              }}
              — Doc.
              {{ patientSearch.result.documentNumber }}
            </span>
            <span v-else-if="patientSearch.error" class="field-error">{{
              patientSearch.error
            }}</span>
            <span v-else-if="fieldErrors.patientId" class="field-error">{{
              fieldErrors.patientId
            }}</span>
          </div>

          <div class="form-group">
            <label for="professionalId">Profesional</label>
            <select
              id="professionalId"
              v-model="form.professionalId"
              :disabled="isSubmitting"
            >
              <option value="" disabled>Selecciona profesional</option>
              <option
                v-for="prof in professionals"
                :key="prof.id"
                :value="prof.id"
              >
                {{ professionalLabel(prof) }}
              </option>
            </select>
            <span v-if="fieldErrors.professionalId" class="field-error">{{
              fieldErrors.professionalId
            }}</span>
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
            <span v-if="fieldErrors.appointmentDate" class="field-error">{{
              fieldErrors.appointmentDate
            }}</span>
          </div>

          <div v-if="showManualTime" class="form-group">
            <label for="startTime">Hora Inicio</label>
            <input
              id="startTime"
              v-model="form.startTime"
              type="time"
              :disabled="isSubmitting"
            />
            <span v-if="fieldErrors.startTime" class="field-error">{{
              fieldErrors.startTime
            }}</span>
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
            <span v-if="fieldErrors.endTime" class="field-error">{{
              fieldErrors.endTime
            }}</span>
          </div>
        </div>

        <!-- Franjas horarias disponibles calculadas por scheduling/AvailableSlotService -->
        <div
          v-if="form.professionalId && form.appointmentDate"
          class="slots-section"
        >
          <label class="slots-label"> Franjas Horarias Disponibles: </label>
          <div v-if="isLoadingSlots" class="slots-loading">
            <span class="spinner-sm"></span> Calculando franjas libres...
          </div>
          <div v-else-if="availableSlots.length === 0" class="no-slots">
            No se encontraron franjas libres para este dia. Puedes ingresar el
            horario manualmente arriba.
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
              {{ slot.startTime.substring(0, 5) }} -
              {{ slot.endTime.substring(0, 5) }}
            </button>
          </div>
        </div>

        <div class="form-actions mt-3">
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="isSubmitting"
          >
            <span v-if="isSubmitting" class="spinner"></span>
            <span v-else>Guardar Cita</span>
          </button>
        </div>
      </form>
    </div>

    <!-- Historial de Citas por Cedula (E4-HU3: MEDICO, AGENDADOR, TERAPISTA, ADMIN) -->
    <div v-if="canConsultHistory" class="card history-card">
      <div class="history-card-header">
        <div>
          <h3>Consultar Historial de Citas por Cédula</h3>
        </div>
      </div>

      <form @submit.prevent="handleConsultHistory" class="history-search-form" novalidate>
        <div class="form-group" style="flex: 1; min-width: 260px; max-width: 420px;">
          <label for="historyDocumentNumber">Cédula del Paciente</label>
          <input
            id="historyDocumentNumber"
            v-model="historySearch.documentNumber"
            type="text"
            placeholder="Ingrese el número de cédula (ej. 1061789234)"
            :class="{ 'input-invalid': !!historySearch.error && !historySearch.isWarningNotFound }"
            :disabled="historySearch.isLoading"
            @input="onHistoryDocumentInput"
          />
          <!-- CA4: Bloquea, resalta en rojo y despliega alerta -->
          <span
            v-if="historySearch.error && !historySearch.isWarningNotFound"
            class="field-error-msg"
            role="alert"
          >
            {{ historySearch.error }}
          </span>
        </div>

        <div class="history-actions">
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="historySearch.isLoading"
          >
            <span v-if="historySearch.isLoading" class="spinner-sm"></span>
            <span v-else>Buscar / Consultar</span>
          </button>
          <button
            v-if="historySearch.hasSearched || historySearch.documentNumber"
            type="button"
            class="btn btn-secondary"
            @click="handleClearHistory"
            :disabled="historySearch.isLoading"
          >
            Limpiar
          </button>
        </div>
      </form>

      <!-- CA3: Notificacion de advertencia si la cedula no corresponde a ningun paciente -->
      <div v-if="historySearch.isWarningNotFound" class="alert alert-warning-custom mt-3">
        <div class="warning-content">
          <div class="warning-icon">⚠️</div>
          <div>
            <strong>{{ historySearch.error }}</strong>
            <p class="suggestion-text">
              Sugerencia: puedes registrar este paciente en el sistema.
              <a
                href="#"
                @click.prevent="navigateToRegisterPatient"
                class="register-link"
              >
                Ir a registro de pacientes
              </a>
            </p>
          </div>
        </div>
      </div>

      <!-- Resultados de la consulta -->
      <div v-if="historySearch.patient && !historySearch.isLoading" class="history-results mt-4">
        <!-- Informacion basica del paciente (CA1 y CA2) -->
        <div class="patient-summary-box">
          <div class="patient-summary-header">
            <h4>Información Básica del Paciente</h4>
            <span class="badge-patient">Paciente Registrado</span>
          </div>
          <div class="patient-details-grid">
            <div>
              <span class="detail-label">Nombre Completo:</span>
              <span class="detail-value">
                {{ historySearch.patient.fullName || "Sin cuenta de usuario asociada" }}
              </span>
            </div>
            <div>
              <span class="detail-label">Cédula:</span>
              <span class="detail-value font-mono">
                {{ historySearch.patient.documentNumber }}
              </span>
            </div>
            <div>
              <span class="detail-label">Teléfono:</span>
              <span class="detail-value">
                {{ historySearch.patient.phone || "No registrado" }}
              </span>
            </div>
            <div>
              <span class="detail-label">Correo Electrónico:</span>
              <span class="detail-value">
                {{ historySearch.patient.email || "No registrado" }}
              </span>
            </div>
            <div>
              <span class="detail-label">Fecha de Nacimiento:</span>
              <span class="detail-value">
                {{ historySearch.patient.birthDate || "No registrada" }}
              </span>
            </div>
          </div>
        </div>

        <!-- CA1: Listado cronologico con historial completo de citas -->
        <div
          v-if="historySearch.appointments.length > 0"
          class="history-table-container mt-3"
        >
          <div class="history-table-header">
            <h4>Historial de Citas Médicas ({{ historySearch.appointments.length }})</h4>
          </div>
          <div class="table-responsive">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>Hora</th>
                  <th>Especialista</th>
                  <th>Especialidad</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="apt in historySearch.appointments" :key="apt.id">
                  <td>
                    <strong>{{ apt.appointmentDate }}</strong>
                  </td>
                  <td>
                    {{ apt.startTime ? apt.startTime.substring(0, 5) : "" }} -
                    {{ apt.endTime ? apt.endTime.substring(0, 5) : "" }}
                  </td>
                  <td>{{ apt.professionalName }}</td>
                  <td>
                    <span class="specialty-pill">{{ apt.specialtyName }}</span>
                  </td>
                  <td>
                    <span
                      class="status-badge"
                      :class="getStatusBadgeClass(apt.status)"
                    >
                      {{ formatStatus(apt.status) }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- CA2: Paciente registrado sin citas previas -->
        <div v-else class="empty-history-box mt-3">
          <div class="empty-history-icon">📋</div>
          <p class="empty-history-message">
            El paciente no registra citas médicas en el historial
          </p>
          <span class="text-muted-xs">
            No se encontraron citas agendadas ni atendidas registradas para este paciente.
          </span>
        </div>
      </div>
    </div>

    <!-- Barra de busqueda y filtros -->
    <div class="card filter-card" v-if="!isPatient"> 
      <h4>Filtrar Citas por Profesional o Fecha</h4>
      <form @submit.prevent="handleSearch" class="filter-form">
        <div class="form-group">
          <label for="filterProfId">Profesional</label>
          <select id="filterProfId" v-model="filter.professionalId">
            <option value="">Todos los profesionales</option>
            <option
              v-for="prof in professionals"
              :key="prof.id"
              :value="prof.id"
            >
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
      <div class="table-header" style="display: flex; justify-content: space-between; align-items: baseline;">
        <h3>Citas Medicas ({{ appointments.length }})</h3>
        <button
          v-if="isFiltered && appointments.length > 0"
          @click="exportToCSV"
          type="button"
          class="btn btn-primary"
          :disabled="isLoading || isExporting"
        >
          Exportar a CSV
        </button>
      </div>

      <div v-if="isLoading" class="loading-state">
        <div class="spinner-large"></div>
        <p>Cargando citas medicas...</p>
      </div>

      <div v-else-if="appointments.length === 0" class="empty-state">
        <p>
          No se encontraron citas medicas registradas con los criterios
          seleccionados.
        </p>
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
              <td>
                <strong>Paciente #{{ item.patientId }}</strong>
              </td>
              <td>
                {{
                  professionalNameById(allProfessionals, item.professionalId)
                }}
              </td>
              <td>{{ item.appointmentDate }}</td>
              <td>{{ item.startTime }} - {{ item.endTime }}</td>
              <td>
                <span
                  class="status-badge"
                  :class="getStatusBadgeClass(item.status)"
                >
                  {{ formatStatus(item.status) }}
                </span>
                <div
                  v-if="item.cancellationReason"
                  class="cancellation-reason-text"
                  title="Motivo de la cancelación"
                >
                  <small><strong>Motivo:</strong> {{ item.cancellationReason }}</small>
                </div>
              </td>

              <td style="text-align: right">
                <button
                  v-if="canManage && !isAppointmentCancelled(item.status)"
                  @click="handleDelete(item.id)"
                  class="btn-danger-sm"
                  title="Cancelar Cita"
                >
                  Cancelar
                </button>
                <button
                  v-else-if="isPatient && !isAppointmentCancelled(item.status)"
                  @click="openCancelModal(item)"
                  class="btn-danger-sm"
                  title="Cancelar mi cita"
                >
                  Cancelar mi cita
                </button>
                <span v-else class="text-muted-xs">—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal para Cancelar Cita (Paciente) -->
    <div
      v-if="showCancelModal"
      class="modal-overlay"
      @click.self="closeCancelModal"
    >
      <div class="modal-card">
        <div class="modal-header">
          <h3>Cancelar Cita Médica</h3>
          <button
            @click="closeCancelModal"
            class="btn-close"
            title="Cerrar modal"
            :disabled="isCancelling"
          >
            &times;
          </button>
        </div>

        <div class="modal-body">
          <div v-if="cancellingAppointment" class="modal-appointment-summary">
            <p><strong>Cita #{{ cancellingAppointment.id }}</strong></p>
            <p>
              <strong>Profesional:</strong>
              {{ professionalNameById(allProfessionals, cancellingAppointment.professionalId) }}
            </p>
            <p>
              <strong>Fecha:</strong> {{ cancellingAppointment.appointmentDate }}
            </p>
            <p>
              <strong>Horario:</strong> {{ cancellingAppointment.startTime }} - {{ cancellingAppointment.endTime }}
            </p>
          </div>

          <p class="modal-instructions">
            Para proceder con la cancelación de la cita, es <strong>obligatorio</strong> indicar el motivo de la cancelación.
          </p>

          <div class="form-group">
            <label for="cancellationReasonInput">Motivo de la cancelación *</label>
            <textarea
              id="cancellationReasonInput"
              v-model="cancellationReason"
              class="modal-textarea"
              rows="3"
              placeholder="Indica la razón por la cual no podrás asistir..."
              :disabled="isCancelling"
              @input="cancelError = ''"
            ></textarea>
            <span v-if="cancelError" class="field-error">{{ cancelError }}</span>
          </div>
        </div>

        <div class="modal-footer">
          <button
            type="button"
            @click="closeCancelModal"
            class="btn btn-outline"
            :disabled="isCancelling"
          >
            Volver
          </button>
          <button
            type="button"
            @click="confirmCancelAppointment"
            class="btn btn-danger"
            :disabled="isCancelling"
          >
            {{ isCancelling ? "Cancelando..." : "Confirmar Cancelación" }}
          </button>
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

/* ==========================================================================
   Estilos Historial de Citas por Cedula (E4-HU3)
   ========================================================================== */
.history-card {
  border-left: 4px solid #2563eb;
}

.history-card-header {
  margin-bottom: 1rem;
}

.section-desc {
  color: #64748b;
  font-size: 0.9rem;
  margin: 0.25rem 0 0;
}

.history-search-form {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  flex-wrap: wrap;
}

.history-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1.7rem;
}

.input-invalid {
  border-color: #ef4444 !important;
  box-shadow: 0 0 0 1px #ef4444 !important;
  background-color: #fef2f2 !important;
}

.field-error-msg {
  color: #dc2626;
  font-size: 0.8rem;
  margin-top: 0.35rem;
  display: block;
  font-weight: 500;
}

.alert-warning-custom {
  background: #fffbeb;
  color: #92400e;
  border: 1px solid #fde68a;
  border-radius: 8px;
  padding: 1rem;
}

.warning-content {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.warning-icon {
  font-size: 1.25rem;
  line-height: 1;
}

.suggestion-text {
  margin: 0.4rem 0 0 0;
  font-size: 0.875rem;
  color: #78350f;
}

.register-link {
  color: #b45309;
  text-decoration: underline;
  font-weight: 600;
  margin-left: 0.25rem;
  cursor: pointer;
}

.register-link:hover {
  color: #78350f;
}

.patient-summary-box {
  background: #f8fafc;
  border-radius: 8px;
  padding: 1.25rem;
  border: 1px solid #e2e8f0;
}

.patient-summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.patient-summary-header h4 {
  margin: 0;
  font-size: 1rem;
  color: #1e293b;
}

.badge-patient {
  background: #dbeafe;
  color: #1e40af;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.2rem 0.6rem;
  border-radius: 9999px;
}

.patient-details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 0.75rem;
}

.detail-label {
  display: block;
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 500;
}

.detail-value {
  font-size: 0.9rem;
  font-weight: 600;
  color: #0f172a;
}

.font-mono {
  font-family: monospace;
}

.specialty-pill {
  background: #f3e8ff;
  color: #6b21a8;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
}

.history-table-container {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.history-table-header {
  padding: 0.75rem 1rem;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.history-table-header h4 {
  margin: 0;
  font-size: 0.95rem;
  color: #1e293b;
}

.empty-history-box {
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  padding: 2.5rem 1rem;
  text-align: center;
}

.empty-history-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.empty-history-message {
  font-size: 1rem;
  font-weight: 600;
  color: #334155;
  margin: 0 0 0.25rem 0;
}

/* Modal de Cancelación */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-card {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 480px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.2), 0 10px 10px -5px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  animation: modal-pop 0.2s ease-out;
}

@keyframes modal-pop {
  from {
    transform: scale(0.95);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #0f172a;
}

.btn-close {
  background: transparent;
  border: none;
  font-size: 1.5rem;
  line-height: 1;
  color: #64748b;
  cursor: pointer;
  padding: 0;
}

.btn-close:hover {
  color: #0f172a;
}

.modal-body {
  padding: 1.25rem 1.5rem;
}

.modal-appointment-summary {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 0.85rem 1rem;
  margin-bottom: 1rem;
  font-size: 0.875rem;
  line-height: 1.5;
  color: #334155;
}

.modal-appointment-summary p {
  margin: 0.2rem 0;
}

.modal-instructions {
  font-size: 0.875rem;
  color: #475569;
  margin-bottom: 1rem;
}

.modal-textarea {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  padding: 0.6rem 0.75rem;
  font-size: 0.9rem;
  font-family: inherit;
  resize: vertical;
  box-sizing: border-box;
}

.modal-textarea:focus {
  outline: none;
  border-color: #7c3aed;
  box-shadow: 0 0 0 2px rgba(124, 58, 237, 0.2);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

.btn-danger {
  background: #dc2626;
  color: white;
  border: none;
}

.btn-danger:hover:not(:disabled) {
  background: #b91c1c;
}

.cancellation-reason-text {
  font-size: 0.75rem;
  color: #b91c1c;
  margin-top: 0.25rem;
  line-height: 1.2;
}

</style>