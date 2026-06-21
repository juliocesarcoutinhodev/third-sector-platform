package br.com.toponesystem.thirdsector.notification.adapter.out.mail;

import br.com.toponesystem.thirdsector.notification.domain.port.out.EmailTemplateRenderer;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.Year;
import java.util.Map;
import java.util.Set;

@Component
class ThymeleafEmailTemplateRenderer implements EmailTemplateRenderer {

    private final SpringTemplateEngine emailTemplateEngine;

    ThymeleafEmailTemplateRenderer(SpringTemplateEngine emailTemplateEngine) {
        this.emailTemplateEngine = emailTemplateEngine;
    }

    @Override
    public String render(String templateName, Map<String, Object> templateData,
                         String municipalityName, String municipalityLogo, String subject) {
        var contentContext = new Context();
        contentContext.setVariable("municipalityName", municipalityName);
        contentContext.setVariable("municipalityLogo", municipalityLogo);
        contentContext.setVariable("subject", subject);
        templateData.forEach(contentContext::setVariable);

        var contentHtml = emailTemplateEngine.process(templateName, Set.of("content"), contentContext);

        var emailContext = new Context();
        emailContext.setVariable("municipalityName", municipalityName);
        emailContext.setVariable("municipalityLogo", municipalityLogo);
        emailContext.setVariable("subject", subject);
        emailContext.setVariable("currentYear", Year.now().getValue());
        emailContext.setVariable("contentHtml", contentHtml);

        return emailTemplateEngine.process("layout/base", emailContext);
    }
}
