package com.enigcode.frozen_backend.common.security;

import com.enigcode.frozen_backend.common.SecurityProperties;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class HttpsRedirectFilter implements Filter {

    private final SecurityProperties securityProperties;

    public HttpsRedirectFilter(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Solo redirigir en producción y si está habilitado
        if (securityProperties.getEnableHttpsRedirect() &&
                !httpRequest.isSecure() &&
                !isLocalEnvironment(httpRequest)) {

            String httpsUrl = "https://" + httpRequest.getServerName() +
                    (httpRequest.getServerPort() != 80 ? ":" + 443 : "") +
                    httpRequest.getRequestURI();

            if (httpRequest.getQueryString() != null) {
                httpsUrl += "?" + httpRequest.getQueryString();
            }

            httpResponse.sendRedirect(httpsUrl);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isLocalEnvironment(HttpServletRequest request) {
        return request.getServerName().contains("localhost") ||
                request.getServerName().contains("127.0.0.1");
    }
}