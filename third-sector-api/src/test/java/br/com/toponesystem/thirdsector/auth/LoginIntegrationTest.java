package br.com.toponesystem.thirdsector.auth;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateUserCommand;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateUserUseCase;
import br.com.toponesystem.thirdsector.auth.application.usecase.LoginCommand;
import br.com.toponesystem.thirdsector.auth.application.usecase.LoginUseCase;
import br.com.toponesystem.thirdsector.auth.domain.exception.AuthenticationFailedException;
import br.com.toponesystem.thirdsector.auth.domain.model.Role;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityCommand;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityUseCase;
import br.com.toponesystem.thirdsector.municipality.domain.model.Plan;
import br.com.toponesystem.thirdsector.organization.application.usecase.CreateOrganizationCommand;
import br.com.toponesystem.thirdsector.organization.application.usecase.CreateOrganizationUseCase;
import br.com.toponesystem.thirdsector.tenant.adapter.out.migration.TenantMigrationService;
import br.com.toponesystem.thirdsector.tenant.domain.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoginIntegrationTest extends AbstractIntegrationTest {

    private static final String TENANT = "maringa";

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
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    void setUpTenant() {
        registerMunicipality.execute(new RegisterMunicipalityCommand(
                "Login Test Tenant", "66666666000191", TENANT, Plan.BASIC, null));
        migrationService.migrate(TENANT);
        TenantContext.setCurrentTenant(TENANT);
        createOrganization.execute(new CreateOrganizationCommand("Login ONG", "12345678000195"));
        createUserUseCase.execute(new CreateUserCommand(
                "Carlos Souza", "carlos@example.com", "Senha123",
                Role.ORGANIZATION_MANAGER, 1L));
        TenantContext.clear();
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void loginWithValidCredentialsReturns200AndSetsCookie() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"carlos@example.com","password":"Senha123"}"""))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().sameSite("access_token", "Lax"))
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.name").value("Carlos Souza"))
                .andExpect(jsonPath("$.email").value("carlos@example.com"))
                .andExpect(jsonPath("$.role").value("ORGANIZATION_MANAGER"))
                .andExpect(jsonPath("$.organizationId").value(1L))
                .andExpect(jsonPath("$.access_token").doesNotExist());
    }

    @Test
    void loginWithWrongPasswordReturnsGenericError() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"carlos@example.com","password":"SenhaErrada"}"""))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void loginWithUnknownEmailReturnsSameGenericError() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"naoexiste@example.com","password":"Senha123"}"""))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void loginUseCaseRejectsInactiveUser() {
        TenantContext.setCurrentTenant(TENANT);
        try {
            createUserUseCase.execute(new CreateUserCommand(
                    "Inativo Silva", "inativo@example.com", "Senha789",
                    Role.OPERATOR, 1L));

            jdbcTemplate.execute("SET search_path TO \"" + TENANT + "\"");
            jdbcTemplate.update("UPDATE users SET active = false WHERE email = ?", "inativo@example.com");

            assertThatThrownBy(() ->
                    loginUseCase.execute(new LoginCommand("inativo@example.com", "Senha789"))
            ).isInstanceOf(AuthenticationFailedException.class)
                    .hasMessageContaining("Invalid email or password");
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void loginResponseDoesNotContainTokenInBody() throws Exception {
        var result = mockMvc.perform(post("/api/auth/login")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"carlos@example.com","password":"Senha123"}"""))
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThat(body).doesNotContain("token");
        assertThat(body).doesNotContain("access_token");
        assertThat(result.getResponse().getHeader("Set-Cookie")).contains("access_token=");
    }
}
