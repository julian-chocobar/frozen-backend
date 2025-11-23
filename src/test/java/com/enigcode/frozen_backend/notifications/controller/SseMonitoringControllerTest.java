package com.enigcode.frozen_backend.notifications.controller;

import com.enigcode.frozen_backend.notifications.service.SseMonitoringService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SseMonitoringControllerTest {

    @Mock
    private SseMonitoringService monitoringService;

    @Test
    void getSseMetrics_and_getSseHealth_returnOk() {
        Map<String, Object> fakeMetrics = Map.of("totalConnections", 2, "connectedUsers", 1);
        when(monitoringService.getSseMetrics()).thenReturn(fakeMetrics);
        when(monitoringService.isHealthy()).thenReturn(true);

        SseMonitoringController controller = new SseMonitoringController(monitoringService);

        ResponseEntity<Map<String, Object>> metricsResp = controller.getSseMetrics();
        assertThat(metricsResp.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.OK);
        assertThat(metricsResp.getBody()).containsEntry("totalConnections", 2);

        ResponseEntity<Map<String, Object>> healthResp = controller.getSseHealth();
        assertThat(healthResp.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.OK);
        assertThat(healthResp.getBody()).containsEntry("healthy", true);
    }
}
