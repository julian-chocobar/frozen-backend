package com.enigcode.frozen_backend.users.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import org.mapstruct.MappingConstants;

import com.enigcode.frozen_backend.common.mapper.GlobalMapperConfig;
import com.enigcode.frozen_backend.users.DTO.UserDetailDTO;
import com.enigcode.frozen_backend.users.DTO.UserResponseDTO;
import com.enigcode.frozen_backend.users.DTO.UserUpdateDTO;
import com.enigcode.frozen_backend.users.DTO.UserCreateDTO;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.model.Role;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponseDTO toResponseDto(User user);

    UserDetailDTO toUserDetailDTO(User user);

    User toEntity(UserCreateDTO dto);

    User partialUpdate(UserUpdateDTO dto, @MappingTarget User user);

    User updateUserRole(Role role, @MappingTarget User user);

    User updateUserPassword(String password, @MappingTarget User user);

}
