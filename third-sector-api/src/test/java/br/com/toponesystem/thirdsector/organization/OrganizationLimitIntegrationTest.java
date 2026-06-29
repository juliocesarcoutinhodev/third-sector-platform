package br.com.toponesystem.thirdsector.organization;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.PlanFixtures;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityCommand;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityUseCase;
import br.com.toponesystem.thirdsector.organization.application.usecase.CreateOrganizationCommand;
import br.com.toponesystem.thirdsector.organization.application.usecase.CreateOrganizationUseCase;
import br.com.toponesystem.thirdsector.organization.domain.exception.OrganizationLimitExceededException;
import br.com.toponesystem.thirdsector.organization.domain.port.out.OrganizationRepository;
import br.com.toponesystem.thirdsector.plan.application.usecase.UpdatePlanCommand;
import br.com.toponesystem.thirdsector.plan.application.usecase.UpdatePlanUseCase;
import br.com.toponesystem.thirdsector.tenant.adapter.out.migration.TenantMigrationService;
import br.com.toponesystem.thirdsector.shared.domain.TenantContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrganizationLimitIntegrationTest extends AbstractIntegrationTest {

    private static final String LIMITED_TENANT = "limit-" + System.nanoTime();
    private static final String UNLIMITED_TENANT = "unlimit-" + System.nanoTime();
    private static final String RETROACTIVE_TENANT = "retro-" + System.nanoTime();

    @DynamicPropertySource
    static void registerTenants(DynamicPropertyRegistry registry) {
        registry.add("tenant.known-tenants",
                () -> "maringa,londrina," + LIMITED_TENANT + "," + UNLIMITED_TENANT + "," + RETROACTIVE_TENANT);
    }

    @Autowired
    private RegisterMunicipalityUseCase registerMunicipality;

    @Autowired
    private TenantMigrationService migrationService;

    @Autowired
    private CreateOrganizationUseCase createOrganizationUseCase;

    @Autowired
    private UpdatePlanUseCase updatePlanUseCase;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PlanFixtures planFixtures;

    @BeforeAll
    void setUpTenants() {
        var basicId = planFixtures.basicPlanId();
        var enterpriseId = planFixtures.enterprisePlanId();

        registerMunicipality.execute(new RegisterMunicipalityCommand(
                "Limit Tenant", validCnpj(1), LIMITED_TENANT, basicId, null,
                "Admin", "adm@test.com"));
        registerMunicipality.execute(new RegisterMunicipalityCommand(
                "Unlimited Tenant", validCnpj(2), UNLIMITED_TENANT, enterpriseId, null,
                "Admin", "adm@test.com"));
        registerMunicipality.execute(new RegisterMunicipalityCommand(
                "Retroactive Tenant", validCnpj(3), RETROACTIVE_TENANT, basicId, null,
                "Admin", "adm@test.com"));

        migrationService.migrate(LIMITED_TENANT);
        migrationService.migrate(UNLIMITED_TENANT);
        migrationService.migrate(RETROACTIVE_TENANT);
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
    void basicPlanAllowsUpTo5OrganizationsAndRejectsThe6th() {
        TenantContext.setCurrentTenant(LIMITED_TENANT);
        try {
            for (int i = 1; i <= 5; i++) {
                var view = createOrganizationUseCase.execute(
                        new CreateOrganizationCommand("Org " + i, validCnpj(10 + i)));
                assertThat(view.id()).isNotNull();
            }

            assertThatThrownBy(() ->
                    createOrganizationUseCase.execute(
                            new CreateOrganizationCommand("Org 6", validCnpj(16)))
            ).isInstanceOf(OrganizationLimitExceededException.class)
                    .hasMessageContaining("Limite de organizacoes atingido");
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void enterprisePlanNeverRejectsOnLimit() {
        TenantContext.setCurrentTenant(UNLIMITED_TENANT);
        try {
            for (int i = 1; i <= 10; i++) {
                var view = createOrganizationUseCase.execute(
                        new CreateOrganizationCommand("Unlimited Org " + i, validCnpj(100 + i)));
                assertThat(view.id()).isNotNull();
            }
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void adjustingPlanLimitTakesEffectImmediately() {
        TenantContext.setCurrentTenant(RETROACTIVE_TENANT);
        try {
            var basicId = planFixtures.basicPlanId();

            for (int i = 1; i <= 5; i++) {
                createOrganizationUseCase.execute(
                        new CreateOrganizationCommand("Adj Org " + i, validCnpj(200 + i)));
            }

            assertThatThrownBy(() ->
                    createOrganizationUseCase.execute(
                            new CreateOrganizationCommand("Should Fail", validCnpj(210)))
            ).isInstanceOf(OrganizationLimitExceededException.class);

            updatePlanUseCase.execute(new UpdatePlanCommand(basicId, 10));

            for (int i = 6; i <= 10; i++) {
                var view = createOrganizationUseCase.execute(
                        new CreateOrganizationCommand("Adj Org " + i, validCnpj(200 + i)));
                assertThat(view.id()).isNotNull();
            }

            assertThatThrownBy(() ->
                    createOrganizationUseCase.execute(
                            new CreateOrganizationCommand("Should Fail Again", validCnpj(220)))
            ).isInstanceOf(OrganizationLimitExceededException.class)
                    .hasMessageContaining("10");

            updatePlanUseCase.execute(new UpdatePlanCommand(basicId, 5));

        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void loweringLimitDoesNotDeleteExistingOrganizations() {
        TenantContext.setCurrentTenant(RETROACTIVE_TENANT);
        try {
            var countBefore = organizationRepository.count();
            assertThat(countBefore).isGreaterThanOrEqualTo(10);

            var basicId = planFixtures.basicPlanId();
            updatePlanUseCase.execute(new UpdatePlanCommand(basicId, 2));

            var countAfter = organizationRepository.count();
            assertThat(countAfter).isEqualTo(countBefore);

            assertThatThrownBy(() ->
                    createOrganizationUseCase.execute(
                            new CreateOrganizationCommand("Should Fail", validCnpj(999)))
            ).isInstanceOf(OrganizationLimitExceededException.class);

            updatePlanUseCase.execute(new UpdatePlanCommand(basicId, 5));
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
