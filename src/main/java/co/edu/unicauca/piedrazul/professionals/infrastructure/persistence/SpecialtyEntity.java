package co.edu.unicauca.piedrazul.professionals.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "specialties")
public class SpecialtyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    public SpecialtyEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
