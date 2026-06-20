package br.com.toponesystem.thirdsector.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.tenant.domain.TenantContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantSchemaRoutingTest extends AbstractIntegrationTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeAll
    static void createTenantSchemas(@Autowired DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS maringa");
            statement.execute("CREATE SCHEMA IF NOT EXISTS londrina");
        }
    }

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void queryExecutesInTheResolvedTenantSchema() {
        TenantContext.setCurrentTenant("maringa");

        String schema = transactionTemplate.execute(status ->
                (String) entityManager.createNativeQuery("SELECT current_schema()").getSingleResult()
        );

        assertThat(schema).isEqualTo("maringa");
    }

    @Test
    void sequentialContextSwitchUsesCorrectSchemaEachTime() {
        TenantContext.setCurrentTenant("maringa");
        String schemaA = transactionTemplate.execute(status ->
                (String) entityManager.createNativeQuery("SELECT current_schema()").getSingleResult()
        );
        TenantContext.clear();

        TenantContext.setCurrentTenant("londrina");
        String schemaB = transactionTemplate.execute(status ->
                (String) entityManager.createNativeQuery("SELECT current_schema()").getSingleResult()
        );

        assertThat(schemaA).isEqualTo("maringa");
        assertThat(schemaB).isEqualTo("londrina");
    }

    @Test
    void withoutTenantContextSchemaDefaultsToMaster() {
        // No tenant set — system/admin operations default to the master schema.
        String schema = transactionTemplate.execute(status ->
                (String) entityManager.createNativeQuery("SELECT current_schema()").getSingleResult()
        );

        assertThat(schema).isEqualTo("master");
    }
}
