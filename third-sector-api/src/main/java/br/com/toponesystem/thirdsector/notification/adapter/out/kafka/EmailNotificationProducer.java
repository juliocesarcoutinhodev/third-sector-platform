package br.com.toponesystem.thirdsector.notification.adapter.out.kafka;

import br.com.toponesystem.thirdsector.notification.domain.model.EmailNotification;
import br.com.toponesystem.thirdsector.notification.domain.port.out.NotificationPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class EmailNotificationProducer implements NotificationPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    EmailNotificationProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public void publish(EmailNotification notification) {
        try {
            var json = objectMapper.writeValueAsString(notification);
            kafkaTemplate.send(KafkaTopicConfig.TOPIC, notification.getId(), json)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish notification {} for tenant {}",
                                    notification.getId(), notification.getTenantId(), ex);
                        } else {
                            log.info("Notification {} published for tenant {}",
                                    notification.getId(), notification.getTenantId());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize notification {}", notification.getId(), e);
        }
    }
}
