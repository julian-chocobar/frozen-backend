package com.enigcode.frozen_backend.users.service;

import com.enigcode.frozen_backend.users.DTO.*;
import com.enigcode.frozen_backend.users.mapper.UserMapper;
import com.enigcode.frozen_backend.users.model.Role;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

        @Mock
        UserRepository userRepository;

        @Mock
        UserMapper userMapper;

        @Mock
        PasswordEncoder passwordEncoder;

        @Mock
        ApplicationContext applicationContext;

        @Mock
        SessionRegistry sessionRegistry;

        @InjectMocks
        UserServiceImpl service;

    // Helper methods
    private Set<String> rolesToNames(Set<Role> roles) {
            return roles.stream()
                            .map(Role::name)
                            .collect(java.util.stream.Collectors.toSet());
    }

        @BeforeEach
        void setUp() {
            lenient().when(userMapper.toResponseDto(any(User.class))).thenAnswer(inv -> {
                        User u = inv.getArgument(0);
                        return UserResponseDTO.builder()
                                        .id(u.getId())
                                        .username(u.getUsername())
                                        .name(u.getName())
                                    .roles(rolesToNames(u.getRoles()))
                                        .isActive(u.getIsActive())
                                        .build();
                });

            lenient().when(userMapper.toUserDetailDTO(any(User.class))).thenAnswer(inv -> {
                        User u = inv.getArgument(0);
                        return UserDetailDTO.builder()
                                        .id(u.getId())
                                        .username(u.getUsername())
                                        .name(u.getName())
                                    .roles(rolesToNames(u.getRoles()))
                                        .email(u.getEmail())
                                        .phoneNumber(u.getPhoneNumber())
                                        .isActive(u.getIsActive())
                                        .build();
                });
        }

        @Test
        void createUser_success() {
                UserCreateDTO createDTO = UserCreateDTO.builder()
                                .username("testuser")
                                .password("password123")
                                .name("Test User")
                                .roles(Set.of("OPERARIO_DE_ALMACEN"))
                                .email("test@example.com")
                                .build();

                User user = User.builder()
                                .username("testuser")
                                .name("Test User")
                                .email("test@example.com")
                                .build();

                when(userMapper.toEntity(createDTO)).thenReturn(user);
                when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
                when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                        User u = inv.getArgument(0);
                        u.setId(1L);
                        return u;
                });

                UserResponseDTO result = service.createUser(createDTO);

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(userCaptor.capture());

                User savedUser = userCaptor.getValue();
                assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
                assertThat(savedUser.getIsActive()).isTrue();
                assertThat(savedUser.getCreationDate()).isNotNull();
        assertThat(savedUser.getRoles()).contains(Role.OPERARIO_DE_ALMACEN);

                assertThat(result.getUsername()).isEqualTo("testuser");
                assertThat(result.getIsActive()).isTrue();
        }

        @Test
        void createUser_setsDefaultValues() {
                UserCreateDTO createDTO = UserCreateDTO.builder()
                                .username("newuser")
                                .password("pass")
                                .name("New User")
                                .roles(Set.of("SUPERVISOR_DE_CALIDAD"))
                                .build();

                User user = User.builder()
                                .username("newuser")
                                .name("New User")
                                .build();

                when(userMapper.toEntity(createDTO)).thenReturn(user);
                when(passwordEncoder.encode(anyString())).thenReturn("encoded");
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

                service.createUser(createDTO);

                ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(captor.capture());

                User saved = captor.getValue();
                assertThat(saved.getIsActive()).isTrue();
                assertThat(saved.getCreationDate()).isNotNull();
        assertThat(saved.getRoles()).contains(Role.SUPERVISOR_DE_CALIDAD);
        }

        @Test
        void toggleActive_whenActive_disablesAndInvalidatesSessions() {
                User user = User.builder()
                                .id(1L)
                                .username("activeuser")
                                .name("Active User")
                                .roles(Set.of(Role.OPERARIO_DE_PRODUCCION))
                                .isActive(true)
                                .enabled(true)
                                .build();

                when(userRepository.findById(1L)).thenReturn(Optional.of(user));
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

                // Configurar mocks para SessionRegistry
                when(sessionRegistry.getAllPrincipals()).thenReturn(List.of());

                UserResponseDTO result = service.toggleActive(1L);

                verify(userRepository).save(argThat(u -> !u.getIsActive() && !u.isEnabled()));

                assertThat(result.getIsActive()).isFalse();
        }

        @Test
        void toggleActive_whenInactive_enables() {
                User user = User.builder()
                                .id(2L)
                                .username("inactiveuser")
                                .name("Inactive User")
                                .roles(Set.of(Role.OPERARIO_DE_PRODUCCION))
                                .isActive(false)
                                .enabled(false)
                                .build();

                when(userRepository.findById(2L)).thenReturn(Optional.of(user));
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

                UserResponseDTO result = service.toggleActive(2L);

                verify(userRepository).save(argThat(u -> u.getIsActive() && u.isEnabled()));

                assertThat(result.getIsActive()).isTrue();
        }

        @Test
        void updateUser_success() {
                User existingUser = User.builder()
                                .id(1L)
                                .username("user1")
                                .name("Old Name")
                                .roles(Set.of(Role.OPERARIO_DE_PRODUCCION))
                                .email("old@example.com")
                                .build();

                UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                                .name("New Name")
                                .email("new@example.com")
                                .build();

                when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
                when(userMapper.partialUpdate(updateDTO, existingUser)).thenAnswer(inv -> {
                        User u = inv.getArgument(1);
                        u.setName("New Name");
                        u.setEmail("new@example.com");
                        return u;
                });
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

                UserResponseDTO result = service.updateUser(1L, updateDTO);

                verify(userRepository).save(any(User.class));
                assertThat(result.getName()).isEqualTo("New Name");
        }

        @Test
        void updateUser_notFound_throws() {
                UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                                .name("New Name")
                                .build();

                when(userRepository.findById(999L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.updateUser(999L, updateDTO))
                                .isInstanceOf(UsernameNotFoundException.class)
                                .hasMessageContaining("Usuario no encontrado con ID: 999");
        }

        @Test
        void updateUserRole_success() {
                User user = User.builder()
                                .id(1L)
                                .username("user1")
                                .name("User")
                                .roles(Set.of(Role.OPERARIO_DE_ALMACEN))
                                .build();

                UpdateRoleDTO updateRoleDTO = UpdateRoleDTO.builder()
                                .roles(Set.of("ADMIN"))
                                .build();

                when(userRepository.findById(1L)).thenReturn(Optional.of(user));
                when(userMapper.updateUserRoles(Set.of(Role.ADMIN), user)).thenAnswer(inv -> {
                        User u = inv.getArgument(1);
                        u.setRoles(Set.of(Role.ADMIN));
                        return u;
                });
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

                UserResponseDTO result = service.updateUserRole(1L, updateRoleDTO);

                verify(userRepository).save(argThat(u -> u.getRoles().contains(Role.ADMIN)));
                assertThat(result.getRoles()).containsExactly("ADMIN");
        }

        @Test
        void updateUserPassword_success() {
                User user = User.builder()
                                .id(1L)
                                .username("user1")
                                .name("User")
                                .password("oldEncoded")
                                .roles(Set.of(Role.OPERARIO_DE_ALMACEN))
                                .build();

                UpdatePasswordDTO updatePasswordDTO = UpdatePasswordDTO.builder()
                                .password("newPassword123")
                                .passwordConfirmacion("newPassword123")
                                .build();

                when(userRepository.findById(1L)).thenReturn(Optional.of(user));
                when(userMapper.updateUserPassword("newPassword123", user)).thenReturn(user);
                when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

                UserResponseDTO result = service.updateUserPassword(1L, updatePasswordDTO);

                verify(userRepository).save(argThat(u -> u.getPassword().equals("newEncodedPassword")));
                assertThat(result).isNotNull();
        }

        @Test
        void getUserById_success() {
                User user = User.builder()
                                .id(1L)
                                .username("user1")
                                .name("User One")
                                .roles(Set.of(Role.OPERARIO_DE_ALMACEN))
                                .email("user1@example.com")
                                .isActive(true)
                                .build();

                when(userRepository.findById(1L)).thenReturn(Optional.of(user));

                UserDetailDTO result = service.getUserById(1L);

                assertThat(result.getId()).isEqualTo(1L);
                assertThat(result.getUsername()).isEqualTo("user1");
                assertThat(result.getEmail()).isEqualTo("user1@example.com");
        }

        @Test
        void getUserById_notFound_throws() {
                when(userRepository.findById(999L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.getUserById(999L))
                                .isInstanceOf(UsernameNotFoundException.class)
                                .hasMessageContaining("Usuario no encontrado con ID: 999");
        }

        @Test
        void getUserByUsername_success() {
                User user = User.builder()
                                .id(1L)
                                .username("testuser")
                                .name("Test User")
                                .roles(Set.of(Role.OPERARIO_DE_ALMACEN))
                                .build();

                when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

                UserDetailDTO result = service.getUserByUsername("testuser");

                assertThat(result.getUsername()).isEqualTo("testuser");
                assertThat(result.getName()).isEqualTo("Test User");
        }

        @Test
        void findAll_returnsPage() {
                User user1 = User.builder()
                                .id(1L)
                                .username("user1")
                                .name("User One")
                                .roles(Set.of(Role.OPERARIO_DE_ALMACEN))
                                .build();

                User user2 = User.builder()
                                .id(2L)
                                .username("user2")
                                .name("User Two")
                                .roles(Set.of(Role.ADMIN))
                                .build();

                Page<User> page = new PageImpl<>(List.of(user1, user2));
                when(userRepository.findAll(any(PageRequest.class))).thenReturn(page);

                Page<UserResponseDTO> result = service.findAll(PageRequest.of(0, 10));

                assertThat(result.getTotalElements()).isEqualTo(2);
                assertThat(result.getContent()).hasSize(2);
                assertThat(result.getContent().get(0).getUsername()).isEqualTo("user1");
        }
}
