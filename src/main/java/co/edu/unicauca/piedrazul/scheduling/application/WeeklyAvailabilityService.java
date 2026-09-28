package co.edu.unicauca.piedrazul.scheduling.application;

import co.edu.unicauca.piedrazul.scheduling.infrastructure.persistence.WeeklyAvailabilityEntity;
import co.edu.unicauca.piedrazul.scheduling.infrastructure.persistence.WeeklyAvailabilityRepository;
import co.edu.unicauca.piedrazul.scheduling.presentation.dto.WeeklyAvailabilityRequest;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
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

        validateNoOverlap(request);

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

    private static final DateTimeFormatter HOUR_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final String[] DAY_NAMES = {
            "lunes", "martes", "miercoles", "jueves",
            "viernes", "sabado", "domingo"
    };

    /**
     * Impide registrar un rango que se cruce con otro activo del mismo
     * profesional y del mismo dia (rangos contiguos como 08-12 y 12-14 si se permiten).
     */
    private void validateNoOverlap(WeeklyAvailabilityRequest request) {
        List<WeeklyAvailabilityEntity> existing =
                repository.findByProfessionalIdAndDayOfWeek(
                        request.professionalId(),
                        request.dayOfWeek()
                );

        for (WeeklyAvailabilityEntity other : existing) {
            if (!Boolean.TRUE.equals(other.getActive())) {
                continue;
            }

            boolean overlaps =
                    request.startTime().isBefore(other.getEndTime())
                            && request.endTime().isAfter(other.getStartTime());

            if (overlaps) {
                throw new IllegalArgumentException(
                        "El horario se cruza con otro ya registrado el "
                                + DAY_NAMES[request.dayOfWeek() - 1]
                                + " (" + other.getStartTime().format(HOUR_FORMAT)
                                + " - " + other.getEndTime().format(HOUR_FORMAT) + ")"
                );
            }
        }
    }
}
