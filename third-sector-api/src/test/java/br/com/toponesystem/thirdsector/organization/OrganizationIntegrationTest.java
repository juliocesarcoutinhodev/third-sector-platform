package br.com.toponesystem.thirdsector.organization;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityCommand;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityUseCase;
import br.com.toponesystem.thirdsector.municipality.domain.model.Plan;
import br.com.toponesystem.thirdsector.organization.application.usecase.CreateOrganizationCommand;
import br.com.toponesystem.thirdsector.organization.application.usecase.CreateOrganizationUseCase;
import br.com.toponesystem.thirdsector.organization.domain.exception.DuplicateCnpjException;
import br.com.toponesystem.thirdsector.organization.domain.model.OrganizationStatus;
import br.com.toponesystem.thirdsector.tenant.adapter.out.migration.TenantMigrationService;
import br.com.toponesystem.thirdsector.tenant.domain.TenantContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrganizationIntegrationTest extends AbstractIntegrationTest {

    private static final String TENANT_X = "org-tenant-x-" + System.nanoTime();
    private static final String TENANT_Y = "org-tenant-y-" + System.nanoTime();

    @Autowired
    private RegisterMunicipalityUseCase registerUseCase;

    @Autowired
    private TenantMigrationService migrationService;

    @Autowired
    private CreateOrganizationUseCase createOrganizationUseCase;

    @BeforeAll
    void setUpTenants() {
        registerUseCase.execute(new RegisterMunicipalityCommand(
                "Org Tenant X", validCnpj(System.nanoTime()), TENANT_X, Plan.BASIC, null));
        registerUseCase.execute(new RegisterMunicipalityCommand(
                "Org Tenant Y", validCnpj(System.nanoTime()), TENANT_Y, Plan.BASIC, null));

        migrationService.migrate(TENANT_X);
        migrationService.migrate(TENANT_Y);
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @AfterAll
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void generatesUuidVersion7() {
        TenantContext.setCurrentTenant(TENANT_X);
        try {
            var view = createOrganizationUseCase.execute(new CreateOrganizationCommand("V7 Test Org", validCnpj(System.nanoTime())));

            assertThat(view.id()).isNotNull();
            assertThat(view.id().version()).isEqualTo(7);
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void createsAndFindsOrganizationWithinTenant() {
        TenantContext.setCurrentTenant(TENANT_X);
        try {
            var cnpj = validCnpj(System.nanoTime());
            var view = createOrganizationUseCase.execute(new CreateOrganizationCommand("ACME ONG", cnpj));

            assertThat(view.id()).isNotNull();
            assertThat(view.name()).isEqualTo("ACME ONG");
            assertThat(view.cnpj()).isEqualTo(cnpj);
            assertThat(view.status()).isEqualTo(OrganizationStatus.PENDING);
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void stripsCnpjMaskOnCreation() {
        TenantContext.setCurrentTenant(TENANT_X);
        try {
            var view = createOrganizationUseCase.execute(new CreateOrganizationCommand("Beta ONG", "12.345.678/0001-95"));

            assertThat(view.cnpj()).isEqualTo("12345678000195");
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void rejectsDuplicateCnpjWithinSameTenant() {
        TenantContext.setCurrentTenant(TENANT_X);
        try {
            var cnpj = validCnpj(System.nanoTime());
            createOrganizationUseCase.execute(new CreateOrganizationCommand("Gamma ONG", cnpj));

            assertThatThrownBy(() ->
                    createOrganizationUseCase.execute(new CreateOrganizationCommand("Gamma Duplicate", cnpj))
            ).isInstanceOf(DuplicateCnpjException.class)
                    .hasMessageContaining(cnpj);
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void sameCnpjCanExistInDifferentTenants() {
        var sharedCnpj = validCnpj(System.nanoTime());

        TenantContext.setCurrentTenant(TENANT_X);
        try {
            createOrganizationUseCase.execute(new CreateOrganizationCommand("Delta X", sharedCnpj));
        } finally {
            TenantContext.clear();
        }

        TenantContext.setCurrentTenant(TENANT_Y);
        try {
            var view = createOrganizationUseCase.execute(new CreateOrganizationCommand("Delta Y", sharedCnpj));

            assertThat(view.id()).isNotNull();
            assertThat(view.cnpj()).isEqualTo(sharedCnpj);
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void tenantIsolationPreventsCrossTenantCnpjConflict() {
        TenantContext.setCurrentTenant(TENANT_Y);
        var cnpj = validCnpj(System.nanoTime());
        try {
            createOrganizationUseCase.execute(new CreateOrganizationCommand("Epsilon Y", cnpj));
        } finally {
            TenantContext.clear();
        }

        TenantContext.setCurrentTenant(TENANT_X);
        try {
            var view = createOrganizationUseCase.execute(new CreateOrganizationCommand("Epsilon X", cnpj));

            assertThat(view.id()).isNotNull();
            assertThat(view.cnpj()).isEqualTo(cnpj);
        } finally {
            TenantContext.clear();
        }
    }

    private static String validCnpj(long seed) {
        var root = String.format("%08d0001", Math.abs(seed) % 100_000_000);
        int[] w1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] w2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int s1 = 0, s2 = 0;
        for (int i = 0; i < 12; i++) {
            int d = root.charAt(i) - '0';
            s1 += d * w1[i];
            s2 += d * w2[i];
        }
        int d1 = 11 - (s1 % 11);
        if (d1 >= 10) d1 = 0;
        s2 += d1 * 2;
        int d2 = 11 - (s2 % 11);
        if (d2 >= 10) d2 = 0;
        return root + d1 + d2;
    }
}
