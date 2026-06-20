package br.com.toponesystem.thirdsector.tenant;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.tenant.adapter.out.migration.TenantMigrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class TenantMigrationServiceTest extends AbstractIntegrationTest {

    @Autowired
    private TenantMigrationService migrationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void migratesNewTenantSchemaCreatingItFromScratch() {
        migrationService.migrate("schema_test_alpha");

        var schemas = jdbcTemplate.queryForList(
                "SELECT schema_name FROM information_schema.schemata WHERE schema_name = 'schema_test_alpha'",
                String.class
        );

        assertThat(schemas).containsExactly("schema_test_alpha");
    }

    @Test
    void twoTenantSchemasAreIsolatedFromEachOther() {
        migrationService.migrate("schema_test_beta");
        migrationService.migrate("schema_test_gamma");

        var betaTables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'schema_test_beta'",
                String.class
        );
        var gammaTables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'schema_test_gamma'",
                String.class
        );

        assertThat(betaTables).containsExactlyInAnyOrderElementsOf(gammaTables);
    }

    @Test
    void migrationIsIdempotent() {
        migrationService.migrate("schema_test_delta");

        assertThatCode(() -> migrationService.migrate("schema_test_delta"))
                .doesNotThrowAnyException();
    }
}
