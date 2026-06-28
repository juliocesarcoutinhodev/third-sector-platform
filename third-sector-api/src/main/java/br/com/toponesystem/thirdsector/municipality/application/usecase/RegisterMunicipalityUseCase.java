package br.com.toponesystem.thirdsector.municipality.application.usecase;

import br.com.toponesystem.thirdsector.municipality.application.dto.MunicipalityView;
import br.com.toponesystem.thirdsector.municipality.domain.exception.DuplicateSubdomainException;
import br.com.toponesystem.thirdsector.municipality.domain.model.Municipality;
import br.com.toponesystem.thirdsector.municipality.domain.port.out.MunicipalityRepository;
import br.com.toponesystem.thirdsector.municipality.domain.port.out.TenantProvisioningPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegisterMunicipalityUseCase {

    private final MunicipalityRepository repository;
    private final TenantProvisioningPort tenantProvisioningPort;

    @Transactional
    public MunicipalityView execute(RegisterMunicipalityCommand command) {
        if (repository.existsBySubdomain(command.subdomain())) {
            throw new DuplicateSubdomainException(command.subdomain());
        }
        var municipality = new Municipality(
                command.name(), stripCnpjMask(command.cnpj()),
                command.subdomain(), command.planId(), command.logo());
        var saved = repository.save(municipality);
        var view = MunicipalityView.from(saved);
        tenantProvisioningPort.provision(view.subdomain());
        return view;
    }

    private static String stripCnpjMask(String cnpj) {
        return cnpj.replaceAll("[^0-9]", "");
    }
}
