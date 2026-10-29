package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentEntity;
import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentRepository;
import co.edu.unicauca.piedrazul.appointments.presentation.command.CreateAppointmentCommand;
import co.edu.unicauca.piedrazul.patients.infrastructure.persistence.PatientEntity;
import co.edu.unicauca.piedrazul.patients.infrastructure.persistence.PatientRepository;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository repository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public AppointmentService(
            AppointmentRepository repository,
            UserRepository userRepository,
            PatientRepository patientRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
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
    
    public List<AppointmentEntity> getAppointmentsByPatientId(Long patientId) {
        return repository.findByPatientId(patientId);
    }

    public List<AppointmentEntity> getAppointmentsByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        PatientEntity patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("No se encontro un paciente vinculado a tu usuario"));
        return repository.findByPatientId(patient.getId());
    }
}

