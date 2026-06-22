package br.com.toponesystem.thirdsector.notification.domain.port.out;

import java.util.Map;

public interface EmailTemplateRenderer {

    String render(String templateName, Map<String, Object> templateData,
                  String municipalityName, String municipalityLogo, String subject);
}
