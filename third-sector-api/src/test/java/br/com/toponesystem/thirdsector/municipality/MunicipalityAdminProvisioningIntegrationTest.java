package br.com.toponesystem.thirdsector.municipality;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.PlanFixtures;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityCommand;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityUseCase;
import br.com.toponesystem.thirdsector.shared.domain.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class MunicipalityAdminProvisioningIntegrationTest extends AbstractIntegrationTest {

    private static final String ADMIN_EMAIL = "admin.provisioning@test.com";
    private static final String TENANT = "provisioning-test";

    @Autowired
    private RegisterMunicipalityUseCase registerMunicipalityUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlanFixtures planFixtures;

    @Test
    void registeringMunicipalityCreatesAdminWithMustChangePasswordTrue() {
        var planId = planFixtures.enterprisePlanId();
        registerMunicipalityUseCase.execute(new RegisterMunicipalityCommand(
                "Provisioning Test City", validCnpj(101), TENANT, planId, null,
                "Admin Provisioning", ADMIN_EMAIL));

        TenantContext.setCurrentTenant(TENANT);
        try {
            jdbcTemplate.execute("SET search_path TO \"" + TENANT + "\"");

            var mustChange = jdbcTemplate.queryForObject(
                    "SELECT must_change_password FROM users WHERE email = ?",
                    Boolean.class, ADMIN_EMAIL);

            assertThat(mustChange).isTrue();

            var role = jdbcTemplate.queryForObject(
                    "SELECT role FROM users WHERE email = ?",
                    String.class, ADMIN_EMAIL);

            assertThat(role).isEqualTo("MUNICIPALITY_ADM");
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void registerMunicipalityWithoutSuperAdminReturnsNotFound() throws Exception {
        // TenantFilter rejects requests without a valid tenant subdomain or SUPER_ADMIN JWT
        // by responding 404 (security-by-obscurity: don't reveal which endpoints exist)
        mockMvc.perform(post("/api/municipalities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Hidden Password City",
                                  "cnpj": "%s",
                                  "subdomain": "hidden-pwd-test",
                                  "planId": "%s",
                                  "adminName": "Admin Hidden",
                                  "adminEmail": "admin.hidden@test.com"
                                }""".formatted(validCnpj(103), planFixtures.enterprisePlanId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminPasswordIsStoredAsBCryptHashInTenantSchema() {
        var planId = planFixtures.enterprisePlanId();
        var tenant = "tmp-pwd-login-test";
        var email = "admin.tmplogin@test.com";

        registerMunicipalityUseCase.execute(new RegisterMunicipalityCommand(
                "TmpPwd Login City", validCnpj(102), tenant, planId, null,
                "Admin TmpLogin", email));

        TenantContext.setCurrentTenant(tenant);
        try {
            jdbcTemplate.execute("SET search_path TO \"" + tenant + "\"");

            var passwordHash = jdbcTemplate.queryForObject(
                    "SELECT password_hash FROM users WHERE email = ?",
                    String.class, email);

            assertThat(passwordHash).isNotBlank();
            assertThat(passwordHash).startsWith("$2a$"); // BCrypt prefix
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
