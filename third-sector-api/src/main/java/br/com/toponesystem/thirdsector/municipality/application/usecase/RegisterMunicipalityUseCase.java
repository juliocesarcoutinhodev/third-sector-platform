package br.com.toponesystem.thirdsector.municipality.application.usecase;

import br.com.toponesystem.thirdsector.municipality.application.dto.MunicipalityView;
import br.com.toponesystem.thirdsector.municipality.domain.exception.DuplicateSubdomainException;
import br.com.toponesystem.thirdsector.municipality.domain.model.Municipality;
import br.com.toponesystem.thirdsector.municipality.domain.port.out.MunicipalityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegisterMunicipalityUseCase {

    private final MunicipalityRepository repository;

    @Transactional
    public MunicipalityView execute(RegisterMunicipalityCommand command) {
        if (repository.existsBySubdomain(command.subdomain())) {
            throw new DuplicateSubdomainException(command.subdomain());
        }
        var municipality = new Municipality(
                command.name(), stripCnpjMask(command.cnpj()),
                command.subdomain(), command.plan(), command.logo());
        var saved = repository.save(municipality);
        return MunicipalityView.from(saved);
    }

    private static String stripCnpjMask(String cnpj) {
        return cnpj.replaceAll("[^0-9]", "");
    }
}
