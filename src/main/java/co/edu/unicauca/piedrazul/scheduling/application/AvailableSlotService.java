package co.edu.unicauca.piedrazul.scheduling.application;

import co.edu.unicauca.piedrazul.appointments.application.AppointmentService;
import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentEntity;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalEntity;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalRepository;
import co.edu.unicauca.piedrazul.scheduling.infrastructure.persistence.WeeklyAvailabilityEntity;
import co.edu.unicauca.piedrazul.scheduling.infrastructure.persistence.WeeklyAvailabilityRepository;
import co.edu.unicauca.piedrazul.scheduling.presentation.dto.AvailableSlotResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvailableSlotService {

    private final WeeklyAvailabilityRepository availabilityRepository;
    private final ProfessionalRepository professionalRepository;
    private final AppointmentService appointmentService;

    public AvailableSlotService(
            WeeklyAvailabilityRepository availabilityRepository,
            ProfessionalRepository professionalRepository,
            AppointmentService appointmentService) {

        this.availabilityRepository = availabilityRepository;
        this.professionalRepository = professionalRepository;
        this.appointmentService = appointmentService;
    }

    public List<AvailableSlotResponse> getAvailableSlots(
            Long professionalId,
            LocalDate date) {

        ProfessionalEntity professional =
                professionalRepository.findById(professionalId)
                        .orElseThrow(() ->
                                new RuntimeException("Profesional no encontrado"));

        if (!Boolean.TRUE.equals(professional.getActive())) {
            throw new RuntimeException("El profesional no está activo");
        }

        int dayOfWeek = date.getDayOfWeek().getValue();

        List<WeeklyAvailabilityEntity> availabilities =
                availabilityRepository
                        .findByProfessionalIdAndDayOfWeek(
                                professionalId,
                                dayOfWeek
                        )
                        .stream()
                        .filter(WeeklyAvailabilityEntity::getActive)
                        .toList();

        List<AppointmentEntity> appointments =
                appointmentService.findAppointments(
                        professionalId,
                        date
                );

        List<AvailableSlotResponse> availableSlots =
                new ArrayList<>();

        int interval =
                professional.getAppointmentIntervalMinutes();

        for (WeeklyAvailabilityEntity availability : availabilities) {

            LocalTime currentTime =
                    availability.getStartTime();

            while (!currentTime.plusMinutes(interval)
                    .isAfter(availability.getEndTime())) {

                LocalTime slotStart = currentTime;

                LocalTime slotEnd =
                        currentTime.plusMinutes(interval);

                boolean occupied =
                        appointments.stream()
                                .anyMatch(appointment ->
                                        slotStart.isBefore(
                                                appointment.getEndTime()
                                        )
                                                &&
                                                slotEnd.isAfter(
                                                        appointment.getStartTime()
                                                )
                                );

                if (!occupied) {
                    availableSlots.add(
                            new AvailableSlotResponse(
                                    slotStart,
                                    slotEnd
                            )
                    );
                }

                currentTime = slotEnd;
            }
        }

        return availableSlots;
    }
}