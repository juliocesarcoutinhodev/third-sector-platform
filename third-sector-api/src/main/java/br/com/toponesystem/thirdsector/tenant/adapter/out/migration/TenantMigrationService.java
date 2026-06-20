package br.com.toponesystem.thirdsector.tenant.adapter.out.migration;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
@RequiredArgsConstructor
public class TenantMigrationService {

    private static final String TENANT_MIGRATION_LOCATION = "classpath:db/migration/tenant";

    private final DataSource dataSource;

    public void migrate(String schemaName) {
        Flyway.configure()
                .dataSource(dataSource)
                .defaultSchema(schemaName)
                .schemas(schemaName)
                .locations(TENANT_MIGRATION_LOCATION)
                .createSchemas(true)
                .load()
                .migrate();
    }
}
