package br.com.toponesystem.thirdsector.auth.adapter.out.persistence;

import br.com.toponesystem.thirdsector.auth.domain.model.RefreshToken;
import br.com.toponesystem.thirdsector.auth.domain.port.out.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class RefreshTokenPersistenceAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository jpaRepo;

    @Override
    public RefreshToken save(RefreshToken domain) {
        var entity = toEntity(domain);
        var saved = jpaRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepo.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    @Transactional
    public void revokeById(Long id) {
        jpaRepo.revokeById(id);
    }

    @Override
    @Transactional
    public void revokeByFamilyId(String familyId) {
        jpaRepo.revokeByFamilyId(familyId);
    }

    @Override
    @Transactional
    public void revokeByUserId(Long userId) {
        jpaRepo.revokeByUserId(userId);
    }

    private RefreshTokenEntity toEntity(RefreshToken domain) {
        return new RefreshTokenEntity(
                domain.getId(), domain.getUserId(), domain.getTokenHash(),
                domain.getExpiresAt(), domain.isRevoked(),
                domain.getCreatedAt(), domain.getFamilyId());
    }

    private RefreshToken toDomain(RefreshTokenEntity entity) {
        return new RefreshToken(
                entity.getId(), entity.getUserId(), entity.getTokenHash(),
                entity.getExpiresAt(), entity.isRevoked(),
                entity.getCreatedAt(), entity.getFamilyId());
    }
}
