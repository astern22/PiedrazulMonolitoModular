package co.edu.unicauca.piedrazul.users.presentation.dto;

public record RegisterRequest(
        String username,
        String password,
        String fullName,
        String email,
        String documentNumber
) {}