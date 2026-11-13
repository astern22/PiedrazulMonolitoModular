package co.edu.unicauca.piedrazul.professionals.presentation;

import co.edu.unicauca.piedrazul.professionals.application.SpecialtyService;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.SpecialtyRequest;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.SpecialtyResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/specialties")
public class SpecialtyController {
    private final SpecialtyService service;

    public SpecialtyController(SpecialtyService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SpecialtyResponse create(
            @Valid @RequestBody SpecialtyRequest request) {

        return service.create(request);
    }

    @GetMapping
    public List<SpecialtyResponse> findAll() {
        return service.findAll();
    }
}
