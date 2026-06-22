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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LogoutIntegrationTest extends AbstractIntegrationTest {

    private static final String TENANT = "logout-test";
    private static final UUID TEST_ORG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @DynamicPropertySource
    static void registerTenant(DynamicPropertyRegistry registry) {
        registry.add("tenant.known-tenants", () -> "maringa,londrina,logout-test");
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
                "Logout Test Tenant", "99999999000191", TENANT, Plan.BASIC, null));
        migrationService.migrate(TENANT);
        TenantContext.setCurrentTenant(TENANT);
        createOrganization.execute(new CreateOrganizationCommand("Logout ONG", "12345678000195"));
        createUserUseCase.execute(new CreateUserCommand(
                "Diana Melo", "diana@example.com", "Senha123",
                Role.ORGANIZATION_MANAGER, TEST_ORG_ID));
        TenantContext.clear();
    }

    @Test
    void logoutRevokesTokensAndRefreshIsRejected() throws Exception {
        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"diana@example.com","password":"Senha123"}"""))
                .andExpect(status().isOk())
                .andReturn();

        var refreshToken = loginResult.getResponse().getCookie("refresh_token").getValue();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Token de acesso inválido ou expirado."));
    }

    @Test
    void logoutWithoutCookiesIsIdempotent() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Host", TENANT + ".thirdsector.com.br"))
                .andExpect(status().isNoContent());
    }

    @Test
    void logoutClearsCookies() throws Exception {
        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"diana@example.com","password":"Senha123"}"""))
                .andExpect(status().isOk())
                .andReturn();

        var refreshToken = loginResult.getResponse().getCookie("refresh_token").getValue();

        var logoutResult = mockMvc.perform(post("/api/auth/logout")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
                .andExpect(status().isNoContent())
                .andReturn();

        var setCookieHeaders = logoutResult.getResponse().getHeaders("Set-Cookie");
        var hasClearedAccess = setCookieHeaders.stream()
                .anyMatch(h -> h.contains("access_token=;") && h.contains("Max-Age=0"));
        var hasClearedRefresh = setCookieHeaders.stream()
                .anyMatch(h -> h.contains("refresh_token=;") && h.contains("Max-Age=0"));

        assertThat(hasClearedAccess).isTrue();
        assertThat(hasClearedRefresh).isTrue();
    }

    @Test
    void doubleLogoutIsIdempotent() throws Exception {
        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"diana@example.com","password":"Senha123"}"""))
                .andExpect(status().isOk())
                .andReturn();

        var refreshToken = loginResult.getResponse().getCookie("refresh_token").getValue();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/logout")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
                .andExpect(status().isNoContent());
    }
}
