package co.edu.unicauca.piedrazul.appointments.presentation;

import java.time.LocalDate;
import java.util.List;

import co.edu.unicauca.piedrazul.appointments.application.AppointmentService;
import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentEntity;
import co.edu.unicauca.piedrazul.appointments.presentation.command.CreateAppointmentCommand;
import co.edu.unicauca.piedrazul.appointments.presentation.dto.AppointmentRequest;
import co.edu.unicauca.piedrazul.appointments.presentation.dto.AppointmentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody AppointmentRequest request) {

        CreateAppointmentCommand command =
                new CreateAppointmentCommand(
                        request.patientId(),
                        request.professionalId(),
                        request.appointmentDate(),
                        request.startTime(),
                        request.endTime()
                );

        AppointmentEntity createdAppointment =
                service.createAppointment(command);

        AppointmentResponse response = new AppointmentResponse(
                createdAppointment.getId(),
                createdAppointment.getPatientId(),
                createdAppointment.getProfessionalId(),
                createdAppointment.getAppointmentDate(),
                createdAppointment.getStartTime(),
                createdAppointment.getEndTime(),
                createdAppointment.getStatus()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentEntity> updateAppointment(@PathVariable Long id, @Valid @RequestBody AppointmentEntity appointment) {
        AppointmentEntity existingAppointment = service.getAppointmentById(id); 
        if (existingAppointment != null) {
            existingAppointment.setProfessionalId(appointment.getProfessionalId());
            existingAppointment.setPatientId(appointment.getPatientId());
            existingAppointment.setAppointmentDate(appointment.getAppointmentDate());
            existingAppointment.setStartTime(appointment.getStartTime());
            existingAppointment.setEndTime(appointment.getEndTime());
            existingAppointment.setStatus(appointment.getStatus());
            if (appointment.getCreatedAt() != null) {
                existingAppointment.setCreatedAt(appointment.getCreatedAt());
            }

            AppointmentEntity updatedAppointment = service.updateAppointment(existingAppointment);
            return ResponseEntity.ok(updatedAppointment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentEntity> getAppointmentById(@PathVariable Long id) {
        AppointmentEntity appointment = service.getAppointmentById(id);
        if (appointment != null) {
            return ResponseEntity.ok(appointment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<AppointmentEntity> getAppointmentByProfessionalId(@PathVariable("professionalId") Long professionalId) {
        AppointmentEntity appointment = service.getAppointmentByProfessionalId(professionalId);
        if (appointment != null) {
            return ResponseEntity.ok(appointment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<AppointmentEntity>> getAllAppointments() {
        List<AppointmentEntity> appointments = service.getAllAppointments();
        return ResponseEntity.ok(appointments);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        AppointmentEntity existingAppointment = service.getAppointmentById(id);
        if (existingAppointment != null) {
            service.deleteAppointment(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public List<AppointmentEntity> search(
            @RequestParam Long professionalId,
            @RequestParam LocalDate date) {

        return service.findAppointments(
                professionalId,
                date
        );
    }
}

