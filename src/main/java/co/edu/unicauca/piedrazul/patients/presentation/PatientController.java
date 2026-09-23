package co.edu.unicauca.piedrazul.patients.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.piedrazul.patients.application.PatientService;
import co.edu.unicauca.piedrazul.patients.presentation.dto.PatientRequest;
import co.edu.unicauca.piedrazul.patients.presentation.dto.PatientResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody PatientRequest request) {
        PatientResponse response = patientService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PatientResponse>> findAll() {
        return ResponseEntity.ok(patientService.findAll());
    }

    @GetMapping("/me")
    public ResponseEntity<PatientResponse> findByCurrentUser(Authentication authentication) {
        String username = (String) authentication.getPrincipal();
        return ResponseEntity.ok(patientService.findByCurrentUser(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.findById(id));
    }

    @GetMapping("/document/{documentNumber}")
    public ResponseEntity<PatientResponse> findByDocumentNumber(@PathVariable String documentNumber) {
        return ResponseEntity.ok(patientService.findByDocumentNumber(documentNumber));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(patientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

