package br.com.toponesystem.thirdsector.auth.adapter.in.cli;

import br.com.toponesystem.thirdsector.auth.application.usecase.CreateSuperAdminCommand;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateSuperAdminUseCase;
import br.com.toponesystem.thirdsector.auth.domain.exception.DuplicateSuperAdminEmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class SuperAdminBootstrapRunner implements ApplicationRunner {

    private static final String CREATE_SUPER_ADMIN_ARG = "create-super-admin";
    private static final String EMAIL_ARG = "email";
    private static final String NAME_ARG = "name";

    private final CreateSuperAdminUseCase createSuperAdminUseCase;

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption(CREATE_SUPER_ADMIN_ARG)) {
            return;
        }

        var name = parseRequiredOption(args, NAME_ARG, "Super Admin");
        var email = parseRequiredOption(args, EMAIL_ARG, null);

        if (email == null) {
            log.error("");
            log.error("============================================");
            log.error("  ERRO: argumento --email e obrigatorio.");
            log.error("  Uso: --create-super-admin --name=\"Nome\" --email=admin@exemplo.com");
            log.error("============================================");
            log.error("");
            shutdown(1);
            return;
        }

        try {
            var result = createSuperAdminUseCase.execute(
                    new CreateSuperAdminCommand(name, email));

            log.info("");
            log.info("============================================");
            log.info("  SUPER ADMIN CRIADO COM SUCESSO");
            log.info("  ----------------------------------------");
            log.info("  Nome:  {}", result.superAdmin().name());
            log.info("  Email: {}", result.superAdmin().email());
            log.info("  Senha temporaria: {}", result.temporaryPassword());
            log.info("  ----------------------------------------");
            log.info("  IMPORTANTE: Esta senha sera exibida");
            log.info("  apenas esta unica vez. Copie-a agora.");
            log.info("  Troque a senha no primeiro login.");
            log.info("============================================");
            log.info("");

            shutdown(0);

        } catch (DuplicateSuperAdminEmailException e) {
            log.info("");
            log.info("============================================");
            log.info("  Super Admin com o email '{}' ja existe.", email);
            log.info("  Nenhuma acao necessaria.");
            log.info("============================================");
            log.info("");
            shutdown(0);
        }
    }

    private static String parseRequiredOption(ApplicationArguments args, String key, String defaultValue) {
        List<String> values = args.getOptionValues(key);
        if (values != null && !values.isEmpty()) {
            return values.getFirst();
        }
        return defaultValue;
    }

    private void shutdown(int exitCode) {
        System.exit(exitCode);
    }
}
