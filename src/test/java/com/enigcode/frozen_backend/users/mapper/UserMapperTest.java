package com.enigcode.frozen_backend.users.mapper;

import com.enigcode.frozen_backend.users.model.RoleEntity;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.DTO.UserDetailDTO;
import com.enigcode.frozen_backend.users.DTO.UserResponseDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toResponseDto_mapsRolesToStrings() {
        RoleEntity admin = RoleEntity.builder().id(1L).name("ADMIN").build();
        RoleEntity operario = RoleEntity.builder().id(2L).name("OPERARIO_DE_ALMACEN").build();
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(admin);
        roles.add(operario);

        User user = User.builder()
                .id(10L)
                .username("john")
                .password("x")
                .name("John")
                .roles(roles)
                .creationDate(OffsetDateTime.now())
                .build();

        UserResponseDTO dto = mapper.toResponseDto(user);
        assertThat(dto.getRoles()).containsExactlyInAnyOrder("ADMIN", "OPERARIO_DE_ALMACEN");
    }

    @Test
    void toUserDetailDTO_mapsRolesToStrings() {
        RoleEntity qa = RoleEntity.builder().id(3L).name("OPERARIO_DE_CALIDAD").build();
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(qa);

        User user = User.builder()
                .id(11L)
                .username("mary")
                .password("x")
                .name("Mary")
                .email("mary@test.com")
                .roles(roles)
                .creationDate(OffsetDateTime.now())
                .build();

        UserDetailDTO dto = mapper.toUserDetailDTO(user);
        assertThat(dto.getRoles()).containsExactly("OPERARIO_DE_CALIDAD");
    }

    @Test
    void updateUserRoles_replacesRoles() {
        RoleEntity r1 = RoleEntity.builder().id(1L).name("ADMIN").build();
        RoleEntity r2 = RoleEntity.builder().id(2L).name("SUPERVISOR_DE_ALMACEN").build();
        Set<RoleEntity> newRoles = new HashSet<>();
        newRoles.add(r1);
        newRoles.add(r2);

        User user = User.builder()
                .id(12L)
                .username("jane")
                .password("x")
                .name("Jane")
                .roles(new HashSet<>())
                .creationDate(OffsetDateTime.now())
                .build();

        mapper.updateUserRoles(newRoles, user);
        assertThat(user.getRoles()).containsExactlyInAnyOrder(r1, r2);

        UserResponseDTO dto = mapper.toResponseDto(user);
        assertThat(dto.getRoles()).containsExactlyInAnyOrder("ADMIN", "SUPERVISOR_DE_ALMACEN");
    }

    @Test
    void updateUserPassword_setsPassword() {
        User user = User.builder()
                .id(13L)
                .username("tim")
                .password("old")
                .name("Tim")
                .roles(new HashSet<>())
                .creationDate(OffsetDateTime.now())
                .build();

        mapper.updateUserPassword("newRaw", user);
        assertThat(user.getPassword()).isEqualTo("newRaw");
    }
}
