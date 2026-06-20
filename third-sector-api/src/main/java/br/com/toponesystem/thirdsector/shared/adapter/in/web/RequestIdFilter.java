package br.com.toponesystem.thirdsector.shared.adapter.in.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter implements Filter {

    private static final String REQUEST_ID = "requestId";
    private static final String TENANT_ID = "tenantId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            MDC.put(REQUEST_ID, UUID.randomUUID().toString());
            MDC.put(TENANT_ID, resolveTenantId(request));
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String resolveTenantId(ServletRequest request) {
        if (request instanceof HttpServletRequest http) {
            String tenant = http.getHeader("X-Tenant-ID");
            return tenant != null ? tenant : "";
        }
        return "";
    }
}
