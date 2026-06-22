package br.com.toponesystem.thirdsector.auth.application.usecase;

import br.com.toponesystem.thirdsector.auth.application.dto.UserView;
import br.com.toponesystem.thirdsector.auth.domain.exception.DuplicateEmailException;
import br.com.toponesystem.thirdsector.auth.domain.model.User;
import br.com.toponesystem.thirdsector.auth.domain.model.UserRegisteredEvent;
import br.com.toponesystem.thirdsector.auth.domain.port.out.UserRepository;
import br.com.toponesystem.thirdsector.tenant.application.api.TenantProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final TenantProvider tenantProvider;

    @Transactional
    public UserView execute(CreateUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException(command.email());
        }

        var encodedPassword = passwordEncoder.encode(command.password());
        var user = User.create(command.name(), command.email(), encodedPassword,
                command.role(), command.organizationId());
        var saved = userRepository.save(user);

        eventPublisher.publishEvent(new UserRegisteredEvent(
                saved.getName(), saved.getEmail(), tenantProvider.currentTenant()));

        return UserView.from(saved);
    }
}
