package com.enigcode.frozen_backend.users.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.model.Role;
import com.enigcode.frozen_backend.users.DTO.*;
import com.enigcode.frozen_backend.users.mapper.UserMapper;
import com.enigcode.frozen_backend.users.repository.UserRepository;
import com.enigcode.frozen_backend.notifications.service.SseNotificationService;

import org.springframework.context.annotation.Lazy;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    // removed RoleRepository, using enum Role directly
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private final SessionRegistry sessionRegistry;
    @Lazy
    private final SseNotificationService sseNotificationService;

    /*
     * @Autowired
     * private WebSocketNotificationService webSocketNotificationService;
     */

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado o ha sido eliminado"));

            // Registrar usuario en cache SSE para evitar queries desde endpoints SSE
            try {
                sseNotificationService.registerUserInCache(username, user.getId());
            } catch (Exception e) {
                // Si el cache falla, solo log pero no bloquear la autenticación
                System.out.println("Warning: No se pudo registrar usuario en SSE cache: " + e.getMessage());
            }

            return user;
        } catch (Exception e) {
            // Cualquier excepción se convierte en UsernameNotFoundException para que Spring
            // Security la maneje
            throw new UsernameNotFoundException("Error al cargar el usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public UserDetailDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        return userMapper.toUserDetailDTO(user);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    @Override
    public UserDetailDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + id));
        return userMapper.toUserDetailDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(UserCreateDTO userCreateDTO) {
        User user = userMapper.toEntity(userCreateDTO);
        user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
        user.setCreationDate(OffsetDateTime.now(ZoneOffset.UTC));
        user.setIsActive(Boolean.TRUE);

        // Convertir nombres de roles a enum Role con validación
        Set<Role> roleEnums = userCreateDTO.getRoles().stream()
                .map(String::trim)
                .map(Role::valueOf)
                .collect(java.util.stream.Collectors.toSet());
        user.setRoles(roleEnums);

        userRepository.save(user);
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDTO toggleActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + id));
        if (user.getIsActive()) {
            // Al desactivar, también deshabilitar el usuario para Spring Security
            user.setEnabled(false);

            // Invalidar sus sesiones activas
            SessionRegistry sessionRegistry = this.sessionRegistry;
            for (Object principal : sessionRegistry.getAllPrincipals()) {
                if (principal instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) principal;
                    if (userDetails.getUsername().equals(user.getUsername())) {
                        for (SessionInformation sessionInfo : sessionRegistry.getAllSessions(principal, false)) {
                            sessionInfo.expireNow();
                        }
                    }
                }
            }
        } else {
            // Al activar, también habilitar el usuario para Spring Security
            user.setEnabled(true);
        }
        user.toggleActive();
        userRepository.save(user);
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User user = userMapper.partialUpdate(userUpdateDTO,
                userRepository.findById(id)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + id)));
        userRepository.save(user);
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserRole(Long id, UpdateRoleDTO updateRolDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + id));

        // Convertir nombres de roles a enum Role
        Set<Role> roleEnums = updateRolDTO.getRoles().stream()
                .map(String::trim)
                .map(Role::valueOf)
                .collect(java.util.stream.Collectors.toSet());
        user = userMapper.updateUserRoles(roleEnums, user);
        userRepository.save(user);
        return userMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDTO updateUserPassword(Long id, UpdatePasswordDTO updatePasswordDTO) {
        User user = userMapper.updateUserPassword(updatePasswordDTO.getPassword(),
                userRepository.findById(id)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + id)));
        user.setPassword(passwordEncoder.encode(updatePasswordDTO.getPassword()));
        userRepository.save(user);
        return userMapper.toResponseDto(user);
    }

    @Override
    public Page<UserResponseDTO> findAll(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toResponseDto);
    }

}
