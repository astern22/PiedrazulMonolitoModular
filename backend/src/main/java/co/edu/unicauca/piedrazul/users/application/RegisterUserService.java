package co.edu.unicauca.piedrazul.users.application;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.unicauca.piedrazul.patients.infrastructure.persistence.PatientEntity;
import co.edu.unicauca.piedrazul.patients.infrastructure.persistence.PatientRepository;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleRepository;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;
import co.edu.unicauca.piedrazul.users.presentation.dto.RegisterRequest;

@Service
public class RegisterUserService {

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserService(
            UserRepository repository,
            RoleRepository roleRepository,
            PatientRepository patientRepository,
            PasswordEncoder passwordEncoder) {

        this.repository = repository;
        this.roleRepository = roleRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity register(RegisterRequest request) {

        String documentNumber = request.documentNumber().trim();

        Optional<PatientEntity> existingPatient =
                patientRepository.findByDocumentNumber(documentNumber);

        if (existingPatient.isPresent() && existingPatient.get().getUserId() != null) {
            throw new RuntimeException("Ya existe una cuenta asociada a este numero de documento");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);

        RoleEntity patientRole = roleRepository
                .findByName("PATIENT")
                .orElseThrow(() -> new RuntimeException("Rol PATIENT no encontrado"));

        user.getRoles().add(patientRole);

        UserEntity savedUser = repository.save(user);

        if (existingPatient.isPresent()) {
            PatientEntity patient = existingPatient.get();
            patient.setUserId(savedUser.getId());
            patientRepository.save(patient);
        } else {
            PatientEntity newPatient = new PatientEntity();
            newPatient.setDocumentNumber(documentNumber);
            newPatient.setUserId(savedUser.getId());
            patientRepository.save(newPatient);
        }

        return savedUser;
    }
}