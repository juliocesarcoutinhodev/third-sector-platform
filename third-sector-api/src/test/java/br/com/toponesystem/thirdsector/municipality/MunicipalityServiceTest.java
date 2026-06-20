package br.com.toponesystem.thirdsector.municipality;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.municipality.application.dto.MunicipalityView;
import br.com.toponesystem.thirdsector.municipality.application.usecase.FindMunicipalityBySubdomainUseCase;
import br.com.toponesystem.thirdsector.municipality.application.usecase.ListActiveMunicipalitiesUseCase;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityUseCase;
import br.com.toponesystem.thirdsector.municipality.domain.exception.DuplicateSubdomainException;
import br.com.toponesystem.thirdsector.municipality.domain.exception.MunicipalityNotFoundException;
import br.com.toponesystem.thirdsector.municipality.domain.model.Plan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MunicipalityServiceTest extends AbstractIntegrationTest {

    private static final String CNPJ_MARINGA   = "11222333000181";
    private static final String CNPJ_LONDRINA  = "11444777000161";
    private static final String CNPJ_LONDRINA2 = "55333222000118";
    private static final String CNPJ_CURITIBA  = "43773954000140";
    private static final String CNPJ_CASCAVEL  = "12345678000195";

    @Autowired
    private RegisterMunicipalityUseCase registerUseCase;

    @Autowired
    private FindMunicipalityBySubdomainUseCase findBySubdomainUseCase;

    @Autowired
    private ListActiveMunicipalitiesUseCase listActiveUseCase;

    @Test
    void registersNewMunicipality() {
        var view = registerUseCase.execute("Maringá", CNPJ_MARINGA, "maringa-test", Plan.BASIC);

        assertThat(view.id()).isNotNull();
        assertThat(view.subdomain()).isEqualTo("maringa-test");
        assertThat(view.cnpj()).isEqualTo(CNPJ_MARINGA);
        assertThat(view.active()).isTrue();
    }

    @Test
    void stripsCnpjMaskOnRegister() {
        var view = registerUseCase.execute("Curitiba", "43.773.954/0001-40", "curitiba-test", Plan.PREMIUM);

        assertThat(view.cnpj()).isEqualTo(CNPJ_CURITIBA);
    }

    @Test
    void rejectsDuplicateSubdomain() {
        registerUseCase.execute("Londrina", CNPJ_LONDRINA, "londrina-test", Plan.STANDARD);

        assertThatThrownBy(() ->
                registerUseCase.execute("Londrina 2", CNPJ_LONDRINA2, "londrina-test", Plan.BASIC)
        ).isInstanceOf(DuplicateSubdomainException.class)
                .hasMessageContaining("londrina-test");
    }

    @Test
    void findsBySubdomain() {
        registerUseCase.execute("Cascavel", CNPJ_CASCAVEL, "cascavel-test", Plan.BASIC);

        MunicipalityView found = findBySubdomainUseCase.execute("cascavel-test");

        assertThat(found.name()).isEqualTo("Cascavel");
    }

    @Test
    void throwsWhenSubdomainNotFound() {
        assertThatThrownBy(() -> findBySubdomainUseCase.execute("nonexistent"))
                .isInstanceOf(MunicipalityNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    @Test
    void listActiveReturnsOnlyActiveMunicipalities() {
        int before = listActiveUseCase.execute().size();
        registerUseCase.execute("Londrina Norte", CNPJ_LONDRINA2, "londrina-norte-test", Plan.STANDARD);

        assertThat(listActiveUseCase.execute()).hasSizeGreaterThan(before);
    }
}
