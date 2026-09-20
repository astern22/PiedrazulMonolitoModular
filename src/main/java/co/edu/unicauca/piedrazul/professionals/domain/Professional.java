package co.edu.unicauca.piedrazul.professionals.domain;

public class Professional {
    private Long id;
    private Long userId;
    private Long specialtyId;
    private String professionalType;
    private Integer appointmentIntervalMinutes;
    private Boolean active;

    public Professional() {
    }

    public Professional(
            Long id,
            Long userId,
            Long specialtyId,
            String professionalType,
            Integer appointmentIntervalMinutes,
            Boolean active) {

        this.id = id;
        this.userId = userId;
        this.specialtyId = specialtyId;
        this.professionalType = professionalType;
        this.appointmentIntervalMinutes = appointmentIntervalMinutes;
        this.active = active;
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

    public String getProfessionalType() {
        return professionalType;
    }

    public void setProfessionalType(String professionalType) {
        this.professionalType = professionalType;
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
