package br.com.toponesystem.thirdsector.tenant.adapter.out.migration;

import br.com.toponesystem.thirdsector.municipality.domain.port.out.TenantProvisioningPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
class TenantProvisioningAdapter implements TenantProvisioningPort {

    private final TenantMigrationService migrationService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void provision(String subdomain) {
        log.info("Provisioning tenant schema for '{}'", subdomain);
        migrationService.migrate(subdomain);
        log.info("Tenant schema for '{}' provisioned successfully", subdomain);
    }
}
