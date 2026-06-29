package br.com.toponesystem.thirdsector.auth;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateUserCommand;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateUserUseCase;
import br.com.toponesystem.thirdsector.auth.domain.model.Role;
import br.com.toponesystem.thirdsector.PlanFixtures;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityCommand;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityUseCase;
import br.com.toponesystem.thirdsector.organization.application.usecase.CreateOrganizationCommand;
import br.com.toponesystem.thirdsector.organization.application.usecase.CreateOrganizationUseCase;
import br.com.toponesystem.thirdsector.tenant.adapter.out.migration.TenantMigrationService;
import br.com.toponesystem.thirdsector.shared.domain.TenantContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthorizationIntegrationTest extends AbstractIntegrationTest {

    private static final String TENANT_X = "auth-tenant-x";
    private static final String TENANT_Y = "auth-tenant-y";
    private static final UUID ORG_ALPHA_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ORG_BETA_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @DynamicPropertySource
    static void registerTenants(DynamicPropertyRegistry registry) {
        registry.add("tenant.known-tenants",
                () -> "maringa,londrina,auth-tenant-x,auth-tenant-y");
    }

    @Autowired
    private RegisterMunicipalityUseCase registerMunicipality;

    @Autowired
    private TenantMigrationService migrationService;

    @Autowired
    private CreateOrganizationUseCase createOrganization;

    @Autowired
    private CreateUserUseCase createUserUseCase;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlanFixtures planFixtures;

    @BeforeAll
    void setUpTenants() {
        var planId = planFixtures.enterprisePlanId();
        registerMunicipality.execute(new RegisterMunicipalityCommand(
                "Auth X", "77777777000191", TENANT_X, planId, null, "Admin", "adm@test.com"));
        registerMunicipality.execute(new RegisterMunicipalityCommand(
                "Auth Y", "55555555000191", TENANT_Y, planId, null, "Admin", "adm@test.com"));
        migrationService.migrate(TENANT_X);
        migrationService.migrate(TENANT_Y);

        TenantContext.setCurrentTenant(TENANT_X);
        createOrganization.execute(new CreateOrganizationCommand("Org Alpha", "11111111000191"));
        createOrganization.execute(new CreateOrganizationCommand("Org Beta", "22222222000191"));
        createUserUseCase.execute(new CreateUserCommand(
                "Admin X", "adminx@t.com", "Senha123", Role.MUNICIPALITY_ADM, null));
        createUserUseCase.execute(new CreateUserCommand(
                "Manager Alpha", "mgr-alpha@t.com", "Senha123", Role.ORGANIZATION_MANAGER, ORG_ALPHA_ID));
        createUserUseCase.execute(new CreateUserCommand(
                "Super Admin", "super@t.com", "Senha123", Role.SUPER_ADMIN, null));
        TenantContext.clear();

        TenantContext.setCurrentTenant(TENANT_Y);
        createOrganization.execute(new CreateOrganizationCommand("Org Y", "33333333000191"));
        createUserUseCase.execute(new CreateUserCommand(
                "Admin Y", "adminy@t.com", "Senha123", Role.MUNICIPALITY_ADM, null));
        TenantContext.clear();
    }

    private String loginAndGetAccessToken(String email, String password, String tenant) throws Exception {
        var result = mockMvc.perform(post("/api/auth/login")
                        .header("Host", tenant + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie("access_token").getValue();
    }

    @Test
    void orgManagerCannotCreateUserInOtherOrganization() throws Exception {
        var token = loginAndGetAccessToken("mgr-alpha@t.com", "Senha123", TENANT_X);

        mockMvc.perform(post("/api/users")
                        .header("Host", TENANT_X + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("access_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Hacker","email":"hacker@t.com","password":"Senha999",
                                 "role":"OPERATOR","organizationId":"00000000-0000-0000-0000-000000000002"}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void orgManagerCanCreateUserInOwnOrganization() throws Exception {
        var token = loginAndGetAccessToken("mgr-alpha@t.com", "Senha123", TENANT_X);

        mockMvc.perform(post("/api/users")
                        .header("Host", TENANT_X + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("access_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New Op","email":"newop@t.com","password":"Senha999",
                                 "role":"OPERATOR","organizationId":"00000000-0000-0000-0000-000000000001"}"""))
                .andExpect(status().isCreated());
    }

    @Test
    void municipalityAdminCannotAccessOtherTenant() throws Exception {
        var token = loginAndGetAccessToken("adminy@t.com", "Senha123", TENANT_Y);

        mockMvc.perform(post("/api/users")
                        .header("Host", TENANT_X + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("access_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cross","email":"cross@t.com","password":"Senha999",
                                 "role":"OPERATOR","organizationId":"00000000-0000-0000-0000-000000000001"}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void municipalityAdminCanCreateUserInOwnTenant() throws Exception {
        var token = loginAndGetAccessToken("adminx@t.com", "Senha123", TENANT_X);

        mockMvc.perform(post("/api/users")
                        .header("Host", TENANT_X + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("access_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New MunUser","email":"munuser@t.com","password":"Senha999",
                                 "role":"OPERATOR","organizationId":"00000000-0000-0000-0000-000000000001"}"""))
                .andExpect(status().isCreated());
    }

    @Test
    void superAdminCanCreateUserInAnyTenant() throws Exception {
        var token = loginAndGetAccessToken("super@t.com", "Senha123", TENANT_X);

        mockMvc.perform(post("/api/users")
                        .header("Host", TENANT_Y + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("access_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Super Created","email":"supercreated@t.com","password":"Senha999",
                                 "role":"OPERATOR","organizationId":"00000000-0000-0000-0000-000000000001"}"""))
                .andExpect(status().isCreated());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Host", TENANT_X + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"NoAuth","email":"noauth@t.com","password":"Senha999",
                                 "role":"OPERATOR","organizationId":"00000000-0000-0000-0000-000000000001"}"""))
                .andExpect(status().isForbidden());
    }
}
