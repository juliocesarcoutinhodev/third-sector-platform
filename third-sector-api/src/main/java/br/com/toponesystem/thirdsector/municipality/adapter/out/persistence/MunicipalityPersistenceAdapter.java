package br.com.toponesystem.thirdsector.municipality.adapter.out.persistence;

import br.com.toponesystem.thirdsector.municipality.domain.exception.DuplicateSubdomainException;
import br.com.toponesystem.thirdsector.municipality.domain.model.Municipality;
import br.com.toponesystem.thirdsector.municipality.domain.port.out.MunicipalityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class MunicipalityPersistenceAdapter implements MunicipalityRepository {

    private final SpringDataMunicipalityRepository jpaRepo;

    @Override
    public Municipality save(Municipality domain) {
        try {
            var entity = toEntity(domain);
            var saved = jpaRepo.save(entity);
            return toDomain(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSubdomainException(domain.getSubdomain());
        }
    }

    @Override
    public Optional<Municipality> findBySubdomain(String subdomain) {
        return jpaRepo.findBySubdomain(subdomain).map(this::toDomain);
    }

    @Override
    public boolean existsBySubdomain(String subdomain) {
        return jpaRepo.existsBySubdomain(subdomain);
    }

    @Override
    public Optional<Municipality> findById(Long id) {
        return jpaRepo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Municipality> findAllActive() {
        return jpaRepo.findAllByActiveTrue().stream().map(this::toDomain).toList();
    }

    private MunicipalityEntity toEntity(Municipality domain) {
        return new MunicipalityEntity(
                domain.getId(),
                domain.getName(),
                domain.getCnpj(),
                domain.getSubdomain(),
                domain.getPlan(),
                domain.getLogo(),
                domain.isActive(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    private Municipality toDomain(MunicipalityEntity entity) {
        return new Municipality(
                entity.getId(),
                entity.getName(),
                entity.getCnpj(),
                entity.getSubdomain(),
                entity.getPlan(),
                entity.getLogo(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
