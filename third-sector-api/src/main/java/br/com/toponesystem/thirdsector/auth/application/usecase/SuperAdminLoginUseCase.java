package br.com.toponesystem.thirdsector.auth.application.usecase;

import br.com.toponesystem.thirdsector.auth.application.dto.LoginResult;
import br.com.toponesystem.thirdsector.auth.domain.exception.AuthenticationFailedException;
import br.com.toponesystem.thirdsector.auth.domain.port.out.SuperAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SuperAdminLoginUseCase {

    private final SuperAdminRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResult execute(SuperAdminLoginCommand command) {
        var superAdmin = repository.findByEmail(command.email())
                .orElseThrow(AuthenticationFailedException::new);

        if (!superAdmin.isActive()) {
            throw new AuthenticationFailedException();
        }

        if (!passwordEncoder.matches(command.password(), superAdmin.getPasswordHash())) {
            throw new AuthenticationFailedException();
        }

        return new LoginResult(
                superAdmin.getId(), superAdmin.getName(), superAdmin.getEmail(),
                "SUPER_ADMIN", null, null);
    }
}
