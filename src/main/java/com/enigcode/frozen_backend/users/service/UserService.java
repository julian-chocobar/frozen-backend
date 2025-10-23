package com.enigcode.frozen_backend.users.service;

import com.enigcode.frozen_backend.users.DTO.UpdatePasswordDTO;
import com.enigcode.frozen_backend.users.DTO.UpdateRoleDTO;
import com.enigcode.frozen_backend.users.DTO.UserCreateDTO;
import com.enigcode.frozen_backend.users.DTO.UserDetailDTO;
import com.enigcode.frozen_backend.users.DTO.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.DTO.UserUpdateDTO;

public interface UserService {

    UserResponseDTO createUser(UserCreateDTO userCreateDTO);

    UserResponseDTO toggleActive(Long id);

    UserResponseDTO updateUser(Long id, UserUpdateDTO userUpdateDTO);

    UserResponseDTO updateUserRole(Long id, UpdateRoleDTO updateRolDTO);

    UserResponseDTO updateUserPassword(Long id, UpdatePasswordDTO updatePasswordDTO);

    UserDetailDTO getUserById(Long id);

    Page<UserResponseDTO> findAll(Pageable pageable);

    User getCurrentUser();

    UserDetailDTO getUserByUsername(String username);

}
