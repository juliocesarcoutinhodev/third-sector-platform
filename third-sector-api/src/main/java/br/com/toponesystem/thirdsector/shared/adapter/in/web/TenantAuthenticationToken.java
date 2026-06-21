package br.com.toponesystem.thirdsector.shared.adapter.in.web;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

public class TenantAuthenticationToken extends AbstractAuthenticationToken {

    private final Long userId;
    private final String role;
    private final String tenantId;
    private final Long organizationId;

    public TenantAuthenticationToken(Long userId, String role, String tenantId, Long organizationId) {
        super(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
        this.userId = userId;
        this.role = role;
        this.tenantId = tenantId;
        this.organizationId = organizationId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }
}
