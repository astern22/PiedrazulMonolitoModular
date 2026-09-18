package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentEntity;
import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository repository;

    public AppointmentService(AppointmentRepository repository) {
        this.repository = repository;
    }

    public AppointmentEntity createAppointment(AppointmentEntity appointment) {
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
}

