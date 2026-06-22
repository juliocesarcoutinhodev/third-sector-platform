package br.com.toponesystem.thirdsector.auth;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateUserCommand;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateUserUseCase;
import br.com.toponesystem.thirdsector.auth.domain.model.Role;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityCommand;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityUseCase;
import br.com.toponesystem.thirdsector.municipality.domain.model.Plan;
import br.com.toponesystem.thirdsector.organization.application.usecase.CreateOrganizationCommand;
import br.com.toponesystem.thirdsector.organization.application.usecase.CreateOrganizationUseCase;
import br.com.toponesystem.thirdsector.tenant.adapter.out.migration.TenantMigrationService;
import br.com.toponesystem.thirdsector.tenant.domain.TenantContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TokenReuseDetectionIntegrationTest extends AbstractIntegrationTest {

    private static final String TENANT = "tokenreuse";

    @DynamicPropertySource
    static void registerTenant(DynamicPropertyRegistry registry) {
        registry.add("tenant.known-tenants", () -> "maringa,londrina,tokenreuse");
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

    @BeforeAll
    void setUpTenant() {
        registerMunicipality.execute(new RegisterMunicipalityCommand(
                "Token Reuse Tenant", "11111111000191", TENANT, Plan.BASIC, null));
        migrationService.migrate(TENANT);
        TenantContext.setCurrentTenant(TENANT);
        createOrganization.execute(new CreateOrganizationCommand("Reuse ONG", "12345678000195"));
        createUserUseCase.execute(new CreateUserCommand(
                "Bruno Lima", "bruno@example.com", "Senha123",
                Role.ORGANIZATION_MANAGER, 1L));
        TenantContext.clear();
    }

    @Test
    void reuseOfRevokedTokenRevokesEntireFamily() throws Exception {
        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bruno@example.com","password":"Senha123"}"""))
                .andExpect(status().isOk())
                .andReturn();

        var refreshToken1 = loginResult.getResponse().getCookie("refresh_token").getValue();

        var refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken1)))
                .andExpect(status().isOk())
                .andReturn();

        var refreshToken2 = refreshResult.getResponse().getCookie("refresh_token").getValue();

        mockMvc.perform(post("/api/auth/refresh")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken1)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Token de acesso inválido ou expirado."));

        mockMvc.perform(post("/api/auth/refresh")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken2)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Token de acesso inválido ou expirado."));
    }

    @Test
    void consecutiveRotationsWorkNormally() throws Exception {
        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bruno@example.com","password":"Senha123"}"""))
                .andExpect(status().isOk())
                .andReturn();

        var token = loginResult.getResponse().getCookie("refresh_token").getValue();

        for (int i = 0; i < 3; i++) {
            var refreshResult = mockMvc.perform(post("/api/auth/refresh")
                            .header("Host", TENANT + ".thirdsector.com.br")
                            .cookie(new jakarta.servlet.http.Cookie("refresh_token", token)))
                    .andExpect(status().isOk())
                    .andReturn();

            token = refreshResult.getResponse().getCookie("refresh_token").getValue();
        }
    }
}
