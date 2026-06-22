package br.com.toponesystem.thirdsector.auth.application.usecase;

import br.com.toponesystem.thirdsector.auth.domain.model.SuspiciousTokenReuseDetectedEvent;
import br.com.toponesystem.thirdsector.auth.domain.port.out.RefreshTokenRepository;
import br.com.toponesystem.thirdsector.auth.domain.port.out.UserRepository;
import br.com.toponesystem.thirdsector.tenant.application.api.TenantProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
class TokenRevocationService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TenantProvider tenantProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReuse(UUID userId, String familyId) {
        refreshTokenRepository.revokeByFamilyId(familyId);

        var user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            eventPublisher.publishEvent(new SuspiciousTokenReuseDetectedEvent(
                    user.getName(), user.getEmail(),
                    tenantProvider.currentTenant(), familyId));
        }
    }
}
