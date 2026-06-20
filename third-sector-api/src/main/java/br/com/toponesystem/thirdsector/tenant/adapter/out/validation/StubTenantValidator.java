package br.com.toponesystem.thirdsector.tenant.adapter.out.validation;

import br.com.toponesystem.thirdsector.tenant.config.TenantProperties;
import br.com.toponesystem.thirdsector.tenant.domain.port.out.TenantValidator;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Primary
@Profile("test")
class StubTenantValidator implements TenantValidator {

    private final Set<String> knownTenants;

    StubTenantValidator(TenantProperties properties) {
        this.knownTenants = Set.copyOf(properties.getKnownTenants());
    }

    @Override
    public boolean isActive(String tenantId) {
        return knownTenants.contains(tenantId);
    }
}
