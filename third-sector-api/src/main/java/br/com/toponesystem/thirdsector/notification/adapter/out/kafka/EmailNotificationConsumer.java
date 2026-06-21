package br.com.toponesystem.thirdsector.notification.adapter.out.kafka;

import br.com.toponesystem.thirdsector.municipality.application.api.MunicipalityDataProvider;
import br.com.toponesystem.thirdsector.notification.domain.model.EmailNotification;
import br.com.toponesystem.thirdsector.notification.domain.port.out.EmailSender;
import br.com.toponesystem.thirdsector.notification.domain.port.out.EmailTemplateRenderer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class EmailNotificationConsumer {

    private final MunicipalityDataProvider municipalityDataProvider;
    private final EmailTemplateRenderer templateRenderer;
    private final EmailSender emailSender;
    private final ObjectMapper objectMapper;

    EmailNotificationConsumer(MunicipalityDataProvider municipalityDataProvider,
                              EmailTemplateRenderer templateRenderer,
                              EmailSender emailSender) {
        this.municipalityDataProvider = municipalityDataProvider;
        this.templateRenderer = templateRenderer;
        this.emailSender = emailSender;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC, groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    void consume(String message) {
        EmailNotification notification;
        try {
            notification = objectMapper.readValue(message, EmailNotification.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize notification message", e);
            return;
        }

        var municipality = municipalityDataProvider.findBySubdomain(notification.getTenantId());
        if (municipality.isEmpty()) {
            log.error("Municipality not found for tenantId={}, skipping notification {}",
                    notification.getTenantId(), notification.getId());
            return;
        }

        var mun = municipality.get();
        try {
            var htmlBody = templateRenderer.render(
                    notification.getTemplateName(),
                    notification.getTemplateData(),
                    mun.name(),
                    mun.logo(),
                    notification.getSubject()
            );

            emailSender.send(notification.getRecipient(), notification.getSubject(), htmlBody);
            log.info("Email sent to {} for tenant {} (template: {})",
                    notification.getRecipient(), mun.subdomain(), notification.getTemplateName());
        } catch (Exception e) {
            log.error("Failed to process notification {} for tenant {}: {}",
                    notification.getId(), notification.getTenantId(), e.getMessage(), e);
            throw e;
        }
    }
}
