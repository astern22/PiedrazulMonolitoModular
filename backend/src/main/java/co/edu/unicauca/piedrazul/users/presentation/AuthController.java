package co.edu.unicauca.piedrazul.users.presentation;

import co.edu.unicauca.piedrazul.users.application.LoginService;
import co.edu.unicauca.piedrazul.users.application.RegisterUserService;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.presentation.dto.LoginRequest;
import co.edu.unicauca.piedrazul.users.presentation.dto.LoginResponse;
import co.edu.unicauca.piedrazul.users.presentation.dto.RegisterRequest;
import co.edu.unicauca.piedrazul.users.presentation.dto.RegisterResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final RegisterUserService registerUserService;
    private final LoginService loginService;

    public AuthController(RegisterUserService registerUserService, LoginService loginService) {
        this.registerUserService = registerUserService;
        this.loginService = loginService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody RegisterRequest request) {

        UserEntity user = registerUserService.register(request);

        return ResponseEntity.ok(
                new RegisterResponse(user.getUsername())
        );
    }

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request) {

        String token =
                loginService.login(
                        request.username(),
                        request.password()
                );

        return new LoginResponse(token);
    }
}
