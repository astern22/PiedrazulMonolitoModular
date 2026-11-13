package co.edu.unicauca.piedrazul.scheduling.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeeklyAvailabilityRepository
        extends JpaRepository<WeeklyAvailabilityEntity, Long> {

    List<WeeklyAvailabilityEntity> findByProfessionalId(
            Long professionalId
    );

    List<WeeklyAvailabilityEntity> findByProfessionalIdAndActiveTrue(
            Long professionalId
    );

    List<WeeklyAvailabilityEntity> findByProfessionalIdAndDayOfWeekAndActiveTrue(
            Long professionalId,
            Integer dayOfWeek
    );

    List<WeeklyAvailabilityEntity> findByProfessionalIdAndDayOfWeek(
            Long professionalId,
            Integer dayOfWeek
    );
}
