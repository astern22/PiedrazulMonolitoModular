package co.edu.unicauca.piedrazul.users.presentation.dto;

import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;

public record UserSummaryResponse(
        Long id,
        String username,
        String fullName,
        String email,
        Boolean enabled
) {

    public static UserSummaryResponse fromEntity(UserEntity user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.isEnabled()
        );
    }
}
