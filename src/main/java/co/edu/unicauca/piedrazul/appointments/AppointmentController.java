package co.edu.unicauca.piedrazul.appointments;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController 
@RequestMapping ("api/appointments")
public class AppointmentController {

    private final AppointmentService service;

    AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody Appointment appointment) {
        Appointment createdAppointment = service.createAppointment(appointment);
        return ResponseEntity.ok(createdAppointment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable Long id, @Valid @RequestBody Appointment appointment) {
        Appointment existingAppointment = service.getAppointmentById(id); 
        if (existingAppointment != null) {

            existingAppointment.setProfessionalId(appointment.getProfessionalId());
            existingAppointment.setPatientId(appointment.getPatientId());
            existingAppointment.setAppointmentDate(appointment.getAppointmentDate());
            existingAppointment.setStartTime(appointment.getStartTime());
            existingAppointment.setEndTime(appointment.getEndTime());
            existingAppointment.setStatus(appointment.getStatus());
            existingAppointment.setCreatedAt(appointment.getCreatedAt());

            Appointment updatedAppointment = service.updateAppointment(existingAppointment);
            return ResponseEntity.ok(updatedAppointment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = service.getAppointmentById(id);
        if (appointment != null) {
            return ResponseEntity.ok(appointment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/professional/{professional_id}")
    public ResponseEntity<Appointment> getAppointmentByProfessionalId(@PathVariable Long professional_id) {
        Appointment appointment = service.getAppointmentByProfessionalId(professional_id);
        if (appointment != null) {
            return ResponseEntity.ok(appointment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("")
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        List<Appointment> appointments = service.getAllAppointments();
        return ResponseEntity.ok(appointments);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        Appointment existingAppointment = service.getAppointmentById(id);
        if (existingAppointment != null) {
            service.deleteAppointment(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
}