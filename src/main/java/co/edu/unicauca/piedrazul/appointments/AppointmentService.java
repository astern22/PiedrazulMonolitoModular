package co.edu.unicauca.piedrazul.appointments;

import java.util.List;

import org.springframework.stereotype.Service;

@Service 
public class AppointmentService {

    private final AppointmentRepository repository;

    public AppointmentService(AppointmentRepository repository) {
        this.repository = repository;
    }

    public Appointment createAppointment(Appointment appointment) {
        return repository.save(appointment);
    }

    public Appointment getAppointmentById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Appointment getAppointmentByProfessionalId(Long professional_id) {
        return repository.findByProfessionalId(professional_id).stream().findFirst().orElse(null);
    }

    public List<Appointment> getAllAppointments() {
        return repository.findAll();
    }

    public Appointment updateAppointment(Appointment appointment) {
        return repository.save(appointment);
    }

    public void deleteAppointment(Long id) {
        repository.deleteById(id);
    }
}
