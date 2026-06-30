package com.isaacabarca.devops_dashboard.security;

import com.isaacabarca.devops_dashboard.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIp = getClientIp(request);

        // Rate limit global para toda la API
        try {
            if (!rateLimitService.tryConsume("global:" + clientIp)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("""
                    {
                        "status": 429,
                        "message": "Demasiadas peticiones. Espera un minuto.",
                        "timestamp": "%s"
                    }
                    """.formatted(java.time.LocalDateTime.now()));
                return;
            }
        } catch (Exception e) {
            // Redis no disponible, permitir
        }

        // Rate limit login
        if (request.getRequestURI().equals("/auth/login") && request.getMethod().equals("POST")) {
            try {
                if (!rateLimitService.tryConsume("login:" + clientIp)) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json");
                    response.getWriter().write("""
                        {
                            "status": 429,
                            "message": "Demasiados intentos de login. Espera 1 minuto.",
                            "timestamp": "%s"
                        }
                        """.formatted(java.time.LocalDateTime.now()));
                    return;
                }
            } catch (Exception e) {
                // Redis no disponible, permitir
            }
        }

        // Rate limit verificación
        if (request.getRequestURI().equals("/auth/verify") && request.getMethod().equals("GET")) {
            try {
                if (!rateLimitService.tryConsume("verify:" + clientIp)) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json");
                    response.getWriter().write("""
                        {
                            "status": 429,
                            "message": "Demasiadas verificaciones. Espera 1 minuto.",
                            "timestamp": "%s"
                        }
                        """.formatted(java.time.LocalDateTime.now()));
                    return;
                }
            } catch (Exception e) {
                // Redis no disponible, permitir
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}