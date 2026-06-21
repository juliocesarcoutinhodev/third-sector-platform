package br.com.toponesystem.thirdsector.notification.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
public class EmailNotification {

    private final String id;
    private final String recipient;
    private final String subject;
    private final String templateName;
    private final Map<String, Object> templateData;
    private final String tenantId;
    private final Instant createdAt;

    public EmailNotification(String recipient, String subject, String templateName,
                              Map<String, Object> templateData, String tenantId) {
        this(UUID.randomUUID().toString(), recipient, subject, templateName,
                templateData, tenantId, Instant.now());
    }

    @JsonCreator
    public EmailNotification(@JsonProperty("id") String id,
                              @JsonProperty("recipient") String recipient,
                              @JsonProperty("subject") String subject,
                              @JsonProperty("templateName") String templateName,
                              @JsonProperty("templateData") Map<String, Object> templateData,
                              @JsonProperty("tenantId") String tenantId,
                              @JsonProperty("createdAt") Instant createdAt) {
        this.id = id;
        this.recipient = recipient;
        this.subject = subject;
        this.templateName = templateName;
        this.templateData = Map.copyOf(templateData);
        this.tenantId = tenantId;
        this.createdAt = createdAt;
    }
}
