package br.com.toponesystem.thirdsector.auth.application.usecase;

import br.com.toponesystem.thirdsector.auth.domain.exception.InvalidPasswordResetTokenException;
import br.com.toponesystem.thirdsector.auth.domain.port.out.PasswordResetTokenRepository;
import br.com.toponesystem.thirdsector.auth.domain.port.out.RefreshTokenRepository;
import br.com.toponesystem.thirdsector.auth.domain.port.out.TokenHasher;
import br.com.toponesystem.thirdsector.auth.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ConfirmPasswordResetUseCase {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final TokenHasher tokenHasher;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void execute(ConfirmPasswordResetCommand command) {
        var hash = tokenHasher.hash(command.token());
        var token = passwordResetTokenRepository.findByTokenHash(hash)
                .orElseThrow(InvalidPasswordResetTokenException::new);

        if (token.isExpired() || token.isUsed()) {
            throw new InvalidPasswordResetTokenException();
        }

        var user = userRepository.findById(token.getUserId())
                .orElseThrow(InvalidPasswordResetTokenException::new);

        var encodedPassword = passwordEncoder.encode(command.newPassword());
        var updatedUser = new br.com.toponesystem.thirdsector.auth.domain.model.User(
                user.getId(), user.getName(), user.getEmail(), encodedPassword,
                user.getRole(), user.getOrganizationId(), user.isActive(),
                user.getCreatedAt(), java.time.Instant.now());
        userRepository.save(updatedUser);

        passwordResetTokenRepository.invalidateByUserId(user.getId());

        refreshTokenRepository.revokeByUserId(user.getId());
    }
}
