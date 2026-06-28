package br.com.toponesystem.thirdsector.organization.domain.port.out;

import br.com.toponesystem.thirdsector.organization.domain.model.Organization;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository {

    Organization save(Organization organization);

    Optional<Organization> findById(UUID id);

    Optional<Organization> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    long count();
}
