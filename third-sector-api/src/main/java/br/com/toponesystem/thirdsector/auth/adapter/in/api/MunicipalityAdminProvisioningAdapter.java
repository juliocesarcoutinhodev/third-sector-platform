package br.com.toponesystem.thirdsector.auth.adapter.in.api;

import br.com.toponesystem.thirdsector.auth.domain.exception.DuplicateEmailException;
import br.com.toponesystem.thirdsector.auth.domain.model.Role;
import br.com.toponesystem.thirdsector.auth.domain.model.User;
import br.com.toponesystem.thirdsector.auth.domain.port.out.UserRepository;
import br.com.toponesystem.thirdsector.auth.application.usecase.SecurePasswordGenerator;
import br.com.toponesystem.thirdsector.municipality.domain.model.MunicipalityAdminCreatedEvent;
import br.com.toponesystem.thirdsector.municipality.domain.port.out.MunicipalityAdminProvisioningPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class MunicipalityAdminProvisioningAdapter implements MunicipalityAdminProvisioningPort {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void provision(String adminName, String adminEmail,
                          String subdomain, String municipalityName) {
        if (userRepository.existsByEmail(adminEmail)) {
            throw new DuplicateEmailException(adminEmail);
        }

        var temporaryPassword = SecurePasswordGenerator.generate();
        var encodedPassword = passwordEncoder.encode(temporaryPassword);
        var admin = User.createWithTemporaryPassword(adminName, adminEmail, encodedPassword, Role.MUNICIPALITY_ADM);
        userRepository.save(admin);

        eventPublisher.publishEvent(
                new MunicipalityAdminCreatedEvent(adminName, adminEmail, temporaryPassword, municipalityName, subdomain));
    }
}
