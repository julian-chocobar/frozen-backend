package com.enigcode.frozen_backend.notifications.controller;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.notifications.dto.NotificationResponseDTO;
import com.enigcode.frozen_backend.notifications.dto.NotificationStatsDTO;
import com.enigcode.frozen_backend.notifications.service.NotificationService;
import com.enigcode.frozen_backend.notifications.service.SseNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.time.OffsetDateTime;
import com.enigcode.frozen_backend.users.service.UserService;
import com.enigcode.frozen_backend.users.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.Collections;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SseNotificationService sseNotificationService;

    private NotificationResponseDTO dto;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationController controller;
    @BeforeEach
    void setUp() {
        dto = new NotificationResponseDTO();
        dto.setId(10L);
        User u = User.builder().id(7L).username("user7").password("x").name("Test").creationDate(OffsetDateTime.now()).build();
        Mockito.lenient().when(userService.getCurrentUser()).thenReturn(u);
        // Set an authenticated principal in SecurityContext for SSE endpoint tests
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("userA", "", Collections.emptyList()));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void getNotifications_returnsPage() throws Exception {
        when(notificationService.getUserNotifications(eq(7L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/notifications")
                .param("userId", "7")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.notifications", hasSize(1)));

        verify(notificationService).getUserNotifications(eq(7L), any(Pageable.class));
    }

    @Test
    void getNotifications_unreadOnly_returnsUnread() throws Exception {
        when(notificationService.getUserUnreadNotifications(eq(7L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/notifications")
                .param("unreadOnly", "true")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications", hasSize(1)));

        verify(notificationService).getUserUnreadNotifications(eq(7L), any(Pageable.class));
    }

    @Test
    void markAsRead_success() throws Exception {
        when(notificationService.markAsRead(5L, 7L)).thenReturn(dto);

        mockMvc.perform(patch("/notifications/5/read")
                .param("userId", "7")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(notificationService).markAsRead(5L, 7L);
    }

    @Test
    void markAsRead_wrongUser_returns400() throws Exception {
        when(notificationService.markAsRead(5L, 7L)).thenThrow(new BadRequestException("Wrong user"));

        mockMvc.perform(patch("/notifications/5/read")
                .param("userId", "7")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(notificationService).markAsRead(5L, 7L);
    }

    @Test
    void markAsRead_notFound_returns404() throws Exception {
        when(notificationService.markAsRead(99L, 7L)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(patch("/notifications/99/read")
                .param("userId", "7")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(notificationService).markAsRead(99L, 7L);
    }

    @Test
    void markAllAsRead_success() throws Exception {
        doNothing().when(notificationService).markAllAsRead(7L);

        mockMvc.perform(patch("/notifications/read-all")
                .param("userId", "7")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService).markAllAsRead(7L);
    }

    @Test
    void getStats_returnsCounts() throws Exception {
        NotificationStatsDTO stats = new NotificationStatsDTO();
        stats.setUnreadCount(2L);
        stats.setTotalCount(5L);
        when(notificationService.getUserNotificationStats(7L)).thenReturn(stats);

        mockMvc.perform(get("/notifications/stats")
                .param("userId", "7")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(2))
                .andExpect(jsonPath("$.totalCount").value(5));

        verify(notificationService).getUserNotificationStats(7L);
    }

    @Test
    void stream_callsSseService_andReturnsOk() throws Exception {
        when(sseNotificationService.createConnectionByUsername("userA")).thenReturn(new org.springframework.web.servlet.mvc.method.annotation.SseEmitter());

        mockMvc.perform(get("/notifications/stream")
                .param("username", "userA"))
            .andExpect(status().isOk());

        verify(sseNotificationService).createConnectionByUsername("userA");
    }

    @Test
    void getConnections_returnsInfo() throws Exception {
        when(sseNotificationService.getConnectionsPerUser()).thenReturn(java.util.Map.of(7L, 2));
        when(sseNotificationService.getTotalConnections()).thenReturn(5);
        when(sseNotificationService.getConnectedUsersCount()).thenReturn(3);

        mockMvc.perform(get("/notifications/connections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userConnections").value(2))
                .andExpect(jsonPath("$.totalSystemConnections").value(5))
                .andExpect(jsonPath("$.totalUsers").value(3));
    }

    @Test
    void testConnectivity_returnsInfo() throws Exception {
        when(sseNotificationService.getActiveConnectionsCount(7L)).thenReturn(2);

        mockMvc.perform(get("/notifications/test")
                .header("Origin", "http://example.com")
                .header("User-Agent", "JUnit-Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.sseConnections").value(2));
    }
}
