package br.com.toponesystem.thirdsector.shared.adapter.in.web;

import br.com.toponesystem.thirdsector.tenant.application.api.TenantProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("scope")
public class ScopeAuthorization {

    private final TenantProvider tenantProvider;

    public ScopeAuthorization(TenantProvider tenantProvider) {
        this.tenantProvider = tenantProvider;
    }

    public boolean isSameTenant(String tenantId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof TenantAuthenticationToken token) {
            if ("SUPER_ADMIN".equals(token.getRole())) {
                return true;
            }
            return tenantId.equals(token.getTenantId());
        }
        return false;
    }

    public boolean isOrganizationMember(UUID organizationId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof TenantAuthenticationToken token) {
            if ("SUPER_ADMIN".equals(token.getRole())) {
                return true;
            }
            if ("MUNICIPALITY_ADM".equals(token.getRole())) {
                return tenantProvider.currentTenant().equals(token.getTenantId());
            }
            return organizationId != null && organizationId.equals(token.getOrganizationId());
        }
        return false;
    }
}
