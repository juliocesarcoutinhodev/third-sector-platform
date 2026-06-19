package br.com.toponesystem.thirdsector.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Third Sector API")
                        .description("API de gestão para organizações do terceiro setor — municípios, organizações, financeiro e transparência pública.")
                        .version("v0.1.0")
                        .contact(new Contact()
                                .name("Topone System")
                                .email("contato@toponesystem.com.br")));
    }
}
