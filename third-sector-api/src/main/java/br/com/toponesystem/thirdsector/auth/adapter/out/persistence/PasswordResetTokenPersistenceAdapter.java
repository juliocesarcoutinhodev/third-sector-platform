package br.com.toponesystem.thirdsector.auth.adapter.out.persistence;

import br.com.toponesystem.thirdsector.auth.domain.model.PasswordResetToken;
import br.com.toponesystem.thirdsector.auth.domain.port.out.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class PasswordResetTokenPersistenceAdapter implements PasswordResetTokenRepository {

    private final SpringDataPasswordResetTokenRepository jpaRepo;

    @Override
    public PasswordResetToken save(PasswordResetToken domain) {
        var entity = toEntity(domain);
        var saved = jpaRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return jpaRepo.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    @Transactional
    public void invalidateByUserId(UUID userId) {
        jpaRepo.invalidateByUserId(userId);
    }

    private PasswordResetTokenEntity toEntity(PasswordResetToken domain) {
        return new PasswordResetTokenEntity(
                domain.getId(), domain.getUserId(), domain.getTokenHash(),
                domain.getExpiresAt(), domain.isUsed(), domain.getCreatedAt());
    }

    private PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
        return new PasswordResetToken(
                entity.getId(), entity.getUserId(), entity.getTokenHash(),
                entity.getExpiresAt(), entity.isUsed(), entity.getCreatedAt());
    }
}
