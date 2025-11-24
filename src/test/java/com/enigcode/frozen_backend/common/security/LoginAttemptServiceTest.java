package com.enigcode.frozen_backend.common.security;

import com.enigcode.frozen_backend.common.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;
    private SecurityProperties securityProperties;

    @BeforeEach
    void setUp() {
        securityProperties = new SecurityProperties();
        securityProperties.setMaxLoginAttempts(3);
        securityProperties.setLoginTimeoutMinutes(15);
        loginAttemptService = new LoginAttemptService(securityProperties);
    }

    @Test
    void newUser_hasMaxAttemptsAvailable() {
        String username = "alice";

        int remaining = loginAttemptService.getRemainingAttempts(username);

        assertThat(remaining).isEqualTo(3);
    }

    @Test
    void loginFailed_decreasesRemainingAttempts() {
        String username = "bob";

        loginAttemptService.loginFailed(username);

        int remaining = loginAttemptService.getRemainingAttempts(username);
        assertThat(remaining).isEqualTo(2);
    }

    @Test
    void loginFailed_afterMaxAttempts_blocksUser() {
        String username = "charlie";

        loginAttemptService.loginFailed(username);
        loginAttemptService.loginFailed(username);
        loginAttemptService.loginFailed(username);

        boolean blocked = loginAttemptService.isBlocked(username);
        assertThat(blocked).isTrue();
        assertThat(loginAttemptService.getRemainingAttempts(username)).isEqualTo(0);
    }

    @Test
    void isBlocked_returnsFalse_whenUserNotBlocked() {
        String username = "dave";

        loginAttemptService.loginFailed(username);

        boolean blocked = loginAttemptService.isBlocked(username);
        assertThat(blocked).isFalse();
    }

    @Test
    void loginSuccess_resetsAttempts() {
        String username = "eve";

        loginAttemptService.loginFailed(username);
        loginAttemptService.loginFailed(username);
        assertThat(loginAttemptService.getRemainingAttempts(username)).isEqualTo(1);

        loginAttemptService.loginSuccess(username);

        assertThat(loginAttemptService.getRemainingAttempts(username)).isEqualTo(3);
        assertThat(loginAttemptService.isBlocked(username)).isFalse();
    }

    @Test
    void getBlockedMessage_whenBlocked_returnsMessageWithMinutes() {
        String username = "frank";

        loginAttemptService.loginFailed(username);
        loginAttemptService.loginFailed(username);
        loginAttemptService.loginFailed(username);

        String message = loginAttemptService.getBlockedMessage(username);

        assertThat(message).contains("Demasiados intentos fallidos");
        assertThat(message).contains("minutos");
    }

    @Test
    void getBlockedMessage_whenNotBlocked_returnsDefaultMessage() {
        String username = "grace";

        loginAttemptService.loginFailed(username);

        String message = loginAttemptService.getBlockedMessage(username);

        assertThat(message).isEqualTo("Credenciales incorrectas.");
    }

    @Test
    void multipleUsers_maintainSeparateAttempts() {
        String user1 = "harry";
        String user2 = "iris";

        loginAttemptService.loginFailed(user1);
        loginAttemptService.loginFailed(user1);

        loginAttemptService.loginFailed(user2);

        assertThat(loginAttemptService.getRemainingAttempts(user1)).isEqualTo(1);
        assertThat(loginAttemptService.getRemainingAttempts(user2)).isEqualTo(2);
        assertThat(loginAttemptService.isBlocked(user1)).isFalse();
        assertThat(loginAttemptService.isBlocked(user2)).isFalse();
    }

    @Test
    void loginSuccess_onlyAffectsSpecificUser() {
        String user1 = "jack";
        String user2 = "kelly";

        loginAttemptService.loginFailed(user1);
        loginAttemptService.loginFailed(user2);
        loginAttemptService.loginFailed(user2);

        loginAttemptService.loginSuccess(user1);

        assertThat(loginAttemptService.getRemainingAttempts(user1)).isEqualTo(3);
        assertThat(loginAttemptService.getRemainingAttempts(user2)).isEqualTo(1);
    }
}
