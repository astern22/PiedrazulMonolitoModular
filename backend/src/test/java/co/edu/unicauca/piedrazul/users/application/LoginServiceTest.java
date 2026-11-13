package co.edu.unicauca.piedrazul.users.application;

import co.edu.unicauca.piedrazul.security.JwtService;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private LoginService loginService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        RoleEntity role = new RoleEntity();
        role.setName("PATIENT");

        user = new UserEntity();
        user.setId(5L);
        user.setUsername("juanperez");
        user.setPassword("hashedPassword");
        user.setEnabled(true);
        user.setRoles(Set.of(role));
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByUsername("juanperez")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPassword", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken("juanperez", List.of("PATIENT"))).thenReturn("mock-jwt-token");

        String token = loginService.login("juanperez", "rawPassword");

        assertNotNull(token);
        assertEquals("mock-jwt-token", token);
        verify(userRepository).findByUsername("juanperez");
        verify(passwordEncoder).matches("rawPassword", "hashedPassword");
        verify(jwtService).generateToken("juanperez", List.of("PATIENT"));
    }

    @Test
    void testLogin_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                loginService.login("unknown", "password")
        );

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString(), anyList());
    }

    @Test
    void testLogin_InvalidPassword_ThrowsException() {
        when(userRepository.findByUsername("juanperez")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                loginService.login("juanperez", "wrongPassword")
        );

        assertEquals("Credenciales invalidas", exception.getMessage());
        verify(jwtService, never()).generateToken(anyString(), anyList());
    }
}
