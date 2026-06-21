package br.com.toponesystem.thirdsector.auth.application.usecase;

import br.com.toponesystem.thirdsector.auth.application.dto.LoginResult;
import br.com.toponesystem.thirdsector.auth.domain.exception.AuthenticationFailedException;
import br.com.toponesystem.thirdsector.auth.domain.port.out.UserRepository;
import br.com.toponesystem.thirdsector.tenant.application.api.TenantProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantProvider tenantProvider;

    @Transactional(readOnly = true)
    public LoginResult execute(LoginCommand command) {
        var user = userRepository.findByEmail(command.email())
                .orElseThrow(AuthenticationFailedException::new);

        if (!user.isActive()) {
            throw new AuthenticationFailedException();
        }

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new AuthenticationFailedException();
        }

        return new LoginResult(
                user.getId(), user.getName(), user.getEmail(),
                user.getRole().name(), tenantProvider.currentTenant(),
                user.getOrganizationId());
    }
}
