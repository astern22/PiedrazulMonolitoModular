package co.edu.unicauca.piedrazul.patients.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.edu.unicauca.piedrazul.patients.infrastructure.persistence.PatientEntity;
import co.edu.unicauca.piedrazul.patients.infrastructure.persistence.PatientRepository;
import co.edu.unicauca.piedrazul.patients.presentation.dto.PatientRequest;
import co.edu.unicauca.piedrazul.patients.presentation.dto.PatientResponse;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public PatientService(PatientRepository patientRepository, UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    public PatientResponse create(PatientRequest request) {
        if (patientRepository.existsByDocumentNumber(request.documentNumber())) {
            throw new RuntimeException("El numero de documento ya esta registrado");
        }

        if (request.userId() != null) {
            if (!userRepository.existsById(request.userId())) {
                throw new RuntimeException("El usuario con ID " + request.userId() + " no existe");
            }
            if (patientRepository.existsByUserId(request.userId())) {
                throw new RuntimeException("El usuario ya esta registrado como paciente");
            }
        }

        PatientEntity entity = new PatientEntity();
        entity.setDocumentNumber(request.documentNumber().trim());
        entity.setPhone(request.phone() != null ? request.phone().trim() : null);
        entity.setBirthDate(request.birthDate());
        entity.setUserId(request.userId());

        PatientEntity saved = patientRepository.save(entity);
        return toResponse(saved);
    }

    public List<PatientResponse> findAll() {
        return patientRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PatientResponse findById(Long id) {
        PatientEntity entity = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));
        return toResponse(entity);
    }

    public PatientResponse findByCurrentUser(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        PatientEntity entity = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("No se encontro un paciente vinculado a tu usuario"));
        return toResponse(entity);
    }

    public PatientResponse findByDocumentNumber(String documentNumber) {
        PatientEntity entity = patientRepository.findByDocumentNumber(documentNumber)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con documento: " + documentNumber));
        return toResponse(entity);
    }

    public PatientResponse update(Long id, PatientRequest request) {
        PatientEntity entity = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));

        // Validar documento duplicado si cambio
        if (!entity.getDocumentNumber().equals(request.documentNumber().trim())) {
            Optional<PatientEntity> existingWithDoc = patientRepository.findByDocumentNumber(request.documentNumber().trim());
            if (existingWithDoc.isPresent() && !existingWithDoc.get().getId().equals(id)) {
                throw new RuntimeException("El numero de documento ya pertenece a otro paciente");
            }
        }

        // Validar userId si cambio
        if (request.userId() != null && !request.userId().equals(entity.getUserId())) {
            if (!userRepository.existsById(request.userId())) {
                throw new RuntimeException("El usuario con ID " + request.userId() + " no existe");
            }
            Optional<PatientEntity> existingWithUser = patientRepository.findByUserId(request.userId());
            if (existingWithUser.isPresent() && !existingWithUser.get().getId().equals(id)) {
                throw new RuntimeException("El usuario ya esta vinculado a otro paciente");
            }
        }

        entity.setDocumentNumber(request.documentNumber().trim());
        entity.setPhone(request.phone() != null ? request.phone().trim() : null);
        entity.setBirthDate(request.birthDate());
        entity.setUserId(request.userId());

        PatientEntity updated = patientRepository.save(entity);
        return toResponse(updated);
    }

    public void delete(Long id) {
        PatientEntity entity = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));
        patientRepository.delete(entity);
    }

    private PatientResponse toResponse(PatientEntity entity) {
        String fullName = null;
        String email = null;
        if (entity.getUserId() != null) {
            Optional<UserEntity> userOpt = userRepository.findById(entity.getUserId());
            if (userOpt.isPresent()) {
                fullName = userOpt.get().getFullName();
                email = userOpt.get().getEmail();
            }
        }
        return PatientResponse.fromEntity(entity, fullName, email);
    }
}

