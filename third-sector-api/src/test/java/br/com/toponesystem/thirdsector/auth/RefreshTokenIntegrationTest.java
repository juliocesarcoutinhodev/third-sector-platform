package br.com.toponesystem.thirdsector.auth;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateUserCommand;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateUserUseCase;
import br.com.toponesystem.thirdsector.auth.application.usecase.LoginCommand;
import br.com.toponesystem.thirdsector.auth.application.usecase.LoginUseCase;
import br.com.toponesystem.thirdsector.auth.domain.model.Role;
import br.com.toponesystem.thirdsector.PlanFixtures;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityCommand;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityUseCase;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RefreshTokenIntegrationTest extends AbstractIntegrationTest {

    private static final long NANO = System.nanoTime();
    private static final String TENANT = "refresh-" + NANO;
    private static final UUID TEST_ORG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @DynamicPropertySource
    static void registerTenant(DynamicPropertyRegistry registry) {
        registry.add("tenant.known-tenants",
                () -> "maringa,londrina,refresh-" + NANO);
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
    private LoginUseCase loginUseCase;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlanFixtures planFixtures;

    @BeforeAll
    void setUpTenant() {
        var planId = planFixtures.enterprisePlanId();
        registerMunicipality.execute(new RegisterMunicipalityCommand(
                "Refresh Test Tenant", "88888888000191", TENANT, planId, null));
        migrationService.migrate(TENANT);
        TenantContext.setCurrentTenant(TENANT);
        createOrganization.execute(new CreateOrganizationCommand("Refresh ONG", "12345678000195"));
        createUserUseCase.execute(new CreateUserCommand(
                "Ana Costa", "ana@example.com", "Senha123",
                Role.ORGANIZATION_MANAGER, TEST_ORG_ID));
        TenantContext.clear();
    }

    @Test
    void refreshWithValidTokenReturnsNewPairAndInvalidatesOld() throws Exception {
        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@example.com","password":"Senha123"}"""))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        var refreshToken = loginResult.getResponse().getCookie("refresh_token").getValue();

        var refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        var newRefreshToken = refreshResult.getResponse().getCookie("refresh_token").getValue();

        assertThat(newRefreshToken).isNotEqualTo(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Token de acesso inválido ou expirado."));
    }

    @Test
    void refreshWithRevokedTokenIsRejected() throws Exception {
        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@example.com","password":"Senha123"}"""))
                .andExpect(status().isOk())
                .andReturn();

        var refreshToken = loginResult.getResponse().getCookie("refresh_token").getValue();

        mockMvc.perform(post("/api/auth/refresh")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/refresh")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Token de acesso inválido ou expirado."));
    }

    @Test
    void refreshWithFakeTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "fake-token-value")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Token de acesso inválido ou expirado."));
    }

    @Test
    void refreshWithoutCookieIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .header("Host", TENANT + ".thirdsector.com.br"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Token de acesso inválido ou expirado."));
    }
}
