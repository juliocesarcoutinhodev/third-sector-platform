package br.com.toponesystem.thirdsector.municipality.application.usecase;

import br.com.toponesystem.thirdsector.municipality.application.dto.MunicipalityView;
import br.com.toponesystem.thirdsector.municipality.domain.exception.DuplicateSubdomainException;
import br.com.toponesystem.thirdsector.municipality.domain.model.Municipality;
import br.com.toponesystem.thirdsector.municipality.domain.port.out.MunicipalityAdminProvisioningPort;
import br.com.toponesystem.thirdsector.municipality.domain.port.out.MunicipalityRepository;
import br.com.toponesystem.thirdsector.municipality.domain.port.out.TenantProvisioningPort;
import br.com.toponesystem.thirdsector.shared.domain.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegisterMunicipalityUseCase {

    private final MunicipalityRepository repository;
    private final TenantProvisioningPort tenantProvisioningPort;
    private final MunicipalityAdminProvisioningPort adminProvisioningPort;

    @Transactional
    public MunicipalityView execute(RegisterMunicipalityCommand command) {
        if (repository.existsBySubdomain(command.subdomain())) {
            throw new DuplicateSubdomainException(command.subdomain());
        }

        var municipality = new Municipality(
                command.name(), stripCnpjMask(command.cnpj()),
                command.subdomain(), command.planId(), command.logo());
        var saved = repository.save(municipality);

        tenantProvisioningPort.provision(saved.getSubdomain());

        // TenantContext must be set before calling the port so that:
        // 1. The REQUIRES_NEW transaction in the adapter uses the correct schema.
        // 2. TenantContextPropagationTaskDecorator captures the tenant for the async
        //    @ApplicationModuleListener that sends the welcome email.
        TenantContext.setCurrentTenant(saved.getSubdomain());
        try {
            adminProvisioningPort.provision(
                    command.adminName(), command.adminEmail(),
                    saved.getSubdomain(), saved.getName());
        } finally {
            TenantContext.clear();
        }

        return MunicipalityView.from(saved);
    }

    private static String stripCnpjMask(String cnpj) {
        return cnpj.replaceAll("[^0-9]", "");
    }
}
