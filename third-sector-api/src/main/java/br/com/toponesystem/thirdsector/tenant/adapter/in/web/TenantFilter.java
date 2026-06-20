package br.com.toponesystem.thirdsector.tenant.adapter.in.web;

import br.com.toponesystem.thirdsector.tenant.TenantProperties;
import br.com.toponesystem.thirdsector.tenant.domain.TenantContext;
import br.com.toponesystem.thirdsector.tenant.domain.port.out.TenantValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
class TenantFilter extends OncePerRequestFilter {

    private static final List<String> BYPASS_PREFIXES = List.of(
            "/actuator", "/swagger-ui", "/v3/api-docs"
    );

    private final TenantProperties properties;
    private final TenantValidator tenantValidator;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        var path = request.getServletPath();
        return BYPASS_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        var tenantId = resolveTenant(request);

        if (tenantId == null || !tenantValidator.isActive(tenantId)) {
            log.debug("Rejected request — could not resolve an active tenant");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        TenantContext.setCurrentTenant(tenantId);
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveTenant(HttpServletRequest request) {
        var subdomain = extractSubdomain(request);
        if (subdomain != null) {
            return subdomain;
        }
        if (properties.isHeaderFallbackEnabled()) {
            return request.getHeader(properties.getFallbackHeader());
        }
        return null;
    }

    private String extractSubdomain(HttpServletRequest request) {
        var baseDomain = properties.getBaseDomain();
        if (baseDomain == null || baseDomain.isBlank()) {
            return null;
        }
        var host = request.getHeader("Host");
        if (host == null) {
            return null;
        }
        var colonIdx = host.indexOf(':');
        if (colonIdx != -1) {
            host = host.substring(0, colonIdx);
        }
        var suffix = "." + baseDomain;
        if (!host.endsWith(suffix)) {
            return null;
        }
        var subdomain = host.substring(0, host.length() - suffix.length());
        return (subdomain.isEmpty() || subdomain.contains(".")) ? null : subdomain;
    }
}
