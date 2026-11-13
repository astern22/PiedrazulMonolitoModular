package co.edu.unicauca.piedrazul.scheduling.application;

import co.edu.unicauca.piedrazul.scheduling.infrastructure.persistence.WeeklyAvailabilityEntity;
import co.edu.unicauca.piedrazul.scheduling.infrastructure.persistence.WeeklyAvailabilityRepository;
import co.edu.unicauca.piedrazul.scheduling.presentation.dto.WeeklyAvailabilityRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeeklyAvailabilityService {

    private final WeeklyAvailabilityRepository repository;

    public WeeklyAvailabilityService(
            WeeklyAvailabilityRepository repository) {

        this.repository = repository;
    }

    public WeeklyAvailabilityEntity create(
            WeeklyAvailabilityRequest request) {

        if (!request.startTime().isBefore(request.endTime())) {
            throw new RuntimeException(
                    "La hora de inicio debe ser anterior a la hora de finalizacion"
            );
        }

        // Un profesional no puede tener dos horarios que se solapen el mismo dia.
        // Los rangos contiguos (08:00-12:00 y 12:00-14:00) son validos.
        repository.findByProfessionalIdAndDayOfWeekAndActiveTrue(
                        request.professionalId(),
                        request.dayOfWeek()
                )
                .stream()
                .filter(existing ->
                        request.startTime().isBefore(existing.getEndTime())
                                && request.endTime().isAfter(existing.getStartTime()))
                .findFirst()
                .ifPresent(existing -> {
                    throw new RuntimeException(
                            "El horario se cruza con otro ya registrado ("
                                    + existing.getStartTime() + " - "
                                    + existing.getEndTime() + ")"
                    );
                });

        WeeklyAvailabilityEntity availability =
                new WeeklyAvailabilityEntity();

        availability.setProfessionalId(
                request.professionalId()
        );

        availability.setDayOfWeek(
                request.dayOfWeek()
        );

        availability.setStartTime(
                request.startTime()
        );

        availability.setEndTime(
                request.endTime()
        );

        availability.setActive(true);

        return repository.save(availability);
    }

    public List<WeeklyAvailabilityEntity> findByProfessional(
            Long professionalId) {

        return repository.findByProfessionalIdAndActiveTrue(
                professionalId
        );
    }

    public List<WeeklyAvailabilityEntity> findByProfessionalAndDay(
            Long professionalId,
            Integer dayOfWeek) {

        return repository.findByProfessionalIdAndDayOfWeek(
                professionalId,
                dayOfWeek
        );
    }

    public void deactivate(Long id) {

        WeeklyAvailabilityEntity availability =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Disponibilidad no encontrada"
                                )
                        );

        availability.setActive(false);

        repository.save(availability);
    }
}
