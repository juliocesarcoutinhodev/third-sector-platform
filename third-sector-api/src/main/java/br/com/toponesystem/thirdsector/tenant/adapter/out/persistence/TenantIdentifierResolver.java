package br.com.toponesystem.thirdsector.tenant.adapter.out.persistence;

import br.com.toponesystem.thirdsector.tenant.domain.TenantContext;
import br.com.toponesystem.thirdsector.tenant.domain.TenantContextNotSetException;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    static final String MASTER = "master";

    @Override
    public String resolveCurrentTenantIdentifier() {
        try {
            return TenantContext.getCurrentTenant();
        } catch (TenantContextNotSetException e) {
            return MASTER;
        }
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}
