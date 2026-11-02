package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentEntity;
import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentRepository;
import co.edu.unicauca.piedrazul.appointments.presentation.command.CreateAppointmentCommand;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository repository;

    public AppointmentService(AppointmentRepository repository) {
        this.repository = repository;
    }

    public AppointmentEntity createAppointment(CreateAppointmentCommand command) {

        if (command.appointmentDate().isBefore(java.time.LocalDate.now())) {
        throw new IllegalArgumentException(
            "No se puede agendar una cita en una fecha anterior a hoy.");
        }

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

        return repository.save(appointment);
    }

    public AppointmentEntity getAppointmentById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<AppointmentEntity> getAppointmentByProfessionalId(Long professionalId) {
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
     * Busca citas por profesional, por fecha o por ambos.
     * Debe llegar al menos uno de los dos criterios.
     */
    public List<AppointmentEntity> findAppointments(Long professionalId, LocalDate date) {
        if (professionalId == null && date == null) {
            throw new IllegalArgumentException(
                    "Indica al menos el profesional o la fecha para buscar");
        }
        if (professionalId == null) {
            return repository.findAllByAppointmentDate(date);
        }
        if (date == null) {
            return repository.findByProfessionalId(professionalId);
        }
        return repository
                .findByProfessionalIdAndAppointmentDate(
                        professionalId,
                        date
                );
    }
    
    public List<AppointmentEntity> getAppointmentsByPatientId(Long patientId) {
    return repository.findByPatientId(patientId);
    }
}

