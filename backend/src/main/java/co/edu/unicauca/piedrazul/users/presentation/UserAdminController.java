package co.edu.unicauca.piedrazul.users.presentation;

import co.edu.unicauca.piedrazul.users.application.SchedulerUserService;
import co.edu.unicauca.piedrazul.users.presentation.dto.SchedulerRegistrationRequest;
import co.edu.unicauca.piedrazul.users.presentation.dto.UserSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Administracion de cuentas internas. Todo /api/users/** exige rol ADMIN.
 */
@RestController
@RequestMapping("/api/users")
public class UserAdminController {

    private final SchedulerUserService schedulerUserService;

    public UserAdminController(SchedulerUserService schedulerUserService) {
        this.schedulerUserService = schedulerUserService;
    }

    @PostMapping("/schedulers")
    public ResponseEntity<UserSummaryResponse> createScheduler(
            @Valid @RequestBody SchedulerRegistrationRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserSummaryResponse.fromEntity(
                        schedulerUserService.createScheduler(request)));
    }

    @GetMapping("/schedulers")
    public ResponseEntity<List<UserSummaryResponse>> findSchedulers() {
        return ResponseEntity.ok(
                schedulerUserService.findSchedulers()
                        .stream()
                        .map(UserSummaryResponse::fromEntity)
                        .toList());
    }
}
