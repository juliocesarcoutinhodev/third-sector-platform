package br.com.toponesystem.thirdsector.auth.adapter.out.persistence;

import br.com.toponesystem.thirdsector.auth.domain.model.SuperAdmin;
import br.com.toponesystem.thirdsector.auth.domain.port.out.SuperAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class SuperAdminPersistenceAdapter implements SuperAdminRepository {

    private final SpringDataSuperAdminRepository jpaRepo;
    private final SuperAdminMapper mapper;

    @Override
    public SuperAdmin save(SuperAdmin domain) {
        var entity = mapper.toEntity(domain);
        var saved = jpaRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<SuperAdmin> findByEmail(String email) {
        return jpaRepo.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepo.existsByEmail(email);
    }

    @Override
    public Optional<SuperAdmin> findById(UUID id) {
        return jpaRepo.findById(id).map(mapper::toDomain);
    }
}
