package br.com.toponesystem.thirdsector.tenant.adapter.out.migration;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
@RequiredArgsConstructor
class MasterMigrationService {

    private static final String MASTER_SCHEMA = "master";
    private static final String MASTER_MIGRATION_LOCATION = "classpath:db/migration/master";

    private final DataSource dataSource;

    void migrate() {
        Flyway.configure()
                .dataSource(dataSource)
                .defaultSchema(MASTER_SCHEMA)
                .schemas(MASTER_SCHEMA)
                .locations(MASTER_MIGRATION_LOCATION)
                .createSchemas(true)
                .load()
                .migrate();
    }
}
