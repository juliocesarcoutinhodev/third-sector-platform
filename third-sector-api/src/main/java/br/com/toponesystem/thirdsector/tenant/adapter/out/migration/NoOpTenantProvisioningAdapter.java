package br.com.toponesystem.thirdsector.tenant.adapter.out.migration;

import br.com.toponesystem.thirdsector.municipality.domain.port.out.TenantProvisioningPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("test")
@RequiredArgsConstructor
class NoOpTenantProvisioningAdapter implements TenantProvisioningPort {

    private final TenantMigrationService migrationService;

    @Override
    public void provision(String subdomain) {
        log.info("Test profile — provisioning tenant schema for '{}'", subdomain);
        migrationService.migrate(subdomain);
    }
}
