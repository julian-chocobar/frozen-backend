package com.enigcode.frozen_backend.notifications.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SseMonitoringServiceTest {

    @Mock
    private SseNotificationService sseNotificationService;

    @InjectMocks
    private SseMonitoringService monitoringService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getSseMetrics_and_isHealthy_normalScenario() {
        when(sseNotificationService.getTotalConnections()).thenReturn(5);
        when(sseNotificationService.getConnectedUsersCount()).thenReturn(3);

        Map<String, Object> metrics = monitoringService.getSseMetrics();

        assertThat(metrics).containsKeys("totalConnections", "connectedUsers", "avgConnectionsPerUser", "loadStatus", "recommendedHikariPoolSize");
        assertThat((Integer) metrics.get("totalConnections")).isEqualTo(5);
        assertThat(monitoringService.isHealthy()).isTrue();
    }

    @Test
    void getSseMetrics_and_isHealthy_overloadScenario() {
        when(sseNotificationService.getTotalConnections()).thenReturn(40);
        when(sseNotificationService.getConnectedUsersCount()).thenReturn(10);

        Map<String, Object> metrics = monitoringService.getSseMetrics();

        assertThat((Integer) metrics.get("totalConnections")).isEqualTo(40);
        assertThat(metrics.get("loadStatus")).isNotNull();
        assertThat(monitoringService.isHealthy()).isFalse();
    }
}
