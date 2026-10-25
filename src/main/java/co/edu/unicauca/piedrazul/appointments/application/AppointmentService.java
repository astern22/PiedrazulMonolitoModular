package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentEntity;
import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentRepository;
import co.edu.unicauca.piedrazul.appointments.presentation.command.CreateAppointmentCommand;
import co.edu.unicauca.piedrazul.patients.infrastructure.persistence.PatientRepository;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalEntity;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalRepository;
import co.edu.unicauca.piedrazul.scheduling.infrastructure.persistence.WeeklyAvailabilityEntity;
import co.edu.unicauca.piedrazul.scheduling.infrastructure.persistence.WeeklyAvailabilityRepository;
import co.edu.unicauca.piedrazul.settings.application.SystemSettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository repository;
    private final ProfessionalRepository professionalRepository;
    private final WeeklyAvailabilityRepository availabilityRepository;
    private final PatientRepository patientRepository;
    private final SystemSettingsService settingsService;

    public AppointmentService(
            AppointmentRepository repository,
            ProfessionalRepository professionalRepository,
            WeeklyAvailabilityRepository availabilityRepository,
            PatientRepository patientRepository,
            SystemSettingsService settingsService) {
        this.repository = repository;
        this.professionalRepository = professionalRepository;
        this.availabilityRepository = availabilityRepository;
        this.patientRepository = patientRepository;
        this.settingsService = settingsService;
    }

    @Transactional
    public AppointmentEntity createAppointment(CreateAppointmentCommand command) {

        if (command.patientId() == null
                || command.professionalId() == null
                || command.appointmentDate() == null
                || command.startTime() == null
                || command.endTime() == null) {
            throw new IllegalArgumentException(
                    "Todos los datos de la cita son obligatorios.");
        }

        LocalDate date = command.appointmentDate();

        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "No se puede agendar una cita en una fecha anterior a hoy.");
        }

        if (!patientRepository.existsById(command.patientId())) {
            throw new IllegalArgumentException(
                    "El paciente indicado no existe.");
        }

        ProfessionalEntity professional = professionalRepository
                .findById(command.professionalId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El profesional indicado no existe."));

        if (!Boolean.TRUE.equals(professional.getActive())) {
            throw new IllegalArgumentException(
                    "El profesional no está activo.");
        }

        if (!command.startTime().isBefore(command.endTime())) {
            throw new IllegalArgumentException(
                    "La hora de inicio debe ser anterior a la hora de fin.");
        }

        Integer interval = professional.getAppointmentIntervalMinutes();

        if (interval == null || interval <= 0) {
            throw new IllegalArgumentException(
                    "El profesional no tiene un intervalo de citas válido.");
        }

        long requestedMinutes = Duration.between(
                command.startTime(), command.endTime()).toMinutes();

        if (requestedMinutes != interval) {
            throw new IllegalArgumentException(
                    "La duración de la cita debe ser de "
                            + interval + " minutos.");
        }

        int dayOfWeek = date.getDayOfWeek().getValue();

        List<WeeklyAvailabilityEntity> availabilities =
                availabilityRepository
                        .findByProfessionalIdAndDayOfWeekAndActiveTrue(
                                professional.getId(), dayOfWeek);

        boolean fitsAvailability = availabilities.stream().anyMatch(
                availability -> {
                    boolean insideRange =
                            !command.startTime().isBefore(
                                    availability.getStartTime())
                                    && !command.endTime().isAfter(
                                    availability.getEndTime());

                    if (!insideRange) {
                        return false;
                    }

                    long minutesFromOpening = Duration.between(
                            availability.getStartTime(),
                            command.startTime()).toMinutes();

                    return minutesFromOpening >= 0
                            && minutesFromOpening % interval == 0;
                });

        if (!fitsAvailability) {
            throw new IllegalArgumentException(
                    "El horario solicitado no corresponde "
                            + "a la disponibilidad del profesional.");
        }

        boolean overlaps = repository
                .existsByProfessionalIdAndAppointmentDateAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
                        professional.getId(),
                        date,
                        "CANCELLED",
                        command.endTime(),
                        command.startTime());

        if (overlaps) {
            throw new IllegalArgumentException(
                    "El profesional ya tiene una cita en ese horario.");
        }

        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setPatientId(command.patientId());
        appointment.setProfessionalId(command.professionalId());
        appointment.setAppointmentDate(date);
        appointment.setStartTime(command.startTime());
        appointment.setEndTime(command.endTime());
        appointment.setStatus("SCHEDULED");

        return repository.save(appointment);
    }

    public AppointmentEntity getAppointmentById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<AppointmentEntity> getAppointmentsByProfessionalId(
            Long professionalId) {
        return repository.findByProfessionalId(professionalId);
    }

    public List<AppointmentEntity> getAllAppointments() {
        return repository.findAll();
    }

    public AppointmentEntity updateAppointment(AppointmentEntity appointment) {
        return repository.save(appointment);
    }

    public void deleteAppointment(Long id) {
        repository.deleteById(id);
    }

    public List<AppointmentEntity> findAppointments(Long professionalId, LocalDate date) {
        return repository
                .findByProfessionalIdAndAppointmentDate(
                        professionalId,
                        date
                );
    }

    public List<AppointmentEntity> getAppointmentsByPatientId(Long patientId) {
    return repository.findByPatientId(patientId);
    }
}

