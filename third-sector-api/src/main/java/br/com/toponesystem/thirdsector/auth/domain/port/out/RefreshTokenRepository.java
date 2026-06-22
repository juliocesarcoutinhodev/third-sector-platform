package br.com.toponesystem.thirdsector.auth.domain.port.out;

import br.com.toponesystem.thirdsector.auth.domain.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void revokeById(Long id);

    void revokeByFamilyId(String familyId);

    void revokeByUserId(Long userId);
}
