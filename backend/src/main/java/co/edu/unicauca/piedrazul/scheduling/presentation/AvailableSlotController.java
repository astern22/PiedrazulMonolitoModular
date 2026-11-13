package co.edu.unicauca.piedrazul.scheduling.presentation;

import co.edu.unicauca.piedrazul.scheduling.application.AvailableSlotService;
import co.edu.unicauca.piedrazul.scheduling.presentation.dto.AvailableSlotResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
public class AvailableSlotController {

    private final AvailableSlotService service;

    public AvailableSlotController(AvailableSlotService service) {
        this.service = service;
    }

    @GetMapping("/professional/{professionalId}/slots")
    public ResponseEntity<List<AvailableSlotResponse>> getAvailableSlots(
            @PathVariable Long professionalId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        List<AvailableSlotResponse> slots =
                service.getAvailableSlots(
                        professionalId,
                        date
                );

        return ResponseEntity.ok(slots);
    }
}