package br.com.toponesystem.thirdsector.auth.application.usecase;

import br.com.toponesystem.thirdsector.auth.domain.model.PasswordResetRequestedEvent;
import br.com.toponesystem.thirdsector.auth.domain.model.PasswordResetToken;
import br.com.toponesystem.thirdsector.auth.domain.port.out.PasswordResetTokenRepository;
import br.com.toponesystem.thirdsector.auth.domain.port.out.RefreshTokenGenerator;
import br.com.toponesystem.thirdsector.auth.domain.port.out.TokenHasher;
import br.com.toponesystem.thirdsector.auth.domain.port.out.UserRepository;
import br.com.toponesystem.thirdsector.tenant.application.api.TenantProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestPasswordResetUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenGenerator tokenGenerator;
    private final TokenHasher tokenHasher;
    private final ApplicationEventPublisher eventPublisher;
    private final TenantProvider tenantProvider;
    private final PasswordResetProperties properties;

    @Transactional
    public void execute(RequestPasswordResetCommand command) {
        var user = userRepository.findByEmail(command.email());

        if (user.isEmpty()) {
            log.debug("Password reset requested for unknown email: {}", command.email());
            return;
        }

        var u = user.get();
        passwordResetTokenRepository.invalidateByUserId(u.getId());

        var rawToken = tokenGenerator.generate();
        var token = new PasswordResetToken(
                u.getId(),
                tokenHasher.hash(rawToken),
                Instant.now().plusMillis(properties.expiration()));
        passwordResetTokenRepository.save(token);

        eventPublisher.publishEvent(new PasswordResetRequestedEvent(
                u.getName(), u.getEmail(), rawToken, tenantProvider.currentTenant()));
    }
}
