package co.edu.unicauca.piedrazul.users.application;

import co.edu.unicauca.piedrazul.security.JwtService;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoginService {
    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public LoginService(
            UserRepository repository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String login(
            String username,
            String password) {

        UserEntity user =
                repository.findByUsername(username)
                        .orElseThrow();

        boolean valid =
                passwordEncoder.matches(
                        password,
                        user.getPassword()
                );

        if (!valid) {
            throw new RuntimeException(
                    "Credenciales invalidas"
            );
        }

        List<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName())
                .toList();

        return jwtService.generateToken(
                user.getUsername(),
                roles
        );
    }
}
