package br.com.toponesystem.thirdsector.auth;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateUserCommand;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateUserUseCase;
import br.com.toponesystem.thirdsector.auth.domain.model.PasswordResetToken;
import br.com.toponesystem.thirdsector.auth.domain.model.Role;
import br.com.toponesystem.thirdsector.auth.domain.port.out.PasswordResetTokenRepository;
import br.com.toponesystem.thirdsector.auth.domain.port.out.RefreshTokenGenerator;
import br.com.toponesystem.thirdsector.auth.domain.port.out.TokenHasher;
import br.com.toponesystem.thirdsector.PlanFixtures;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityCommand;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityUseCase;
import br.com.toponesystem.thirdsector.organization.application.usecase.CreateOrganizationCommand;
import br.com.toponesystem.thirdsector.organization.application.usecase.CreateOrganizationUseCase;
import br.com.toponesystem.thirdsector.tenant.adapter.out.migration.TenantMigrationService;
import br.com.toponesystem.thirdsector.tenant.domain.TenantContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PasswordResetIntegrationTest extends AbstractIntegrationTest {

    private static final String TENANT = "pwdreset";
    private static final UUID TEST_ORG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private UUID testUserId;

    @DynamicPropertySource
    static void registerTenant(DynamicPropertyRegistry registry) {
        registry.add("tenant.known-tenants", () -> "maringa,londrina,pwdreset");
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
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private RefreshTokenGenerator tokenGenerator;

    @Autowired
    private TokenHasher tokenHasher;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlanFixtures planFixtures;

    @BeforeAll
    void setUpTenant() {
        var planId = planFixtures.enterprisePlanId();
        registerMunicipality.execute(new RegisterMunicipalityCommand(
                "PwdReset Tenant", "12121212000106", TENANT, planId, null));
        migrationService.migrate(TENANT);
        TenantContext.setCurrentTenant(TENANT);
        createOrganization.execute(new CreateOrganizationCommand("PwdReset ONG", "12345678000195"));
        var userView = createUserUseCase.execute(new CreateUserCommand(
                "Eduardo Reis", "edu@example.com", "Senha123",
                Role.ORGANIZATION_MANAGER, TEST_ORG_ID));
        testUserId = userView.id();
        TenantContext.clear();
    }

    @BeforeEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void requestForExistingEmailReturnsSameMessageAsUnknown() throws Exception {
        var response1 = mockMvc.perform(post("/api/auth/password-reset/request")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"edu@example.com"}"""))
                .andExpect(status().isOk())
                .andReturn();

        var response2 = mockMvc.perform(post("/api/auth/password-reset/request")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"naoexiste@example.com"}"""))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(response1.getResponse().getContentAsString())
                .isEqualTo(response2.getResponse().getContentAsString());
    }

    @Test
    void confirmWithValidTokenResetsPasswordAndRevokesSessions() throws Exception {
        var token = createValidToken();

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"newPassword\":\"NovaSenha1\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"edu@example.com","password":"NovaSenha1"}"""))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"edu@example.com","password":"Senha123"}"""))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void reuseOfSameTokenIsRejected() throws Exception {
        var token = createValidToken();

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"newPassword\":\"NovaSenha2\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"newPassword\":\"OutraSenha1\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void confirmWithFakeTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"fake-token","newPassword":"Senha999"}"""))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Token de redefinição inválido ou expirado."));
    }

    @Test
    void requestEndpointCallSucceedsForExistingUser() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset/request")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"edu@example.com"}"""))
                .andExpect(status().isOk());
    }

    private String createValidToken() {
        var rawToken = tokenGenerator.generate();
        TenantContext.setCurrentTenant(TENANT);
        try {
            var token = new PasswordResetToken(
                    testUserId,
                    tokenHasher.hash(rawToken),
                    Instant.now().plusSeconds(1800));
            tokenRepository.save(token);
        } finally {
            TenantContext.clear();
        }
        return rawToken;
    }
}
