package co.edu.unicauca.piedrazul.appointments.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    List<AppointmentEntity> findByProfessionalId(Long professionalId);
    List<AppointmentEntity> findByAppointmentDate(LocalDate appointmentDate);
    List<AppointmentEntity> findByProfessionalIdAndAppointmentDate(Long professionalId, LocalDate appointmentDate);
    List<AppointmentEntity> findByPatientId(Long patientId);
    boolean existsByProfessionalIdAndAppointmentDateAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
            Long professionalId,
            LocalDate appointmentDate,
            String status,
            LocalTime requestedEndTime,
            LocalTime requestedStartTime
    );
}

