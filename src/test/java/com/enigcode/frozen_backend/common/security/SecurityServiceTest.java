package com.enigcode.frozen_backend.common.security;

import com.enigcode.frozen_backend.production_phases.repository.ProductionPhaseRepository;
import com.enigcode.frozen_backend.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private ProductionPhaseRepository productionPhaseRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SecurityService securityService;

    private User supervisorUser;

    @BeforeEach
    void setUp() {
        supervisorUser = new User();
        supervisorUser.setId(10L);
        when(authentication.getPrincipal()).thenReturn(supervisorUser);
    }

    @Test
    void isSupervisorOfPhase_ReturnsTrue_WhenExistsRelation() {
        Long phaseId = 5L;
        when(productionPhaseRepository.existsByIdAndSector_Supervisor_Id(phaseId, supervisorUser.getId())).thenReturn(true);

        boolean result = securityService.isSupervisorOfPhase(authentication, phaseId);

        assertTrue(result);
        verify(productionPhaseRepository).existsByIdAndSector_Supervisor_Id(phaseId, supervisorUser.getId());
    }

    @Test
    void isSupervisorOfPhase_ReturnsFalse_WhenRelationDoesNotExist() {
        Long phaseId = 7L;
        when(productionPhaseRepository.existsByIdAndSector_Supervisor_Id(phaseId, supervisorUser.getId())).thenReturn(false);

        boolean result = securityService.isSupervisorOfPhase(authentication, phaseId);

        assertFalse(result);
        verify(productionPhaseRepository).existsByIdAndSector_Supervisor_Id(phaseId, supervisorUser.getId());
    }
}
