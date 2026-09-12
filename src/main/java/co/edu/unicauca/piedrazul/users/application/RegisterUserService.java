package co.edu.unicauca.piedrazul.users.application;

import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;
import co.edu.unicauca.piedrazul.users.presentation.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserService(
            UserRepository repository,
            PasswordEncoder passwordEncoder) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {

        UserEntity user = new UserEntity();

        user.setUsername(request.username());
        user.setFullName(request.fullName());
        user.setEmail(request.email());

        user.setPassword(
                passwordEncoder.encode(
                        request.password()
                )
        );

        user.setEnabled(true);

        repository.save(user);
    }
}
