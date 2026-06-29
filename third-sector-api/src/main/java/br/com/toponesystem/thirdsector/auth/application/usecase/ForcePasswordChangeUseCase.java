package br.com.toponesystem.thirdsector.auth.application.usecase;

import br.com.toponesystem.thirdsector.auth.application.dto.LoginResult;
import br.com.toponesystem.thirdsector.auth.domain.exception.AuthenticationFailedException;
import br.com.toponesystem.thirdsector.auth.domain.port.out.RefreshTokenRepository;
import br.com.toponesystem.thirdsector.auth.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ForcePasswordChangeUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public LoginResult execute(ForcePasswordChangeCommand command) {
        var user = userRepository.findById(command.userId())
                .orElseThrow(AuthenticationFailedException::new);

        if (!user.isMustChangePassword()) {
            throw new AuthenticationFailedException();
        }

        var encodedPassword = passwordEncoder.encode(command.newPassword());
        var updatedUser = user.withPasswordChanged(encodedPassword);
        userRepository.save(updatedUser);

        refreshTokenRepository.revokeByUserId(user.getId());

        return new LoginResult(user.getId(), user.getName(), user.getEmail(),
                user.getRole().name(), command.tenantId(), user.getOrganizationId(), false);
    }
}
