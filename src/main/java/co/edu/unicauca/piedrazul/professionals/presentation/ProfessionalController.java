package co.edu.unicauca.piedrazul.professionals.presentation;

import co.edu.unicauca.piedrazul.professionals.application.ProfessionalService;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalEntity;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.ProfessionalRequest;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.ProfessionalResponse;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professionals")
public class ProfessionalController {

    private final ProfessionalService service;

    public ProfessionalController(
            ProfessionalService service) {

        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProfessionalResponse>> findAll() {
        List<ProfessionalResponse> professionals =
                service.findAll()
                        .stream()
                        .map(ProfessionalResponse::fromEntity)
                        .toList();

        return ResponseEntity.ok(professionals);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProfessionalResponse>> findActive() {
        List<ProfessionalResponse> professionals =
                service.findActive()
                        .stream()
                        .map(ProfessionalResponse::fromEntity)
                        .toList();

        return ResponseEntity.ok(professionals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalResponse> findById(@PathVariable Long id) {
        ProfessionalEntity professional =
                service.findById(id);

        return ResponseEntity.ok(
                ProfessionalResponse.fromEntity(
                        professional
                )
        );
    }

    // Revisar bien
    @GetMapping("/specialty/{specialtyId}")
    public ResponseEntity<List<ProfessionalResponse>> findBySpecialty(@PathVariable Long specialtyId) {
        List<ProfessionalResponse> professionals =
                service.findBySpecialty(specialtyId)
                        .stream()
                        .map(ProfessionalResponse::fromEntity)
                        .toList();

        return ResponseEntity.ok(professionals);
    }
}