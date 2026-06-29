package br.com.toponesystem.thirdsector.organization.application.usecase;

import br.com.toponesystem.thirdsector.organization.application.dto.OrganizationView;
import br.com.toponesystem.thirdsector.organization.domain.exception.DuplicateCnpjException;
import br.com.toponesystem.thirdsector.organization.domain.exception.OrganizationLimitExceededException;
import br.com.toponesystem.thirdsector.organization.domain.model.Organization;
import br.com.toponesystem.thirdsector.organization.domain.port.out.OrganizationRepository;
import br.com.toponesystem.thirdsector.plan.domain.port.out.PlanRepository;
import br.com.toponesystem.thirdsector.shared.domain.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreateOrganizationUseCase {

    private final OrganizationRepository repository;
    private final PlanRepository planRepository;

    @Transactional
    public OrganizationView execute(CreateOrganizationCommand command) {
        var cnpj = stripCnpjMask(command.cnpj());
        if (repository.existsByCnpj(cnpj)) {
            throw new DuplicateCnpjException(cnpj);
        }

        var tenantId = TenantContext.getCurrentTenant();
        var plan = planRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalStateException(
                        "Plano nao encontrado para o tenant '" + tenantId + "'."));

        var currentCount = repository.count();
        if (plan.hasReachedLimit(currentCount)) {
            throw new OrganizationLimitExceededException(
                    (int) currentCount, plan.getMaxOrganizations());
        }

        var organization = Organization.create(command.name(), cnpj);
        var saved = repository.save(organization);
        return OrganizationView.from(saved);
    }

    private static String stripCnpjMask(String cnpj) {
        return cnpj.replaceAll("[^0-9]", "");
    }
}
