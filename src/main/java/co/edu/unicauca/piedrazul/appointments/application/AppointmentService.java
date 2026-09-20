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

    public AppointmentEntity getAppointmentByProfessionalId(Long professionalId) {
        return repository.findByProfessionalId(professionalId).stream().findFirst().orElse(null);
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

    public List<AppointmentEntity> findAppointments(Long professionalId, LocalDate date) {
        return repository
                .findByProfessionalIdAndAppointmentDate(
                        professionalId,
                        date
                );
    }
}

