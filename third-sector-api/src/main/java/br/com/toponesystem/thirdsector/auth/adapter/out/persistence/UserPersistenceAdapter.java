package br.com.toponesystem.thirdsector.auth.adapter.out.persistence;

import br.com.toponesystem.thirdsector.auth.domain.model.User;
import br.com.toponesystem.thirdsector.auth.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class UserPersistenceAdapter implements UserRepository {

    private final SpringDataUserRepository jpaRepo;
    private final UserMapper mapper;

    @Override
    public User save(User domain) {
        var entity = mapper.toEntity(domain);
        var saved = jpaRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepo.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepo.existsByEmail(email);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepo.findById(id).map(mapper::toDomain);
    }
}
