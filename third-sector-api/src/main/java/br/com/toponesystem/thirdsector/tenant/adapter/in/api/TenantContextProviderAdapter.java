package br.com.toponesystem.thirdsector.tenant.adapter.in.api;

import br.com.toponesystem.thirdsector.tenant.application.api.TenantProvider;
import br.com.toponesystem.thirdsector.tenant.domain.TenantContext;
import org.springframework.stereotype.Component;

@Component
class TenantContextProviderAdapter implements TenantProvider {

    @Override
    public String currentTenant() {
        return TenantContext.getCurrentTenant();
    }
}
