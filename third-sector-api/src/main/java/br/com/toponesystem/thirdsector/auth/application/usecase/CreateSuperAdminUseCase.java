package br.com.toponesystem.thirdsector.auth.application.usecase;

import br.com.toponesystem.thirdsector.auth.application.dto.SuperAdminView;
import br.com.toponesystem.thirdsector.auth.domain.exception.DuplicateSuperAdminEmailException;
import br.com.toponesystem.thirdsector.auth.domain.model.SuperAdmin;
import br.com.toponesystem.thirdsector.auth.domain.port.out.SuperAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreateSuperAdminUseCase {

    private final SuperAdminRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CreateSuperAdminResult execute(CreateSuperAdminCommand command) {
        return doExecute(command, SecurePasswordGenerator.generate());
    }

    @Transactional
    public CreateSuperAdminResult executeWithPassword(CreateSuperAdminCommand command, String rawPassword) {
        return doExecute(command, rawPassword);
    }

    private CreateSuperAdminResult doExecute(CreateSuperAdminCommand command, String rawPassword) {
        if (repository.existsByEmail(command.email())) {
            throw new DuplicateSuperAdminEmailException(command.email());
        }

        var encodedPassword = passwordEncoder.encode(rawPassword);

        var superAdmin = SuperAdmin.create(command.name(), command.email(), encodedPassword);
        var saved = repository.save(superAdmin);

        return new CreateSuperAdminResult(SuperAdminView.from(saved), rawPassword);
    }
}
