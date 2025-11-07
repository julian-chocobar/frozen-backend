package com.enigcode.frozen_backend.common.security;

import com.enigcode.frozen_backend.production_phases.repository.ProductionPhaseRepository;
import com.enigcode.frozen_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityService {
    private final ProductionPhaseRepository productionPhaseRepository;

    public boolean isSupervisorOfPhase(Authentication authentication, Long phaseId) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        return productionPhaseRepository.existsByIdAndSector_Supervisor_Id(phaseId, userId);
    }
}
