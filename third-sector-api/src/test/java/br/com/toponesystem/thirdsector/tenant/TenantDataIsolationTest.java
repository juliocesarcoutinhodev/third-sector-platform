package br.com.toponesystem.thirdsector.tenant;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.PlanFixtures;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityCommand;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityUseCase;
import br.com.toponesystem.thirdsector.tenant.adapter.out.migration.TenantMigrationService;
import br.com.toponesystem.thirdsector.tenant.adapter.out.persistence.IsolationRecordRepository;
import br.com.toponesystem.thirdsector.tenant.domain.TenantContext;
import br.com.toponesystem.thirdsector.tenant.domain.model.IsolationRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TenantDataIsolationTest extends AbstractIntegrationTest {

    private static final String TENANT_A = "isolation-a";
    private static final String TENANT_B = "isolation-b";
    private static final String CNPJ_A = "22222222000191";
    private static final String CNPJ_B = "33333333000191";

    @Autowired
    private RegisterMunicipalityUseCase registerUseCase;

    @Autowired
    private TenantMigrationService migrationService;

    @Autowired
    private IsolationRecordRepository repository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private PlanFixtures planFixtures;

    @BeforeAll
    void setUpTenants() {
        var planId = planFixtures.enterprisePlanId();
        registerUseCase.execute(new RegisterMunicipalityCommand(
                "Alpha Municipality", CNPJ_A, TENANT_A, planId, null));
        registerUseCase.execute(new RegisterMunicipalityCommand(
                "Beta Municipality", CNPJ_B, TENANT_B, planId, null));

        migrationService.migrate(TENANT_A);
        migrationService.migrate(TENANT_B);

        insertRecord(TENANT_A, "alpha-record");
        insertRecord(TENANT_B, "beta-record");
    }

    @AfterAll
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void tenantASeesOnlyItsOwnData() {
        TenantContext.setCurrentTenant(TENANT_A);
        try {
            List<String> data = transactionTemplate.execute(status ->
                    repository.findAll().stream().map(IsolationRecord::getData).toList()
            );

            assertThat(data).containsExactly("alpha-record");
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void tenantBSeesOnlyItsOwnData() {
        TenantContext.setCurrentTenant(TENANT_B);
        try {
            List<String> data = transactionTemplate.execute(status ->
                    repository.findAll().stream().map(IsolationRecord::getData).toList()
            );

            assertThat(data).containsExactly("beta-record");
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void tenantANeverSeesTenantBData() {
        TenantContext.setCurrentTenant(TENANT_A);
        try {
            List<String> data = transactionTemplate.execute(status ->
                    repository.findAll().stream().map(IsolationRecord::getData).toList()
            );

            assertThat(data).doesNotContain("beta-record");
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void failsWhenNoTenantIsSetBecauseTableOnlyExistsInTenantSchemas() {
        assertThatThrownBy(() ->
                transactionTemplate.executeWithoutResult(status -> repository.findAll())
        ).isInstanceOf(RuntimeException.class);
    }

    private void insertRecord(String tenantId, String recordData) {
        TenantContext.setCurrentTenant(tenantId);
        try {
            transactionTemplate.executeWithoutResult(status ->
                    repository.save(new IsolationRecord(recordData))
            );
        } finally {
            TenantContext.clear();
        }
    }
}
