package br.com.toponesystem.thirdsector.shared.adapter.in.web;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

public class TenantAuthenticationToken extends AbstractAuthenticationToken {

    private final UUID userId;
    private final String role;
    private final String tenantId;
    private final UUID organizationId;

    public TenantAuthenticationToken(UUID userId, String role, String tenantId, UUID organizationId) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        this.userId = userId;
        this.role = role;
        this.tenantId = tenantId;
        this.organizationId = organizationId;
        setAuthenticated(true);
    }

    private TenantAuthenticationToken(UUID userId, String role, String tenantId) {
        super(List.of(new SimpleGrantedAuthority("ROLE_FORCE_PASSWORD_CHANGE")));
        this.userId = userId;
        this.role = role;
        this.tenantId = tenantId;
        this.organizationId = null;
        setAuthenticated(true);
    }

    public static TenantAuthenticationToken forPasswordChange(UUID userId, String role, String tenantId) {
        return new TenantAuthenticationToken(userId, role, tenantId);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getTenantId() {
        return tenantId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }
}
