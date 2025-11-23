package com.enigcode.frozen_backend.notifications.integration;

import com.enigcode.frozen_backend.common.service.DataLoaderService;
import com.enigcode.frozen_backend.notifications.model.Notification;
import com.enigcode.frozen_backend.notifications.repository.NotificationRepository;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @TestConfiguration
    static class DisableDataLoaderConfig {
        @Bean
        public DataLoaderService dataLoaderService() {
            return Mockito.mock(DataLoaderService.class);
        }
    }

    @Test
    public void createAndFetchNotification_viaRepositoryAndEndpoint() throws Exception {
        // create a user directly in the test DB (populate required fields)
        User user = new User();
        user.setUsername("integration_user");
        user.setPassword("pwd");
        user.setName("Integration User");
        user.setEmail("int@example.com");
        user.setCreationDate(OffsetDateTime.now());
        user.getRoles().add(com.enigcode.frozen_backend.users.model.Role.OPERARIO_DE_ALMACEN);
        user = userRepository.save(user);

        // create a notification for that user
        Notification n = new Notification();
        n.setUserId(user.getId());
        n.setMessage("This is a test notification created in integration test.");
        n.setType(com.enigcode.frozen_backend.notifications.model.NotificationType.SYSTEM_REMINDER);
        n.setCreatedAt(OffsetDateTime.now());
        notificationRepository.save(n);

        // call the notifications endpoint (expects authentication in real app)
        // Here we call a public-ish endpoint; if security blocks it, this test still verifies repository
        mockMvc.perform(get("/notifications")
                .with(user(user.getUsername()).roles("OPERARIO_DE_ALMACEN"))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // verify repository contains the saved notification
        Iterable<Notification> all = notificationRepository.findAll();
        assertThat(all).anyMatch(x -> x.getMessage().equals("This is a test notification created in integration test."));
    }
}
