package br.com.toponesystem.thirdsector.municipality.adapter.in.seed;

import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityCommand;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityUseCase;
import br.com.toponesystem.thirdsector.municipality.domain.exception.DuplicateSubdomainException;
import br.com.toponesystem.thirdsector.municipality.domain.model.Plan;
import br.com.toponesystem.thirdsector.tenant.adapter.out.migration.TenantMigrationService;
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

    private static final String DEV_SUBDOMAIN = "maringa";
    private static final String DEV_MUNICIPALITY_NAME = "Município de Teste (Dev)";
    private static final String DEV_CNPJ = "11222333000181";

    private final RegisterMunicipalityUseCase registerMunicipalityUseCase;
    private final TenantMigrationService tenantMigrationService;

    @Override
    public void run(String... args) {
        log.info("Dev profile active — seeding test municipality '{}'", DEV_SUBDOMAIN);

        try {
            registerMunicipalityUseCase.execute(new RegisterMunicipalityCommand(
                    DEV_MUNICIPALITY_NAME, DEV_CNPJ, DEV_SUBDOMAIN, Plan.BASIC, null));
            log.info("Test municipality '{}' created successfully", DEV_SUBDOMAIN);
        } catch (DuplicateSubdomainException e) {
            log.info("Test municipality '{}' already exists — skipping creation", DEV_SUBDOMAIN);
        }

        log.info("Running tenant migrations for '{}'", DEV_SUBDOMAIN);
        tenantMigrationService.migrate(DEV_SUBDOMAIN);
        log.info("Dev seed completed — tenant '{}' ready", DEV_SUBDOMAIN);
    }
}
