package br.com.toponesystem.thirdsector.auth.domain.port.out;

import br.com.toponesystem.thirdsector.auth.domain.model.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    void invalidateByUserId(Long userId);
}
