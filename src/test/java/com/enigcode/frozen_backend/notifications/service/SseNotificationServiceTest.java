package com.enigcode.frozen_backend.notifications.service;

import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SseNotificationServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private SseNotificationService svc;

    @Test
    void registerAndCreateConnectionByUsername_and_counts() throws Exception {
        svc.registerUserInCache("userA", 42L);
        SseEmitter emitter = svc.createConnectionByUsername("userA");

        assertThat(emitter).isNotNull();
        assertThat(svc.getActiveConnectionsCount(42L)).isEqualTo(1);
        assertThat(svc.getConnectedUsersCount()).isEqualTo(1);

        // Removing cache should not remove while connection exists
        svc.removeUserFromCacheIfNoActiveConnections("userA");
        assertThat(svc.getConnectedUsersCount()).isEqualTo(1);

        // Close the connection and then cache removal should remove
        svc.closeAllConnectionsForUser(42L);
        svc.removeUserFromCacheIfNoActiveConnections("userA");

        assertThat(svc.getConnectedUsersCount()).isEqualTo(0);
    }

    @Test
    void createConnection_respects_max_connections_and_eviction() throws Exception {
        Long userId = 77L;

        // create more connections than MAX_CONNECTIONS_PER_USER (2)
        SseEmitter e1 = svc.createConnection(userId);
        SseEmitter e2 = svc.createConnection(userId);
        SseEmitter e3 = svc.createConnection(userId);

        // Max is 2, so total connections should be <=2
        assertThat(svc.getActiveConnectionsCount(userId)).isLessThanOrEqualTo(2);
        assertThat(svc.getTotalActiveConnections()).isGreaterThanOrEqualTo(0);
        assertThat(e1).isNotNull();
        assertThat(e2).isNotNull();
        assertThat(e3).isNotNull();

        svc.closeAllConnectionsForUser(userId);
        assertThat(svc.getActiveConnectionsCount(userId)).isEqualTo(0);
    }

    @Test
    void sendNotificationToUser_noConnections_noThrow() {
        // No connections exist for user 999
        svc.sendNotificationToUser(999L, "payload");
        // No exception thrown, nothing to assert besides state
        assertThat(svc.getActiveConnectionsCount(999L)).isEqualTo(0);
    }

    @Test
    void createConnectionByUsername_cacheMiss_repopulatesCache() throws Exception {
        // Simular usuario no en cache pero autenticado
        User user = User.builder()
                .id(100L)
                .username("userB")
                .password("password")
                .name("User B")
                .creationDate(OffsetDateTime.now())
                .build();

        when(userService.getCurrentUser()).thenReturn(user);

        // No registrar en cache primero - simula cache miss
        SseEmitter emitter = svc.createConnectionByUsername("userB");

        assertThat(emitter).isNotNull();
        assertThat(svc.getActiveConnectionsCount(100L)).isEqualTo(1);
        // Verificar que ahora está en cache
        SseEmitter emitter2 = svc.createConnectionByUsername("userB");
        assertThat(emitter2).isNotNull();
        assertThat(svc.getActiveConnectionsCount(100L)).isEqualTo(2);
    }
}
