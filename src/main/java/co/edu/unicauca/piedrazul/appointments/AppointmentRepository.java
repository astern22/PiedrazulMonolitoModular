package co.edu.unicauca.piedrazul.appointments;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDate;




public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByProfessionalId(Long professional_id);
    Appointment findByAppointmentDate(LocalDate appointment_date);
}
