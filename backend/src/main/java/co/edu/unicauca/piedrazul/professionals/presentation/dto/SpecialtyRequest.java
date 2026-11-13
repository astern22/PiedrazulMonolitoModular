package co.edu.unicauca.piedrazul.professionals.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record SpecialtyRequest(
        @NotBlank
        String name
) {
}
