package co.edu.unicauca.piedrazul.users.application;

import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleRepository;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;
import co.edu.unicauca.piedrazul.users.presentation.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserService {

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserService(
            UserRepository repository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.repository = repository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity register(RegisterRequest request) {

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

        RoleEntity patientRole = roleRepository
                .findByName("PATIENT")
                .orElseThrow(() ->
                        new RuntimeException("Rol PATIENT no encontrado")
                );

        user.getRoles().add(patientRole);

        return repository.save(user);
    }
}