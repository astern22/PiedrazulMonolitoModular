package co.edu.unicauca.piedrazul.users.presentation.dto;

import co.edu.unicauca.piedrazul.users.application.RegisterUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final RegisterUserService registerUserService;

    public AuthController(
            RegisterUserService registerUserService) {

        this.registerUserService = registerUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @RequestBody RegisterRequest request) {

        registerUserService.register(request);

        return ResponseEntity.ok().build();
    }
}
