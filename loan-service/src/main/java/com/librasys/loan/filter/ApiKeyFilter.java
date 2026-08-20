package com.librasys.loan.filter;

import com.librasys.loan.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${api.key:loan-service-key-2026}")
    private String configuredApiKey;

    @Autowired(required = false)
    private ApiKeyRepository apiKeyRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        String key = request.getHeader("X-API-KEY");
        if (key == null || (!key.equals(configuredApiKey) && (apiKeyRepository == null || !apiKeyRepository.existsByKey(key)))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing API Key");
            return;
        }
        chain.doFilter(request, response);
    }
}
