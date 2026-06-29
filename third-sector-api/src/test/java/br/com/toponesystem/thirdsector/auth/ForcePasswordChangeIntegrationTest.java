package br.com.toponesystem.thirdsector.auth;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.PlanFixtures;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityCommand;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityUseCase;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ForcePasswordChangeIntegrationTest extends AbstractIntegrationTest {

    private static final String TENANT = "force-pwd-tenant";
    private static final String ADMIN_EMAIL = "admin.force@test.com";
    private static final String NEW_PASSWORD = "NewSecure1@";

    @DynamicPropertySource
    static void registerTenants(DynamicPropertyRegistry registry) {
        registry.add("tenant.known-tenants", () -> "maringa,londrina," + TENANT);
    }

    @Autowired
    private RegisterMunicipalityUseCase registerMunicipalityUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlanFixtures planFixtures;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String knownPasswordHash;

    @BeforeAll
    void setUp() {
        knownPasswordHash = passwordEncoder.encode(NEW_PASSWORD);
        var planId = planFixtures.enterprisePlanId();
        registerMunicipalityUseCase.execute(new RegisterMunicipalityCommand(
                "Force Pwd City", validCnpj(200), TENANT, planId, null,
                "Admin Force", ADMIN_EMAIL));
    }

    @Test
    void loginWithTemporaryPasswordReturnsMustChangePasswordTrue() throws Exception {
        var tempPwd = resetToKnownPassword();

        mockMvc.perform(post("/api/auth/login")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}""".formatted(ADMIN_EMAIL, tempPwd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mustChangePassword").value(true))
                .andExpect(cookie().exists("access_token"));
    }

    @Test
    void restrictedTokenCannotAccessProtectedEndpoints() throws Exception {
        var restrictedToken = obtainRestrictedToken();

        mockMvc.perform(post("/api/users")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new Cookie("access_token", restrictedToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Hacker","email":"hacker@t.com","password":"Senha123",
                                 "role":"OPERATOR","organizationId":"00000000-0000-0000-0000-000000000001"}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void forcePasswordChangeWithRestrictedTokenSucceedsAndIssuesFullToken() throws Exception {
        var restrictedToken = obtainRestrictedToken();

        var result = mockMvc.perform(post("/api/auth/force-password-change")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new Cookie("access_token", restrictedToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newPassword":"%s"}""".formatted(NEW_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mustChangePassword").value(false))
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        var fullToken = result.getResponse().getCookie("access_token").getValue();
        assertThat(fullToken).isNotBlank();
    }

    @Test
    void afterPasswordChangeFullTokenCannotAccessForceChangeEndpoint() throws Exception {
        var restrictedToken = obtainRestrictedToken();

        var changeResult = mockMvc.perform(post("/api/auth/force-password-change")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new Cookie("access_token", restrictedToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newPassword":"Final@Pass99"}"""))
                .andExpect(status().isOk())
                .andReturn();

        var fullToken = changeResult.getResponse().getCookie("access_token").getValue();

        mockMvc.perform(post("/api/auth/force-password-change")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new Cookie("access_token", fullToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newPassword":"AnotherPass1"}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void mustChangePasswordIsFalseInDbAfterForceChange() throws Exception {
        var restrictedToken = obtainRestrictedToken();

        mockMvc.perform(post("/api/auth/force-password-change")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .cookie(new Cookie("access_token", restrictedToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newPassword":"DbCheck@Pass1"}"""))
                .andExpect(status().isOk());

        var mustChange = jdbcTemplate.queryForObject(
                "SELECT must_change_password FROM \"" + TENANT + "\".users WHERE email = ?",
                Boolean.class, ADMIN_EMAIL);
        assertThat(mustChange).isFalse();
    }

    private String obtainRestrictedToken() throws Exception {
        var tempPwd = resetToKnownPassword();

        var result = mockMvc.perform(post("/api/auth/login")
                        .header("Host", TENANT + ".thirdsector.com.br")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}""".formatted(ADMIN_EMAIL, tempPwd)))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getCookie("access_token").getValue();
    }

    // BCrypt is one-way — reset to a known hash so tests can authenticate.
    // In production, the admin receives the temporary password via email (Mailpit in dev).
    private String resetToKnownPassword() {
        jdbcTemplate.update(
                "UPDATE \"" + TENANT + "\".users SET password_hash = ?, must_change_password = true WHERE email = ?",
                knownPasswordHash, ADMIN_EMAIL);
        return NEW_PASSWORD;
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
