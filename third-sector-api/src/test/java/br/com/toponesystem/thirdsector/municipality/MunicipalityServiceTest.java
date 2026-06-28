package br.com.toponesystem.thirdsector.municipality;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.PlanFixtures;
import br.com.toponesystem.thirdsector.municipality.application.dto.MunicipalityView;
import br.com.toponesystem.thirdsector.municipality.application.usecase.FindMunicipalityBySubdomainUseCase;
import br.com.toponesystem.thirdsector.municipality.application.usecase.ListActiveMunicipalitiesUseCase;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityCommand;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityUseCase;
import br.com.toponesystem.thirdsector.municipality.domain.exception.DuplicateSubdomainException;
import br.com.toponesystem.thirdsector.municipality.domain.exception.MunicipalityNotFoundException;
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

    @Autowired
    private PlanFixtures planFixtures;

    @Test
    void registersNewMunicipality() {
        var planId = planFixtures.basicPlanId();
        var view = registerUseCase.execute(new RegisterMunicipalityCommand(
                "Maringá", CNPJ_MARINGA, "maringa-test", planId, null));

        assertThat(view.id()).isNotNull();
        assertThat(view.subdomain()).isEqualTo("maringa-test");
        assertThat(view.cnpj()).isEqualTo(CNPJ_MARINGA);
        assertThat(view.active()).isTrue();
    }

    @Test
    void stripsCnpjMaskOnRegister() {
        var planId = planFixtures.enterprisePlanId();
        var view = registerUseCase.execute(new RegisterMunicipalityCommand(
                "Curitiba", "43.773.954/0001-40", "curitiba-test", planId, null));

        assertThat(view.cnpj()).isEqualTo(CNPJ_CURITIBA);
    }

    @Test
    void rejectsDuplicateSubdomain() {
        var intermediateId = planFixtures.intermediatePlanId();
        var basicId = planFixtures.basicPlanId();
        registerUseCase.execute(new RegisterMunicipalityCommand(
                "Londrina", CNPJ_LONDRINA, "londrina-test", intermediateId, null));

        assertThatThrownBy(() ->
                registerUseCase.execute(new RegisterMunicipalityCommand(
                        "Londrina 2", CNPJ_LONDRINA2, "londrina-test", basicId, null))
        ).isInstanceOf(DuplicateSubdomainException.class)
                .hasMessageContaining("londrina-test");
    }

    @Test
    void findsBySubdomain() {
        var planId = planFixtures.basicPlanId();
        registerUseCase.execute(new RegisterMunicipalityCommand(
                "Cascavel", CNPJ_CASCAVEL, "cascavel-test", planId, null));

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
        var planId = planFixtures.intermediatePlanId();
        registerUseCase.execute(new RegisterMunicipalityCommand(
                "Londrina Norte", CNPJ_LONDRINA2, "londrina-norte-test", planId, null));

        assertThat(listActiveUseCase.execute()).hasSizeGreaterThan(before);
    }
}
