package co.edu.unicauca.piedrazul.scheduling.presentation;

import co.edu.unicauca.piedrazul.scheduling.application.WeeklyAvailabilityService;
import co.edu.unicauca.piedrazul.scheduling.infrastructure.persistence.WeeklyAvailabilityEntity;
import co.edu.unicauca.piedrazul.scheduling.presentation.dto.WeeklyAvailabilityRequest;
import co.edu.unicauca.piedrazul.scheduling.presentation.dto.WeeklyAvailabilityResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/availability")
public class WeeklyAvailabilityController {

    private final WeeklyAvailabilityService service;

    public WeeklyAvailabilityController(
            WeeklyAvailabilityService service) {

        this.service = service;
    }

    @PostMapping
    public ResponseEntity<WeeklyAvailabilityResponse> create(
            @Valid @RequestBody WeeklyAvailabilityRequest request) {

        WeeklyAvailabilityEntity availability =
                service.create(request);

        return ResponseEntity.ok(
                WeeklyAvailabilityResponse.fromEntity(
                        availability
                )
        );
    }

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<WeeklyAvailabilityResponse>>
    findByProfessional(
            @PathVariable Long professionalId) {

        List<WeeklyAvailabilityResponse> availability =
                service.findByProfessional(professionalId)
                        .stream()
                        .map(WeeklyAvailabilityResponse::fromEntity)
                        .toList();

        return ResponseEntity.ok(availability);
    }

    @GetMapping("/professional/{professionalId}/day/{dayOfWeek}")
    public ResponseEntity<List<WeeklyAvailabilityResponse>>
    findByProfessionalAndDay(
            @PathVariable Long professionalId,
            @PathVariable Integer dayOfWeek) {

        List<WeeklyAvailabilityResponse> availability =
                service.findByProfessionalAndDay(
                                professionalId,
                                dayOfWeek
                        )
                        .stream()
                        .map(WeeklyAvailabilityResponse::fromEntity)
                        .toList();

        return ResponseEntity.ok(availability);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id) {

        service.deactivate(id);

        return ResponseEntity.noContent().build();
    }
}
