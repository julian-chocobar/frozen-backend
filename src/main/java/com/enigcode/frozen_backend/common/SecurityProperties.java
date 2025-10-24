package com.enigcode.frozen_backend.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    @NotNull
    @Min(1)
    private Integer maxLoginAttempts = 3;

    @NotNull
    @Min(1)
    private Integer loginTimeoutMinutes = 30;

    @NotNull
    private Boolean enableHttpsRedirect = true;

    // Getters y Setters
    public Integer getMaxLoginAttempts() {
        return maxLoginAttempts;
    }

    public void setMaxLoginAttempts(Integer maxLoginAttempts) {
        this.maxLoginAttempts = maxLoginAttempts;
    }

    public Integer getLoginTimeoutMinutes() {
        return loginTimeoutMinutes;
    }

    public void setLoginTimeoutMinutes(Integer loginTimeoutMinutes) {
        this.loginTimeoutMinutes = loginTimeoutMinutes;
    }

    public Boolean getEnableHttpsRedirect() {
        return enableHttpsRedirect;
    }

    public void setEnableHttpsRedirect(Boolean enableHttpsRedirect) {
        this.enableHttpsRedirect = enableHttpsRedirect;
    }
}