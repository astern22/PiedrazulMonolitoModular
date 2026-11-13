package co.edu.unicauca.piedrazul.professionals.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "professionals")
public class ProfessionalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "specialty_id", nullable = false)
    private Long specialtyId;

    @Column(name = "appointment_interval_minutes", nullable = false)
    private Integer appointmentIntervalMinutes;

    @Column(nullable = false)
    private Boolean active = true;

    public ProfessionalEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(Long specialtyId) {
        this.specialtyId = specialtyId;
    }

    public Integer getAppointmentIntervalMinutes() {
        return appointmentIntervalMinutes;
    }

    public void setAppointmentIntervalMinutes(Integer appointmentIntervalMinutes) {
        this.appointmentIntervalMinutes = appointmentIntervalMinutes;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}