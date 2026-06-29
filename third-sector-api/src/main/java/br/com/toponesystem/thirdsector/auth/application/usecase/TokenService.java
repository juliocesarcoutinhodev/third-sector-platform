package br.com.toponesystem.thirdsector.auth.application.usecase;

import br.com.toponesystem.thirdsector.auth.application.dto.LoginResult;
import br.com.toponesystem.thirdsector.auth.domain.exception.InvalidRefreshTokenException;
import br.com.toponesystem.thirdsector.auth.domain.model.RefreshToken;
import br.com.toponesystem.thirdsector.auth.domain.model.SuspiciousTokenReuseDetectedEvent;
import br.com.toponesystem.thirdsector.auth.domain.port.out.JwtTokenGenerator;
import br.com.toponesystem.thirdsector.auth.domain.port.out.RefreshTokenGenerator;
import br.com.toponesystem.thirdsector.auth.domain.port.out.RefreshTokenRepository;
import br.com.toponesystem.thirdsector.auth.domain.port.out.TokenHasher;
import br.com.toponesystem.thirdsector.auth.domain.port.out.UserRepository;
import br.com.toponesystem.thirdsector.tenant.application.api.TenantProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenGenerator jwtTokenGenerator;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenHasher tokenHasher;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final TenantProvider tenantProvider;
    private final RefreshTokenProperties refreshTokenProperties;
    private final TokenRevocationService tokenRevocationService;

    @Transactional
    public TokenPair createTokenPair(LoginResult loginResult) {
        if (loginResult.mustChangePassword()) {
            var accessToken = jwtTokenGenerator.generateForPasswordChange(
                    loginResult.userId(), loginResult.role(), loginResult.tenantId());
            return new TokenPair(accessToken, null);
        }

        var accessToken = jwtTokenGenerator.generate(
                loginResult.userId(), loginResult.role(),
                loginResult.tenantId(), loginResult.organizationId());

        var refreshTokenValue = refreshTokenGenerator.generate();
        var refreshToken = new RefreshToken(
                loginResult.userId(),
                tokenHasher.hash(refreshTokenValue),
                Instant.now().plusMillis(refreshTokenProperties.expiration()),
                UUID.randomUUID().toString());
        refreshTokenRepository.save(refreshToken);

        return new TokenPair(accessToken, refreshTokenValue);
    }

    @Transactional
    public TokenPair rotateRefreshToken(String rawRefreshToken) {
        var hash = tokenHasher.hash(rawRefreshToken);
        var existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (existing.isExpired()) {
            throw new InvalidRefreshTokenException();
        }

        if (existing.isRevoked()) {
            log.warn("Reused revoked refresh token detected — familyId={}", existing.getFamilyId());
            tokenRevocationService.handleReuse(existing.getUserId(), existing.getFamilyId());
            throw new InvalidRefreshTokenException();
        }

        refreshTokenRepository.revokeById(existing.getId());

        var user = userRepository.findById(existing.getUserId())
                .orElseThrow(InvalidRefreshTokenException::new);

        var accessToken = jwtTokenGenerator.generate(
                user.getId(), user.getRole().name(),
                tenantProvider.currentTenant(), user.getOrganizationId());

        var newRefreshTokenValue = refreshTokenGenerator.generate();
        var newRefreshToken = new RefreshToken(
                user.getId(),
                tokenHasher.hash(newRefreshTokenValue),
                Instant.now().plusMillis(refreshTokenProperties.expiration()),
                existing.getFamilyId());
        refreshTokenRepository.save(newRefreshToken);

        return new TokenPair(accessToken, newRefreshTokenValue);
    }
}
