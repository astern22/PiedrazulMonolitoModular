package co.edu.unicauca.piedrazul.professionals.presentation;

import co.edu.unicauca.piedrazul.professionals.application.ProfessionalService;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalEntity;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.AppointmentIntervalRequest;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.ProfessionalRegistrationRequest;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.ProfessionalRequest;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.ProfessionalResponse;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/professionals")
public class ProfessionalController {

    private final ProfessionalService service;

    public ProfessionalController(
            ProfessionalService service) {

        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProfessionalResponse> create(
            @Valid @RequestBody ProfessionalRequest request) {

        ProfessionalEntity professional =
                service.create(request);

        return ResponseEntity.ok(toResponse(professional));
    }

    /** Registra la cuenta de usuario y el profesional en un solo paso (sin pedir IDs). */
    @PostMapping("/register")
    public ResponseEntity<ProfessionalResponse> register(
            @Valid @RequestBody ProfessionalRegistrationRequest request) {

        ProfessionalEntity professional =
                service.register(request);

        return ResponseEntity.ok(toResponse(professional));
    }

    @GetMapping
    public ResponseEntity<List<ProfessionalResponse>> findAll() {
        return ResponseEntity.ok(toResponses(service.findAll()));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProfessionalResponse>> findActive() {
        return ResponseEntity.ok(toResponses(service.findActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalResponse> findById(@PathVariable Long id) {
        ProfessionalEntity professional =
                service.findById(id);

        return ResponseEntity.ok(toResponse(professional));
    }

    // Revisar bien
    @GetMapping("/specialty/{specialtyId}")
    public ResponseEntity<List<ProfessionalResponse>> findBySpecialty(@PathVariable Long specialtyId) {
        return ResponseEntity.ok(toResponses(service.findBySpecialty(specialtyId)));
    }

    /** Cambia la duracion de las citas de un profesional. */
    @PatchMapping("/{id}/appointment-interval")
    public ResponseEntity<ProfessionalResponse> updateAppointmentInterval(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentIntervalRequest request) {

        ProfessionalEntity professional =
                service.updateAppointmentInterval(
                        id,
                        request.appointmentIntervalMinutes()
                );

        return ResponseEntity.ok(toResponse(professional));
    }

    private ProfessionalResponse toResponse(ProfessionalEntity professional) {
        return toResponses(List.of(professional)).getFirst();
    }

    private List<ProfessionalResponse> toResponses(List<ProfessionalEntity> professionals) {
        Map<Long, String> names = service.findFullNamesByUserIds(
                professionals.stream()
                        .map(ProfessionalEntity::getUserId)
                        .toList()
        );

        return professionals.stream()
                .map(professional -> ProfessionalResponse.fromEntity(
                        professional,
                        names.get(professional.getUserId())
                ))
                .toList();
    }
}
