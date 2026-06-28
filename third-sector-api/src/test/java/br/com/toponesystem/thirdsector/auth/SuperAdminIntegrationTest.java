package br.com.toponesystem.thirdsector.auth;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateSuperAdminCommand;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateSuperAdminUseCase;
import br.com.toponesystem.thirdsector.auth.domain.exception.DuplicateSuperAdminEmailException;
import br.com.toponesystem.thirdsector.auth.domain.port.out.SuperAdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SuperAdminIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CreateSuperAdminUseCase createSuperAdminUseCase;

    @Autowired
    private SuperAdminRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static String uniqueEmail(String prefix) {
        return prefix + "-" + System.nanoTime() + "@test.com";
    }

    @Test
    void createsSuperAdminWithHashedPasswordInMasterSchema() {
        var email = uniqueEmail("sa-create");

        var result = createSuperAdminUseCase.execute(
                new CreateSuperAdminCommand("Test SA", email));

        assertThat(result.superAdmin().id()).isNotNull();
        assertThat(result.superAdmin().email()).isEqualTo(email);
        assertThat(result.superAdmin().name()).isEqualTo("Test SA");
        assertThat(result.superAdmin().active()).isTrue();
        assertThat(result.temporaryPassword()).hasSize(16);
        assertThat(result.temporaryPassword()).isNotBlank();

        var persisted = repository.findByEmail(email).orElseThrow();
        assertThat(persisted.getPasswordHash()).isNotEqualTo(result.temporaryPassword());
        assertThat(persisted.getPasswordHash()).startsWith("$2a$");
        assertThat(passwordEncoder.matches(result.temporaryPassword(), persisted.getPasswordHash())).isTrue();
    }

    @Test
    void rejectsDuplicateEmailWithoutGeneratingNewPassword() {
        var email = uniqueEmail("sa-dup");

        createSuperAdminUseCase.execute(
                new CreateSuperAdminCommand("Test SA", email));

        assertThatThrownBy(() ->
                createSuperAdminUseCase.execute(
                        new CreateSuperAdminCommand("Test SA Duplicate", email))
        ).isInstanceOf(DuplicateSuperAdminEmailException.class)
                .hasMessageContaining(email);
    }

    @Test
    void executeWithPasswordUsesProvidedPassword() {
        var email = uniqueEmail("sa-fixed");
        var fixedPassword = "FixedPass1";

        var result = createSuperAdminUseCase.executeWithPassword(
                new CreateSuperAdminCommand("Fixed SA", email), fixedPassword);

        assertThat(result.temporaryPassword()).isEqualTo(fixedPassword);
        var persisted = repository.findByEmail(email).orElseThrow();
        assertThat(passwordEncoder.matches(fixedPassword, persisted.getPasswordHash())).isTrue();
    }
}
