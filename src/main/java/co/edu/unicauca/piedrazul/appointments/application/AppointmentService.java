package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentEntity;
import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentRepository;
import co.edu.unicauca.piedrazul.appointments.presentation.command.CreateAppointmentCommand;
import co.edu.unicauca.piedrazul.patients.application.PatientService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository repository;
    private final PatientService patientService;

    public AppointmentService(AppointmentRepository repository, PatientService patientService) {
        this.repository = repository;
        this.patientService = patientService;
    }

    public AppointmentEntity createAppointment(CreateAppointmentCommand command) {

        if (command.appointmentDate().isBefore(java.time.LocalDate.now())) {
        throw new IllegalArgumentException(
            "No se puede agendar una cita en una fecha anterior a hoy.");
        }

        validateSlotAvailable(command);

        AppointmentEntity appointment =
                new AppointmentEntity();

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
            // Dos reservas simultaneas: el indice unico uk_professional_slot rechaza la segunda
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
                .anyMatch(existing ->
                        command.startTime().isBefore(existing.getEndTime())
                                && command.endTime().isAfter(existing.getStartTime()));

        if (overlaps) {
            throw slotUnavailable();
        }
    }

    private boolean isCancelled(AppointmentEntity appointment) {
        return "CANCELLED".equals(appointment.getStatus())
                || "CANCELADA".equals(appointment.getStatus());
    }

    private ResponseStatusException slotUnavailable() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "El horario ya no está disponible. Por favor selecciona otra franja.");
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
}
