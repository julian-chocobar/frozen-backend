package com.enigcode.frozen_backend.notifications.service;

import org.junit.jupiter.api.Test;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SseNotificationServiceTest {

    private final SseNotificationService svc = new SseNotificationService();

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
    void createConnectionByUsername_cacheMiss_throwsException() {
        // No registrar en cache primero - simula cache miss
        assertThatThrownBy(() -> svc.createConnectionByUsername("userB"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado en cache");
    }

    @Test
    void createConnectionAndRegisterInCache_registersAndCreatesConnection() throws Exception {
        // Usar el método que registra en cache y crea conexión
        SseEmitter emitter = svc.createConnectionAndRegisterInCache("userB", 100L);

        assertThat(emitter).isNotNull();
        assertThat(svc.getActiveConnectionsCount(100L)).isEqualTo(1);
        // Verificar que ahora está en cache y puede crear otra conexión
        SseEmitter emitter2 = svc.createConnectionByUsername("userB");
        assertThat(emitter2).isNotNull();
        assertThat(svc.getActiveConnectionsCount(100L)).isEqualTo(2);
    }
}
