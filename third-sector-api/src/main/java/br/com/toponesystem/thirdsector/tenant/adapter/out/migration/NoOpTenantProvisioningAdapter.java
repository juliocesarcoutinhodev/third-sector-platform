package br.com.toponesystem.thirdsector.tenant.adapter.out.migration;

import br.com.toponesystem.thirdsector.municipality.domain.port.out.TenantProvisioningPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("test")
class NoOpTenantProvisioningAdapter implements TenantProvisioningPort {

    @Override
    public void provision(String subdomain) {
        log.info("Test profile — skipping tenant provisioning for '{}'", subdomain);
    }
}
