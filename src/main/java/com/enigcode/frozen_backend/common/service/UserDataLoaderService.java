package com.enigcode.frozen_backend.common.service;

import com.enigcode.frozen_backend.users.DTO.UserCreateDTO;
import com.enigcode.frozen_backend.users.DTO.UserResponseDTO;
import com.enigcode.frozen_backend.users.repository.UserRepository;
import com.enigcode.frozen_backend.users.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDataLoaderService {

    private final UserRepository userRepository;
    private final UserService userService;

    @Getter
    private Long supervisorAlmacenId;
    @Getter
    private Long supervisorCalidadId;
    @Getter
    private Long superUserId;

    public void loadUsers() {
        if (userRepository.count() == 0) {
            log.info("Cargando usuarios de ejemplo...");

            // Admin
            UserCreateDTO admin = UserCreateDTO.builder()
                    .username("admin")
                    .password("EnigCode123")
                    .name("Administrador")
                    .email("admin@frozen.com")
                    .phoneNumber("1234567890")
                    .roles(Set.of("ADMIN"))
                    .build();
            userService.createUser(admin);

            // Gerente General
            UserCreateDTO gerenteGeneral = UserCreateDTO.builder()
                    .username("gerentegeneral")
                    .password("EnigCode123")
                    .name("Laura Gerente")
                    .email("gerentegeneral@frozen.com")
                    .phoneNumber("1234567894")
                    .roles(Set.of("GERENTE_GENERAL"))
                    .build();
            userService.createUser(gerenteGeneral);

            // Gerente de Planta
            UserCreateDTO gerentePlanta = UserCreateDTO.builder()
                    .username("gerenteplanta")
                    .password("EnigCode123")
                    .name("Carlos Planta")
                    .email("gerenteplanta@frozen.com")
                    .phoneNumber("1234567895")
                    .roles(Set.of("GERENTE_DE_PLANTA"))
                    .build();
            userService.createUser(gerentePlanta);

            // Supervisor de Producción
            UserCreateDTO supervisorProduccion = UserCreateDTO.builder()
                    .username("supervisorproduccion")
                    .password("EnigCode123")
                    .name("Ana Producción")
                    .email("supervisorproduccion@frozen.com")
                    .phoneNumber("1234567896")
                    .roles(Set.of("SUPERVISOR_DE_PRODUCCION"))
                    .build();
            userService.createUser(supervisorProduccion);

            // Supervisor de Calidad
            UserCreateDTO supervisorCalidad = UserCreateDTO.builder()
                    .username("supervisorcalidad")
                    .password("EnigCode123")
                    .name("Miguel Calidad")
                    .email("supervisorcalidad@frozen.com")
                    .phoneNumber("1234567897")
                    .roles(Set.of("SUPERVISOR_DE_CALIDAD"))
                    .build();
            UserResponseDTO supervisorCalidadResponse = userService.createUser(supervisorCalidad);
            supervisorCalidadId = supervisorCalidadResponse.getId();

            // Supervisor de Almacén
            UserCreateDTO supervisorAlmacen = UserCreateDTO.builder()
                    .username("supervisoralmacen")
                    .password("EnigCode123")
                    .name("Juan Supervisor Almacen")
                    .email("supervisoralmacen@frozen.com")
                    .phoneNumber("1234567891")
                    .roles(Set.of("SUPERVISOR_DE_ALMACEN"))
                    .build();
            UserResponseDTO supervisorAlmacenResponse = userService.createUser(supervisorAlmacen);
            supervisorAlmacenId = supervisorAlmacenResponse.getId();

            // Operario de Producción
            UserCreateDTO operarioProduccion = UserCreateDTO.builder()
                    .username("operarioproduccion")
                    .password("EnigCode123")
                    .name("Luis Operario")
                    .email("operarioproduccion@frozen.com")
                    .phoneNumber("1234567896")
                    .roles(Set.of("OPERARIO_DE_PRODUCCION"))
                    .build();
            userService.createUser(operarioProduccion);

            // Operario de Calidad
            UserCreateDTO operarioCalidad = UserCreateDTO.builder()
                    .username("operariocalidad")
                    .password("EnigCode123")
                    .name("Sofía Operaria")
                    .email("operariocalidad@frozen.com")
                    .phoneNumber("1234567897")
                    .roles(Set.of("OPERARIO_DE_CALIDAD"))
                    .build();
            userService.createUser(operarioCalidad);

            // Operario de Almacén
            UserCreateDTO operarioAlmacen = UserCreateDTO.builder()
                    .username("operarioalmacen")
                    .password("EnigCode123")
                    .name("Pedro Operario")
                    .email("operarioalmacen@frozen.com")
                    .phoneNumber("1234567892")
                    .roles(Set.of("OPERARIO_DE_ALMACEN"))
                    .build();
            userService.createUser(operarioAlmacen);

            // Super Usuario
            UserCreateDTO superUser = UserCreateDTO.builder()
                    .username("super")
                    .password("EnigCode123")
                    .name("Super Usuario")
                    .email("super@frozen.com")
                    .phoneNumber("1234567890")
                    .roles(Set.of("ADMIN", "GERENTE_GENERAL", "GERENTE_DE_PLANTA",
                            "SUPERVISOR_DE_PRODUCCION", "SUPERVISOR_DE_CALIDAD",
                            "SUPERVISOR_DE_ALMACEN", "OPERARIO_DE_PRODUCCION",
                            "OPERARIO_DE_CALIDAD", "OPERARIO_DE_ALMACEN"))
                    .build();
            UserResponseDTO superUserResponse = userService.createUser(superUser);
            superUserId = superUserResponse.getId();

            log.info("Usuarios cargados.");
        }
    }
}

