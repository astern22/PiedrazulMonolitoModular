package co.edu.unicauca.piedrazul.professionals.application;

import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.SpecialtyEntity;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.SpecialtyRepository;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.SpecialtyRequest;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.SpecialtyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecialtyServiceTest {

    @Mock
    private SpecialtyRepository repository;

    @InjectMocks
    private SpecialtyService service;

    private SpecialtyEntity sampleEntity;

    @BeforeEach
    void setUp() {
        sampleEntity = new SpecialtyEntity();
        ReflectionTestUtils.setField(sampleEntity, "id", 1L);
        sampleEntity.setName("Cardiologia");
    }

    @Test
    void testCreate_Success() {
        SpecialtyRequest request = new SpecialtyRequest("Cardiologia");
        when(repository.existsByNameIgnoreCase("Cardiologia")).thenReturn(false);
        when(repository.save(any(SpecialtyEntity.class))).thenAnswer(i -> {
            SpecialtyEntity e = i.getArgument(0);
            ReflectionTestUtils.setField(e, "id", 1L);
            return e;
        });

        SpecialtyResponse response = service.create(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Cardiologia", response.name());
        verify(repository).save(any(SpecialtyEntity.class));
    }

    @Test
    void testCreate_AlreadyExists_ThrowsException() {
        SpecialtyRequest request = new SpecialtyRequest("Cardiologia");
        when(repository.existsByNameIgnoreCase("Cardiologia")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.create(request));
        assertEquals("La especialidad ya existe", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(sampleEntity));

        List<SpecialtyResponse> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("Cardiologia", result.getFirst().name());
        verify(repository).findAll();
    }
}
