package br.com.toponesystem.thirdsector.organization.adapter.out.persistence;

import br.com.toponesystem.thirdsector.organization.domain.exception.DuplicateCnpjException;
import br.com.toponesystem.thirdsector.organization.domain.model.Organization;
import br.com.toponesystem.thirdsector.organization.domain.port.out.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class OrganizationPersistenceAdapter implements OrganizationRepository {

    private final SpringDataOrganizationRepository jpaRepo;
    private final OrganizationEntityMapper mapper;

    @Override
    public Organization save(Organization domain) {
        try {
            var entity = mapper.toEntity(domain);
            var saved = jpaRepo.save(entity);
            return mapper.toDomain(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateCnpjException(domain.getCnpj());
        }
    }

    @Override
    public Optional<Organization> findById(UUID id) {
        return jpaRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Organization> findByCnpj(String cnpj) {
        return jpaRepo.findByCnpj(cnpj).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCnpj(String cnpj) {
        return jpaRepo.existsByCnpj(cnpj);
    }
}
