package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentEntity;
import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentRepository;
import co.edu.unicauca.piedrazul.appointments.presentation.command.CreateAppointmentCommand;
import co.edu.unicauca.piedrazul.appointments.presentation.dto.AppointmentHistoryItemResponse;
import co.edu.unicauca.piedrazul.appointments.presentation.dto.PatientAppointmentHistoryResponse;
import co.edu.unicauca.piedrazul.patients.application.PatientService;
import co.edu.unicauca.piedrazul.patients.presentation.dto.PatientResponse;
import co.edu.unicauca.piedrazul.professionals.application.ProfessionalService;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalEntity;

import org.springframework.cglib.core.Local;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.slf4j.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository repository;
    private final PatientService patientService;
    private final ProfessionalService professionalService;

    public AppointmentService(
            AppointmentRepository repository,
            PatientService patientService,
            ProfessionalService professionalService) {
        this.repository = repository;
        this.patientService = patientService;
        this.professionalService = professionalService;
    }

    public AppointmentEntity createAppointment(CreateAppointmentCommand command) {

        if (command.appointmentDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException(
                    "No se puede agendar una cita en una fecha anterior a hoy.");
        }

        validateSlotAvailable(command);

        AppointmentEntity appointment = new AppointmentEntity();

        appointment.setPatientId(
                command.patientId());

        appointment.setProfessionalId(
                command.professionalId());

        appointment.setAppointmentDate(
                command.appointmentDate());

        appointment.setStartTime(
                command.startTime());

        appointment.setEndTime(
                command.endTime());

        appointment.setStatus("SCHEDULED");

        try {
            return repository.save(appointment);
        } catch (DataIntegrityViolationException e) {
            // Dos reservas simultaneas: el indice unico uk_professional_slot rechaza la
            // segunda
            throw slotUnavailable();
        }
    }

    /**
     * Rechaza la cita si el profesional ya tiene otra cita (no cancelada)
     * que se cruza con el horario solicitado en esa fecha.
     */
    private void validateSlotAvailable(CreateAppointmentCommand command) {
        boolean overlaps = repository
                .findByProfessionalIdAndAppointmentDate(command.professionalId(), command.appointmentDate())
                .stream()
                .filter(existing -> !isCancelled(existing))
                .anyMatch(existing -> command.startTime().isBefore(existing.getEndTime())
                        && command.endTime().isAfter(existing.getStartTime()));

        if (overlaps) {
            throw slotUnavailable();
        }
    }

    private boolean isCancelled(AppointmentEntity appointment) {
        if (appointment == null || appointment.getStatus() == null) {
            return false;
        }
        String status = appointment.getStatus().toUpperCase();
        return "CANCELLED".equals(status) || "CANCELED".equals(status) || "CANCELADA".equals(status);
    }

    private ResponseStatusException slotUnavailable() {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "El horario ya no está disponible. Por favor selecciona otra franja.");
    }

    public AppointmentEntity getAppointmentById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<AppointmentEntity> getAppointmentsByProfessionalId(Long professionalId) {
        return repository.findByProfessionalId(professionalId);
    }

    public List<AppointmentEntity> getAllAppointments() {
        return repository.findAll();
    }

    public AppointmentEntity updateAppointment(AppointmentEntity appointment) {
        return repository.save(appointment);
    }

    public void deleteAppointment(Long id) {
        repository.deleteById(id);
    }

    /**
     * Busca citas por profesional, por fecha, o por ambos.
     * Basta con indicar uno de los dos filtros.
     */
    public List<AppointmentEntity> findAppointments(Long professionalId, LocalDate date) {
        List<AppointmentEntity> found;
        if (professionalId != null && date != null) {
            found = repository.findByProfessionalIdAndAppointmentDate(professionalId, date);
        } else if (professionalId != null) {
            found = repository.findByProfessionalId(professionalId);
        } else if (date != null) {
            found = repository.findByAppointmentDate(date);
        } else {
            throw new IllegalArgumentException("Debes indicar un profesional o una fecha.");
        }
        // Resultados ordenados por fecha y hora de inicio
        return found.stream()
                .filter(appointment -> !isCancelled(appointment))
                .sorted(java.util.Comparator
                        .comparing(AppointmentEntity::getAppointmentDate)
                        .thenComparing(AppointmentEntity::getStartTime))
                .toList();
    }

    public List<AppointmentEntity> getAppointmentsByPatientId(Long patientId) {
        return repository.findByPatientId(patientId);
    }

    /**
     * Id del paciente vinculado al usuario autenticado (se obtiene del token,
     * nunca de un dato enviado por el cliente).
     */
    public Long getPatientIdByUsername(String username) {
        return patientService.findByCurrentUser(username).id();
    }

    /**
     * Citas del paciente dueño de la sesion, ordenadas por fecha y hora.
     * Los filtros son opcionales y siempre se aplican DENTRO de sus propias citas.
     */
    public List<AppointmentEntity> getAppointmentsByUsername(String username, Long professionalId, LocalDate date) {
        return repository.findByPatientId(getPatientIdByUsername(username)).stream()
                .filter(a -> professionalId == null || professionalId.equals(a.getProfessionalId()))
                .filter(a -> date == null || date.equals(a.getAppointmentDate()))
                .sorted(java.util.Comparator
                        .comparing(AppointmentEntity::getAppointmentDate)
                        .thenComparing(AppointmentEntity::getStartTime))
                .toList();
    }

    /**
     * Consulta el historial cronologico completo de citas de un paciente por su
     * cedula.
     * Roles permitidos: ADMIN, SCHEDULER, PROFESSIONAL (Medico/Terapista).
     */
    public PatientAppointmentHistoryResponse getAppointmentHistoryByDocument(String documentNumber) {
        if (documentNumber == null || documentNumber.isBlank() || !documentNumber.trim().matches("^\\d+$")) {
            throw new IllegalArgumentException("El campo es obligatorio y solo acepta caracteres numéricos válidos.");
        }

        PatientResponse patient = patientService.findOptionalByDocumentNumber(documentNumber.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró ningún paciente registrado con la cédula ingresada"));

        List<AppointmentEntity> appointments = repository
                .findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patient.id());

        if (appointments.isEmpty()) {
            return new PatientAppointmentHistoryResponse(patient, Collections.emptyList());
        }

        Set<Long> professionalIds = appointments.stream()
                .map(AppointmentEntity::getProfessionalId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, ProfessionalEntity> profMap = new HashMap<>();
        for (Long profId : professionalIds) {
            try {
                profMap.put(profId, professionalService.findById(profId));
            } catch (Exception ignored) {
            }
        }

        Set<Long> userIds = profMap.values().stream()
                .map(ProfessionalEntity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> specialtyIds = profMap.values().stream()
                .map(ProfessionalEntity::getSpecialtyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> fullNames = professionalService.findFullNamesByUserIds(userIds);
        Map<Long, String> specialtyNames = professionalService.findSpecialtyNamesByIds(specialtyIds);

        List<AppointmentHistoryItemResponse> items = appointments.stream().map(a -> {
            ProfessionalEntity prof = profMap.get(a.getProfessionalId());
            String profName = "Profesional #" + a.getProfessionalId();
            String specName = "General";
            if (prof != null) {
                if (prof.getUserId() != null && fullNames.containsKey(prof.getUserId())) {
                    profName = fullNames.get(prof.getUserId());
                }
                if (prof.getSpecialtyId() != null && specialtyNames.containsKey(prof.getSpecialtyId())) {
                    specName = specialtyNames.get(prof.getSpecialtyId());
                }
            }
            return new AppointmentHistoryItemResponse(
                    a.getId(),
                    a.getPatientId(),
                    a.getProfessionalId(),
                    a.getAppointmentDate(),
                    a.getStartTime(),
                    a.getEndTime(),
                    profName,
                    specName,
                    a.getStatus());
        }).toList();

        return new PatientAppointmentHistoryResponse(patient, items);
    }

    public AppointmentEntity cancelAppointment(
            AppointmentEntity appointment,
            String cancellationReason) {

        if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El motivo de la cancelación es obligatorio.");
        }

        if (isCancelled(appointment)) {
            throw new IllegalArgumentException(
                    "La cita ya se encuentra cancelada.");
        }

        long diasRestantes = ChronoUnit.DAYS.between(
                LocalDate.now(),
                appointment.getAppointmentDate());

        if (diasRestantes < 2) {
            throw new IllegalArgumentException(
                    "La cita no puede cancelarse con menos de dos dias de anticipación.");
        }

        appointment.setStatus("CANCELLED");
        appointment.setCancellationReason(cancellationReason.trim());

        return repository.save(appointment);
    }

    public AppointmentEntity cancelAppointment(
            AppointmentEntity appointment) {
        return cancelAppointment(appointment, null);
    }
}
