package co.edu.unicauca.piedrazul.appointments.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    List<AppointmentEntity> findByProfessionalId(Long professionalId);
    AppointmentEntity findByAppointmentDate(LocalDate appointmentDate);
    List<AppointmentEntity> findByProfessionalIdAndAppointmentDate(Long professionalId, LocalDate appointmentDate);
    List<AppointmentEntity> findByPatientId(Long patientId);
}

