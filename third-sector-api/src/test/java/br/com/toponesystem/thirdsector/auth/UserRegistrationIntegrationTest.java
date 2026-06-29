package br.com.toponesystem.thirdsector.auth;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateUserCommand;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateUserUseCase;
import br.com.toponesystem.thirdsector.auth.domain.exception.DuplicateEmailException;
import br.com.toponesystem.thirdsector.auth.domain.exception.InvalidUserRoleAssignmentException;
import br.com.toponesystem.thirdsector.auth.domain.model.Role;
import br.com.toponesystem.thirdsector.auth.domain.port.out.UserRepository;
import br.com.toponesystem.thirdsector.PlanFixtures;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityCommand;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityUseCase;
import br.com.toponesystem.thirdsector.organization.application.usecase.CreateOrganizationCommand;
import br.com.toponesystem.thirdsector.organization.application.usecase.CreateOrganizationUseCase;
import br.com.toponesystem.thirdsector.tenant.adapter.out.migration.TenantMigrationService;
import br.com.toponesystem.thirdsector.shared.domain.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRegistrationIntegrationTest extends AbstractIntegrationTest {

    private static final String TENANT = "userreg-" + System.nanoTime();
    private static final UUID TEST_ORG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private RegisterMunicipalityUseCase registerMunicipality;

    @Autowired
    private TenantMigrationService migrationService;

    @Autowired
    private CreateOrganizationUseCase createOrganization;

    @Autowired
    private CreateUserUseCase createUserUseCase;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PlanFixtures planFixtures;

    @BeforeAll
    void setUpTenant() {
        var planId = planFixtures.enterprisePlanId();
        registerMunicipality.execute(new RegisterMunicipalityCommand(
                "User Registration Tenant", "44444444000191", TENANT, planId, null, "Admin", "adm@test.com"));
        migrationService.migrate(TENANT);
        TenantContext.setCurrentTenant(TENANT);
        createOrganization.execute(new CreateOrganizationCommand("Teste ONG", "12345678000195"));
        TenantContext.clear();
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void registersUserWithHashedPassword() {
        TenantContext.setCurrentTenant(TENANT);
        try {
            var view = createUserUseCase.execute(new CreateUserCommand(
                    "João Silva", "joao@example.com", "Senha123",
                    Role.OPERATOR, TEST_ORG_ID));

            assertThat(view.id()).isNotNull();
            assertThat(view.name()).isEqualTo("João Silva");
            assertThat(view.email()).isEqualTo("joao@example.com");
            assertThat(view.role()).isEqualTo(Role.OPERATOR);
            assertThat(view.active()).isTrue();

            var persisted = userRepository.findByEmail("joao@example.com").orElseThrow();
            assertThat(persisted.getPasswordHash()).isNotEqualTo("Senha123");
            assertThat(passwordEncoder.matches("Senha123", persisted.getPasswordHash())).isTrue();
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void rejectsDuplicateEmail() {
        TenantContext.setCurrentTenant(TENANT);
        try {
            createUserUseCase.execute(new CreateUserCommand(
                    "Maria Souza", "maria@example.com", "Senha456",
                    Role.ORGANIZATION_MANAGER, TEST_ORG_ID));

            assertThatThrownBy(() ->
                    createUserUseCase.execute(new CreateUserCommand(
                            "Maria Duplicate", "maria@example.com", "OutraSenha1",
                            Role.OPERATOR, TEST_ORG_ID))
            ).isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("maria@example.com");
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void rejectsSuperAdminWithOrganization() {
        TenantContext.setCurrentTenant(TENANT);
        try {
            assertThatThrownBy(() ->
                    createUserUseCase.execute(new CreateUserCommand(
                            "Admin Inválido", "admin-inv@example.com", "Senha789",
                            Role.SUPER_ADMIN, TEST_ORG_ID))
            ).isInstanceOf(InvalidUserRoleAssignmentException.class)
                    .hasMessageContaining("SUPER_ADMIN")
                    .hasMessageContaining("não permite vínculo com organização.");
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void rejectsOperatorWithoutOrganization() {
        TenantContext.setCurrentTenant(TENANT);
        try {
            assertThatThrownBy(() ->
                    createUserUseCase.execute(new CreateUserCommand(
                            "Operador Sem Org", "opsemorg@example.com", "Senha000",
                            Role.OPERATOR, null))
            ).isInstanceOf(InvalidUserRoleAssignmentException.class)
                    .hasMessageContaining("OPERATOR")
                    .hasMessageContaining("exige vínculo com uma organização.");
        } finally {
            TenantContext.clear();
        }
    }
}
