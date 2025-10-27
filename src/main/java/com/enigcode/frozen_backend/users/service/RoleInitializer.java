package com.enigcode.frozen_backend.users.service;

import com.enigcode.frozen_backend.users.model.RoleEntity;
import com.enigcode.frozen_backend.users.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
    }

    private void initializeRoles() {
        String[] roles = {
                "ADMIN",
                "GERENTE_GENERAL",
                "GERENTE_DE_PLANTA",
                "SUPERVISOR_DE_CALIDAD",
                "SUPERVISOR_DE_ALMACEN",
                "SUPERVISOR_DE_PRODUCCION",
                "OPERARIO_DE_CALIDAD",
                "OPERARIO_DE_ALMACEN",
                "OPERARIO_DE_PRODUCCION"
        };

        for (String roleName : roles) {
            if (!roleRepository.existsByName(roleName)) {
                RoleEntity role = RoleEntity.builder()
                        .name(roleName)
                        .description(getRoleDescription(roleName))
                        .build();
                roleRepository.save(role);
                log.info("Rol creado: {}", roleName);
            }
        }
    }

    private String getRoleDescription(String roleName) {
        return switch (roleName) {
            case "ADMIN" -> "Administrador del sistema con acceso completo";
            case "GERENTE_GENERAL" -> "Gerente general con acceso ejecutivo";
            case "GERENTE_DE_PLANTA" -> "Gerente de planta con supervisión completa";
            case "SUPERVISOR_DE_CALIDAD" -> "Supervisor del área de calidad";
            case "SUPERVISOR_DE_ALMACEN" -> "Supervisor del área de almacén";
            case "SUPERVISOR_DE_PRODUCCION" -> "Supervisor del área de producción";
            case "OPERARIO_DE_CALIDAD" -> "Operario del área de calidad";
            case "OPERARIO_DE_ALMACEN" -> "Operario del área de almacén";
            case "OPERARIO_DE_PRODUCCION" -> "Operario del área de producción";
            default -> "Rol del sistema";
        };
    }
}