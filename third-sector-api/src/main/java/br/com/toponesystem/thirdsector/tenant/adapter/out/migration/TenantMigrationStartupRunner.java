package br.com.toponesystem.thirdsector.tenant.adapter.out.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class TenantMigrationStartupRunner implements ApplicationRunner {

    private final MasterMigrationService masterMigrationService;
    private final TenantMigrationService tenantMigrationService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("Applying master schema migrations");
            masterMigrationService.migrate();

            var subdomains = jdbcTemplate.queryForList(
                    "SELECT subdomain FROM master.municipality WHERE active = true",
                    String.class
            );

            log.info("Running tenant migrations for {} active municipality/municipalities", subdomains.size());

            for (var subdomain : subdomains) {
                log.info("Migrating tenant schema: {}", subdomain);
                tenantMigrationService.migrate(subdomain);
            }

            log.info("All schema migrations completed");
        } catch (Exception e) {
            log.error("Migration failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}
