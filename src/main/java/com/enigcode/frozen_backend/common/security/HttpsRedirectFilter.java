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

        // En local o si está deshabilitado: no redirigir
        if (!securityProperties.getEnableHttpsRedirect() || isLocalEnvironment(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        // Respeta proxies (X-Forwarded-Proto/Forwarded)
        String forwardedProto = httpRequest.getHeader("X-Forwarded-Proto");
        boolean alreadyHttps = "https".equalsIgnoreCase(forwardedProto) || httpRequest.isSecure();

        if (!alreadyHttps) {
            StringBuilder httpsUrl = new StringBuilder();
            httpsUrl.append("https://").append(httpRequest.getServerName());
            // Normalmente no necesitas puerto explícito en 443
            httpsUrl.append(httpRequest.getRequestURI());
            if (httpRequest.getQueryString() != null) {
                httpsUrl.append("?").append(httpRequest.getQueryString());
            }
            httpResponse.sendRedirect(httpsUrl.toString());
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isLocalEnvironment(HttpServletRequest request) {
        String host = request.getServerName();
        return "localhost".equalsIgnoreCase(host)
                || "127.0.0.1".equals(host)
                || "::1".equals(host);
    }
}