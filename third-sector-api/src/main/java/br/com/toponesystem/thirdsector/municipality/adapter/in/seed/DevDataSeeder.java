package br.com.toponesystem.thirdsector.municipality.adapter.in.seed;

import br.com.toponesystem.thirdsector.auth.application.usecase.CreateUserCommand;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateUserUseCase;
import br.com.toponesystem.thirdsector.auth.domain.exception.DuplicateEmailException;
import br.com.toponesystem.thirdsector.auth.domain.model.Role;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityCommand;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityUseCase;
import br.com.toponesystem.thirdsector.municipality.domain.exception.DuplicateSubdomainException;
import br.com.toponesystem.thirdsector.municipality.domain.model.Plan;
import br.com.toponesystem.thirdsector.organization.application.usecase.CreateOrganizationCommand;
import br.com.toponesystem.thirdsector.organization.application.usecase.CreateOrganizationUseCase;
import br.com.toponesystem.thirdsector.organization.domain.exception.DuplicateCnpjException;
import br.com.toponesystem.thirdsector.tenant.adapter.out.migration.TenantMigrationService;
import br.com.toponesystem.thirdsector.tenant.domain.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
class DevDataSeeder implements CommandLineRunner {

    private static final String DEV_SUBDOMAIN = "maringa";
    private static final String DEV_MUNICIPALITY_NAME = "Município de Teste (Dev)";
    private static final String DEV_CNPJ = "11222333000181";
    private static final String DEV_ADM_EMAIL = "admin@dev.local";
    private static final String DEV_ADM_PASSWORD = "AdminDev1";
    private static final String DEV_ORG_NAME = "Organização de Teste (Dev)";
    private static final String DEV_ORG_CNPJ = "12345678000195";
    private static final String DEV_MGR_EMAIL = "manager@dev.local";
    private static final String DEV_MGR_PASSWORD = "ManagerDev1";

    private UUID devOrganizationId;

    private final RegisterMunicipalityUseCase registerMunicipalityUseCase;
    private final TenantMigrationService tenantMigrationService;
    private final CreateUserUseCase createUserUseCase;
    private final CreateOrganizationUseCase createOrganizationUseCase;

    @Override
    public void run(String... args) {
        seedMunicipality();
        seedMunicipalityAdmin();
        seedOrganization();
        seedOrganizationManager();
        TenantContext.clear();
        log.info("Dev seed completed — tenant '{}' ready with adm, org and manager", DEV_SUBDOMAIN);
    }

    private void seedMunicipality() {
        log.info("Seeding test municipality '{}'", DEV_SUBDOMAIN);
        try {
            registerMunicipalityUseCase.execute(new RegisterMunicipalityCommand(
                    DEV_MUNICIPALITY_NAME, DEV_CNPJ, DEV_SUBDOMAIN, Plan.BASIC, null));
            log.info("Test municipality '{}' created", DEV_SUBDOMAIN);
        } catch (DuplicateSubdomainException e) {
            log.info("Test municipality '{}' already exists", DEV_SUBDOMAIN);
        }
        tenantMigrationService.migrate(DEV_SUBDOMAIN);
    }

    private void seedMunicipalityAdmin() {
        TenantContext.setCurrentTenant(DEV_SUBDOMAIN);
        log.info("Seeding MUNICIPALITY_ADM '{}'", DEV_ADM_EMAIL);
        try {
            createUserUseCase.execute(new CreateUserCommand(
                    "Administrador de Teste", DEV_ADM_EMAIL, DEV_ADM_PASSWORD,
                    Role.MUNICIPALITY_ADM, null));
            log.info("MUNICIPALITY_ADM '{}' created", DEV_ADM_EMAIL);
        } catch (DuplicateEmailException e) {
            log.info("MUNICIPALITY_ADM '{}' already exists", DEV_ADM_EMAIL);
        }
    }

    private void seedOrganization() {
        TenantContext.setCurrentTenant(DEV_SUBDOMAIN);
        log.info("Seeding test organization '{}'", DEV_ORG_NAME);
        try {
            var org = createOrganizationUseCase.execute(new CreateOrganizationCommand(
                    DEV_ORG_NAME, DEV_ORG_CNPJ));
            devOrganizationId = org.id();
            log.info("Test organization '{}' created (id={})", DEV_ORG_NAME, devOrganizationId);
        } catch (DuplicateCnpjException e) {
            devOrganizationId = UUID.randomUUID();
            log.info("Test organization '{}' already exists", DEV_ORG_NAME);
        }
    }

    private void seedOrganizationManager() {
        TenantContext.setCurrentTenant(DEV_SUBDOMAIN);
        log.info("Seeding ORGANIZATION_MANAGER '{}'", DEV_MGR_EMAIL);
        try {
            createUserUseCase.execute(new CreateUserCommand(
                    "Gestor de Teste", DEV_MGR_EMAIL, DEV_MGR_PASSWORD,
                    Role.ORGANIZATION_MANAGER, devOrganizationId));
            log.info("ORGANIZATION_MANAGER '{}' created", DEV_MGR_EMAIL);
        } catch (DuplicateEmailException e) {
            log.info("ORGANIZATION_MANAGER '{}' already exists", DEV_MGR_EMAIL);
        }
    }
}
