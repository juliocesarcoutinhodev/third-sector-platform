package br.com.toponesystem.thirdsector.bootstrap;

import br.com.toponesystem.thirdsector.auth.application.usecase.CreateSuperAdminCommand;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateSuperAdminUseCase;
import br.com.toponesystem.thirdsector.auth.domain.exception.DuplicateSuperAdminEmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
class DevDataSeeder implements CommandLineRunner {

    private static final String DEV_SUPER_ADMIN_NAME = "Super Admin (Dev)";
    private static final String DEV_SUPER_ADMIN_EMAIL = "superadmin@dev.local";
    private static final String DEV_SUPER_ADMIN_PASSWORD = "SuperAdminDev1";

    private final CreateSuperAdminUseCase createSuperAdminUseCase;

    @Override
    public void run(String... args) {
        seedSuperAdmin();
        log.info("Dev seed completed — Super Admin ready. Use the API to create municipalities and users.");
    }

    private void seedSuperAdmin() {
        log.info("Seeding SUPER_ADMIN '{}'", DEV_SUPER_ADMIN_EMAIL);
        try {
            createSuperAdminUseCase.executeWithPassword(
                    new CreateSuperAdminCommand(DEV_SUPER_ADMIN_NAME, DEV_SUPER_ADMIN_EMAIL),
                    DEV_SUPER_ADMIN_PASSWORD);
            log.info("SUPER_ADMIN '{}' created", DEV_SUPER_ADMIN_EMAIL);
        } catch (DuplicateSuperAdminEmailException e) {
            log.info("SUPER_ADMIN '{}' already exists", DEV_SUPER_ADMIN_EMAIL);
        }
    }
}
