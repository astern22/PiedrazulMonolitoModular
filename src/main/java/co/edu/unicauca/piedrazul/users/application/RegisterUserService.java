
package co.edu.unicauca.piedrazul.users.application;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.piedrazul.patients.infrastructure.persistence.PatientEntity;
import co.edu.unicauca.piedrazul.patients.infrastructure.persistence.PatientRepository;
import co.edu.unicauca.piedrazul.security.infrastructure.KeycloakAdminClient;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;
import co.edu.unicauca.piedrazul.users.presentation.dto.RegisterRequest;

@Service
public class RegisterUserService {

    private final UserRepository repository;
    private final PatientRepository patientRepository;
    private final KeycloakAdminClient keycloakAdminClient;

    public RegisterUserService(
            UserRepository repository,
            PatientRepository patientRepository,
            KeycloakAdminClient keycloakAdminClient) {

        this.repository = repository;
        this.patientRepository = patientRepository;
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @Transactional
    public UserEntity register(RegisterRequest request) {

        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();
        String fullName = request.fullName().trim();
        String documentNumber = request.documentNumber().trim();

        Optional<PatientEntity> existingPatient =
                patientRepository.findByDocumentNumber(documentNumber);

        if (existingPatient.isPresent()
                && existingPatient.get().getUserId() != null) {
            throw new IllegalArgumentException(
                    "Ya existe una cuenta asociada a este número de documento");
        }

        if (repository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "El nombre de usuario ya está registrado");
        }

        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "El correo electrónico ya está registrado");
        }

        String keycloakUserId = keycloakAdminClient.createUser(
                username,
                email,
                fullName,
                request.password()
        );

        try {

            keycloakAdminClient.assignRealmRole(
                    keycloakUserId,
                    "PATIENT"
            );

            UserEntity user = new UserEntity();
            user.setUsername(username);
            user.setFullName(fullName);
            user.setEmail(email);
            user.setEnabled(true);
            user.setKeycloakUserId(keycloakUserId);

            user.setPassword(null);

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

        } catch (RuntimeException exception) {

            try {
                keycloakAdminClient.deleteUser(keycloakUserId);
            } catch (RuntimeException deleteException) {
                exception.addSuppressed(deleteException);
            }

            throw exception;
        }
    }
}
