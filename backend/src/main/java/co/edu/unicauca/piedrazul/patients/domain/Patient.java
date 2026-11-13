package co.edu.unicauca.piedrazul.patients.domain;

import java.time.LocalDate;

public class Patient {

    private Long id;
    private Long userId;
    private String documentNumber;
    private String phone;
    private LocalDate birthDate;

    public Patient() {
    }

    public Patient(Long id, Long userId, String documentNumber, String phone, LocalDate birthDate) {
        this.id = id;
        this.userId = userId;
        this.documentNumber = documentNumber;
        this.phone = phone;
        this.birthDate = birthDate;
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

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}

