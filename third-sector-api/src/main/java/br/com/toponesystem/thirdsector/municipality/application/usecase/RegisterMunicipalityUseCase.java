package br.com.toponesystem.thirdsector.municipality.application.usecase;

import br.com.toponesystem.thirdsector.municipality.application.MunicipalityView;
import br.com.toponesystem.thirdsector.municipality.domain.exception.DuplicateSubdomainException;
import br.com.toponesystem.thirdsector.municipality.domain.model.Municipality;
import br.com.toponesystem.thirdsector.municipality.domain.model.Plan;
import br.com.toponesystem.thirdsector.municipality.domain.port.out.MunicipalityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegisterMunicipalityUseCase {

    private final MunicipalityRepository repository;

    @Transactional
    public MunicipalityView execute(String name, String cnpj, String subdomain, Plan plan) {
        if (repository.existsBySubdomain(subdomain)) {
            throw new DuplicateSubdomainException(subdomain);
        }
        var municipality = new Municipality(name, stripCnpjMask(cnpj), subdomain, plan);
        var saved = repository.save(municipality);
        return MunicipalityView.from(saved);
    }

    private static String stripCnpjMask(String cnpj) {
        return cnpj.replaceAll("[^0-9]", "");
    }
}
