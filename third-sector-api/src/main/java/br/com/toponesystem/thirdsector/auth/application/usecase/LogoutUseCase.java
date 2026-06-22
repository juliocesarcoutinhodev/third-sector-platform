package br.com.toponesystem.thirdsector.auth.application.usecase;

import br.com.toponesystem.thirdsector.auth.domain.port.out.RefreshTokenRepository;
import br.com.toponesystem.thirdsector.auth.domain.port.out.TokenHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHasher tokenHasher;

    @Transactional
    public void execute(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        var hash = tokenHasher.hash(rawRefreshToken);
        var token = refreshTokenRepository.findByTokenHash(hash);

        if (token.isPresent() && !token.get().isRevoked()) {
            refreshTokenRepository.revokeByFamilyId(token.get().getFamilyId());
            log.info("Logout: revoked family={} for userId={}",
                    token.get().getFamilyId(), token.get().getUserId());
        }
    }
}
