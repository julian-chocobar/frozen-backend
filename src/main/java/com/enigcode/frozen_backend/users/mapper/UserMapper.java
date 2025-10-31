package com.enigcode.frozen_backend.users.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.users.DTO.UserDetailDTO;
import com.enigcode.frozen_backend.users.DTO.UserResponseDTO;
import com.enigcode.frozen_backend.users.DTO.UserUpdateDTO;
import com.enigcode.frozen_backend.users.DTO.UserCreateDTO;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.model.Role;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user.getRoles()))")
    UserResponseDTO toResponseDto(User user);

    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user.getRoles()))")
    UserDetailDTO toUserDetailDTO(User user);

    @Mapping(target = "roles", ignore = true)
    User toEntity(UserCreateDTO dto);

    User partialUpdate(UserUpdateDTO dto, @MappingTarget User user);

    default User updateUserRoles(Set<Role> roles, User user) {
        user.setRoles(roles);
        return user;
    }

    User updateUserPassword(String password, @MappingTarget User user);

    // Métodos helper para conversión de roles
    default Set<String> mapRolesToStrings(Set<Role> roles) {
        return roles.stream()
                .map(Role::name)
                .collect(Collectors.toSet());
    }

}
