package com.spotify.analytics.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class TenantAuthenticationFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-KEY";
    public static final String TENANT_HEADER = "X-TENANT-ID";

    private static final Map<String, String> API_KEY_TO_TENANT = Map.of(
            "key-tenant-a-secret", "tenant-a",
            "key-tenant-b-secret", "tenant-b",
            "key-demo-spotify", "spotify-global"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String apiKey = request.getHeader(API_KEY_HEADER);
            String tenantId = null;

            if (apiKey != null && API_KEY_TO_TENANT.containsKey(apiKey)) {
                tenantId = API_KEY_TO_TENANT.get(apiKey);
            } else {
                String explicitHeader = request.getHeader(TENANT_HEADER);
                if (explicitHeader != null && !explicitHeader.trim().isEmpty()) {
                    tenantId = explicitHeader.trim();
                } else {
                    tenantId = "tenant-a";
                }
            }

            TenantContext.setTenantId(tenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
